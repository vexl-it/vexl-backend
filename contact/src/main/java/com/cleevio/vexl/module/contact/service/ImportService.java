package com.cleevio.vexl.module.contact.service;

import com.cleevio.vexl.module.contact.dto.request.ImportRequest;
import com.cleevio.vexl.module.contact.dto.response.ImportResponse;
import com.cleevio.vexl.module.user.entity.User;
import com.cleevio.vexl.module.contact.entity.UserContact;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        final List<String> trimContacts = importRequest.contacts()
                .stream()
                .map(String::trim)
                .filter(c -> !c.equals(user.getHash()))
                .toList();

        final Set<String> importedHashes = new HashSet<>();
        final Set<String> existingContacts = this.contactRepository.retrieveExistingContacts(user.getHash());

        for (final String trimContact : trimContacts) {
            if (existingContacts.add(trimContact)) {
                final UserContact contact = UserContact.builder()
                        .hashFrom(user.getHash())
                        .hashTo(trimContact)
                        .build();
                this.contactRepository.save(contact);
                importedHashes.add(trimContact);
            }
        }

        final String message = String.format(IMPORTED_CONTACTS_MESSAGE,
                importedHashes.size(),
                importSize);

        log.info(message);

        contactService.sendNotificationToContacts(importedHashes, user);

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
