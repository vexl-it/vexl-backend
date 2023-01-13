package com.cleevio.vexl.module.export.service;

import com.cleevio.vexl.module.contact.constant.ConnectionLevel;
import com.cleevio.vexl.module.contact.service.ContactService;
import com.cleevio.vexl.module.user.entity.User;
import lombok.SneakyThrows;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ExportServiceTest {

    private static final User USER;

    //titles
    private static final String MY_DATA_EXPORT = "My data export";
    private static final String PUBLIC_KEY = "Public key: ";
    private static final String CONTACT = "Contact: ";
    private static final String CONTACT_LIST = "Your contact list: ";

    //fields
    private static final String MY_PUBLIC_KEY = "dummy_public_key";
    private static final String MY_HASH = "dummy_hash";
    private static final List<String> CONTACTS = List.of("phone_number_1", "phone_number_2", "phone_number_3", "phone_number_4");
    private final ContactService contactService = mock(ContactService.class);

    private final ExportService exportService = new ExportService(
            contactService
    );

    static {
        USER = new User();
        USER.setPublicKey(MY_PUBLIC_KEY);
        USER.setHash(MY_HASH);
    }

    @Test
    @SneakyThrows
    void createPdFile_shouldBeCreated() {
        Mockito.when(contactService.retrieveContactsByUser(USER, 0, 100000000, ConnectionLevel.FIRST)).thenReturn(new PageImpl<>(CONTACTS));
        final String base64Data = exportService.exportMyData(USER);
        final byte[] decode = Base64.getDecoder().decode(base64Data);
        final PDDocument loadedFile = PDDocument.load(decode);
        final PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);
        final PDFTextStripper tStripper = new PDFTextStripper();
        final String pdfFileInText = tStripper.getText(loadedFile);
        final String lines[] = pdfFileInText.split("\\r?\\n");

        StringBuilder stringBuilderContacts = new StringBuilder();
        for (final String contact : CONTACTS) {
            stringBuilderContacts.append(contact);
        }

        final String textPdf = MY_DATA_EXPORT +
                PUBLIC_KEY +
                MY_PUBLIC_KEY +
                CONTACT +
                MY_HASH +
                CONTACT_LIST +
                stringBuilderContacts;

        StringBuilder stringBuilderLines = new StringBuilder();
        for (final String line : lines) {
            stringBuilderLines.append(line);
        }
        assertThat(stringBuilderLines.toString()).isEqualTo(textPdf);
    }
}
