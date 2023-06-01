package com.cleevio.vexl.module.contact.service;

import com.cleevio.vexl.module.contact.dto.request.ImportRequest;
import com.cleevio.vexl.module.contact.dto.response.ImportResponse;
import com.cleevio.vexl.module.user.entity.User;
import com.cleevio.vexl.module.contact.entity.UserContact;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for importing contacts. All contacts are from phoneHash/facebookIdHash and contact encrypted with HmacSHA256.
 * We get contacts not encrypted, so we need to encrypt them on BE.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ImportService {

    private final ContactRepository contactRepository;
    private final ContactService contactService;
    private static final String NO_CONTACTS_SENT = "You did not import any contact.";
    private static final String IMPORTED_CONTACTS_MESSAGE = "Imported %s / %s contacts.";

    public ImportResponse importContacts(final User user, final @Valid ImportRequest importRequest) {
        return importContacts(user, importRequest, false);
    }
    @Transactional
    public ImportResponse importContacts(final User user, final @Valid ImportRequest importRequest, final boolean replace) {
        final Set<String> existingContactsBeforeClear = this.contactRepository.retrieveExistingContacts(user.getHash());

        if (replace) {
            // Already in transaction. Let's not open another.
            contactRepository.deleteAllByPublicKeyNotTransactional(user.getPublicKey());
        }

        if (importRequest.contacts().isEmpty()) {
            return new ImportResponse(true, NO_CONTACTS_SENT);
        }

        final int importSize = importRequest.contacts().size();
        log.info("Importing new {} contacts for {}",
                importRequest.contacts().size(),
                user.getId());

        final Set<String> trimedContacts = importRequest.contacts()
                .stream()
                .map(String::trim)
                .filter(c -> !c.equals(user.getHash().replace("next:", "")))
                .collect(Collectors.toSet());

        final Set<String> existingContacts = this.contactRepository.retrieveExistingContacts(user.getHash());

        final List<UserContact> contactsToAdd = Sets.difference(trimedContacts, existingContacts)
                .stream()
                .map(c -> UserContact.builder().hashFrom(user.getHash()).hashTo(c).build()).toList();
        final Set<String> hashesToRemove = Sets.difference(existingContacts, trimedContacts);


        // print contents of trimedContacts to Log.info
        for (String hash : trimedContacts) {
            log.info("trimedContact: {}", hash);
        }
        // print contents of contactsToAdd to Log.info
        for (UserContact contact : contactsToAdd) {
            log.info("contactToAdd: {}", contact);
        }
        // print contents of hashesToRemove to Log.info
        for (String hash : hashesToRemove) {
            log.info("hashToRemove: {}", hash);
        }
        // print contentsOf existingContacts to Log.info
        for (String hash : existingContacts) {
            log.info("existingContact: {}", hash);
        }
        // print contents of existingContactsBeforeClear to Log.info
        for (String hash : existingContactsBeforeClear) {
            log.info("existingContactBeforeClear: {}", hash);
        }

        contactRepository.saveAll(contactsToAdd);
        contactRepository.deleteContactsByHashes(user.getHash(), hashesToRemove);

        final String message = String.format(IMPORTED_CONTACTS_MESSAGE,
                contactsToAdd.size(),
                importSize);

        // Notify only the delta contacts
        final Set<String> contactsToNotify = Sets.symmetricDifference(trimedContacts, existingContactsBeforeClear);
        contactService.sendNotificationToContacts(contactsToNotify, user);

        return new ImportResponse(true, message);
    }

    @Transactional
    public void importGroupToContacts(final User user, final String groupUuid) {
        final String trimmedContact = groupUuid.trim();
        if (this.contactRepository.existsContact(user.getHash(), trimmedContact)) {
            return;
        }
        final UserContact contact = UserContact.builder()
                .hashFrom(user.getHash())
                .hashTo(trimmedContact)
                .build();
        this.contactRepository.save(contact);
    }
}
