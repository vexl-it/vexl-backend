package com.cleevio.vexl.module.contact.service;

import com.cleevio.vexl.module.contact.dto.CommonFriendsDto;
import com.cleevio.vexl.module.contact.dto.request.CommonContactsRequest;
import com.cleevio.vexl.module.contact.dto.request.DeleteContactsRequest;
import com.cleevio.vexl.module.contact.dto.request.NewContactsRequest;
import com.cleevio.vexl.module.contact.dto.request.ContactsImportedEvent;
import com.cleevio.vexl.module.contact.dto.response.CommonContactsResponse;
import com.cleevio.vexl.module.contact.constant.ConnectionLevel;
import com.cleevio.vexl.module.contact.event.GroupJoinedEvent;
import com.cleevio.vexl.module.stats.constant.StatsKey;
import com.cleevio.vexl.module.stats.dto.StatsDto;
import com.cleevio.vexl.module.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of receiving and deleting contacts. Adding (importing) contacts is done in ImportService.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final VContactRepository vContactRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(readOnly = true)
    public Page<String> retrieveContactsByUser(final User user, final int page, final int limit, final ConnectionLevel level) {
        log.info("Retrieving contacts for user {}",
                user.getId());

        return vContactRepository.findPublicKeysByMyPublicKeyAndLevel(
                user.getPublicKey(),
                ConnectionLevel.ALL == level ? EnumSet.complementOf(EnumSet.of(ConnectionLevel.ALL)) : EnumSet.of(level),
                PageRequest.of(page, limit));
    }

    public void deleteAllContacts(String userPublicKey) {
        this.contactRepository.deleteAllByPublicKey(userPublicKey);
    }

    public void deleteContacts(User user, @Valid DeleteContactsRequest deleteContactsRequest) {
        log.info("Deleting contacts for user {}",
                user.getId());

        final List<String> contactsToDelete = deleteContactsRequest.contactsToDelete().stream()
                .map(String::trim)
                .toList();

        this.contactRepository.deleteContactsByHashes(user.getHash(), contactsToDelete);
    }

    public void deleteContactByHash(String hash, String contactHash) {
        this.contactRepository.deleteContactByHash(hash, contactHash);
    }

    @Transactional(readOnly = true)
    public List<String> retrieveNewContacts(User user, @Valid NewContactsRequest contactsRequest) {
        if (contactsRequest.contacts().isEmpty()) {
            return Collections.emptyList();
        }

        final List<String> newContacts = new ArrayList<>();

        final List<String> trimContacts = contactsRequest.contacts()
                .stream()
                .map(String::trim)
                .toList();

        final Set<String> existingContacts = this.contactRepository.retrieveExistingContacts(user.getHash());

        trimContacts.forEach(tr -> {
            if (existingContacts.add(tr)) {
                newContacts.add(tr);
            }
        });

        return newContacts;
    }

    @Transactional(readOnly = true)
    public Set<String> retrieveExistingContacts(String hashFrom) {
        return this.contactRepository.retrieveExistingContacts(hashFrom);
    }

    @Transactional(readOnly = true)
    public int getContactsCountByHashFrom(final String hash) {
        return this.contactRepository.countContactsByHashFrom(hash);
    }

    @Transactional(readOnly = true)
    public int getContactsCountByHashTo(final String hash) {
        return this.contactRepository.countContactsByHashTo(hash);
    }

    @Transactional(readOnly = true)
    public CommonContactsResponse retrieveCommonContacts(final String ownerPublicKey, @Valid final CommonContactsRequest request) {
        final Set<String> publicKeys = request.publicKeys()
                .stream()
                .map(String::trim)
                .filter(pk -> !pk.equals(ownerPublicKey))
                .collect(Collectors.toSet());
        final List<CommonContactsResponse.Contacts> contacts = new ArrayList<>();

        final List<CommonFriendsDto> commonFriendsDto = this.contactRepository.retrieveCommonContacts(
                ownerPublicKey,
                publicKeys
        );

        commonFriendsDto.forEach(it -> contacts.add(
                        new CommonContactsResponse.Contacts(
                                it.getFriendPublicKey(),
                                new CommonContactsResponse.Contacts.CommonContacts(it.getCommonFriends())
                        )
                )
        );

        return new CommonContactsResponse(contacts);
    }

    @Transactional(readOnly = true)
    public List<String> getGroupsUuidsByHash(String hash) {
        return this.contactRepository.getGroupsUuidsByHash(hash);
    }

    @Transactional(readOnly = true)
    public List<String> retrieveNewGroupMembers(final String groupUuidHash, final List<String> publicKeys) {
        return publicKeys.isEmpty() ?
                Collections.emptyList() :
                this.contactRepository.retrieveNewGroupMembers(groupUuidHash, publicKeys);
    }

    @Transactional(readOnly = true)
    public void storeNotificationsForLaterProcessing(final String groupUuid, final String publicKey) {
        final Set<String> membersFirebaseTokens = this.contactRepository.retrieveGroupMembersFirebaseTokens(groupUuid, publicKey);
        if (membersFirebaseTokens.isEmpty()) return;
        applicationEventPublisher.publishEvent(new GroupJoinedEvent(groupUuid, membersFirebaseTokens));
    }

    /**
     * Send a notification to all existing contacts, so they can encrypt their Offers for a new user.
     */
    @Async("sendNotificationToContactsExecutor")
    @Transactional(readOnly = true)
    public void sendNotificationToContacts(final Set<String> importedHashes, final User user) {
        if (importedHashes.isEmpty()) {
            return;
        }

        long start = System.currentTimeMillis();

        log.info("Running retrieveFirebaseTokensByHashes");
        final Set<String> firebaseTokens = retrieveFirebaseTokensByHashes(importedHashes, user.getHash());
        if (firebaseTokens.isEmpty()) {
            log.info("Returning firebaseTokens is empty");
            return;
        }

        log.info("Running retrieveSecondDegreeFirebaseTokensByHashes");
        final Set<String> firebaseTokensSecondDegrees = retrieveSecondDegreeFirebaseTokensByHashes(importedHashes, user.getHash(), firebaseTokens);

        log.info("Sending notification from {} to {} contacts and {} second degree contacts",
                user.getHash(),
                firebaseTokens.size(),
                firebaseTokensSecondDegrees.size());

        log.info("FCM tokens for contacts took {} s", (System.currentTimeMillis() - start) / 1000);
        applicationEventPublisher.publishEvent(new ContactsImportedEvent(firebaseTokens, firebaseTokensSecondDegrees, user.getPublicKey()));
    }

    public Set<String> retrieveFirebaseTokensByHashes(final Set<String> importedHashes, final String hash) {
        final Set<User> users = this.contactRepository.retrieveFirebaseTokensByHashes(hash);
        return users.stream()
                .filter(user -> importedHashes.contains(user.getHash()))
                .map(User::getFirebaseToken)
                .collect(Collectors.toSet());
    }

    public Set<String> retrieveSecondDegreeFirebaseTokensByHashes(final Set<String> importedHashes, final String hash, final Set<String> firebaseTokens) {
        final Set<String> usersSecondDegree = this.contactRepository.retrieveSecondDegreeFirebaseTokensByHashes(hash, importedHashes);
        return usersSecondDegree.stream()
                .filter(firebaseToken -> !firebaseTokens.contains(firebaseToken))
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public List<String> retrieveRemovedGroupMembers(String groupUuid, List<String> publicKeys) {
        final List<String> activeMembersPublicKeys = this.contactRepository.retrieveAllGroupMembers(groupUuid);
        publicKeys.removeAll(activeMembersPublicKeys);
        return publicKeys;
    }

    @Transactional(readOnly = true)
    public List<StatsDto> retrieveStats(final StatsKey... statsKeys) {
        final List<StatsDto> statsDtos = new ArrayList<>();
        Arrays.stream(statsKeys).forEach(statKey -> {
            if (statKey == StatsKey.CONTACTS_COUNT) {
                statsDtos.add(new StatsDto(
                        StatsKey.CONTACTS_COUNT,
                        this.contactRepository.getConnectionsCount()
                ));
            } else if (statKey == StatsKey.UNIQUE_CONTACTS_COUNT) {
                statsDtos.add(new StatsDto(
                        StatsKey.UNIQUE_CONTACTS_COUNT,
                        this.contactRepository.getCountOfContacts()
                ));
            } else if (statKey == StatsKey.UNIQUE_USERS_COUNT) {
                statsDtos.add(new StatsDto(
                        StatsKey.UNIQUE_USERS_COUNT,
                        this.contactRepository.getCountOfUsers()
                ));
            }
        });
        return statsDtos;
    }

    @Transactional(readOnly = true)
    public int retrieveTotalCountOfConnections() {
        return contactRepository.getConnectionsCount();
    }

    @Transactional(readOnly = true)
    public int retrieveCountOfUniqueUsers() {
        return contactRepository.getCountOfUsers();
    }

    @Transactional(readOnly = true)
    public int retrieveCountOfUniqueContacts() {
        return contactRepository.getCountOfContacts();
    }
}
