package com.cleevio.vexl.module.export.service;

import com.cleevio.vexl.module.inbox.entity.Inbox;
import com.cleevio.vexl.module.message.entity.Message;
import com.cleevio.vexl.module.inbox.entity.Whitelist;
import com.cleevio.vexl.module.inbox.service.InboxService;
import lombok.SneakyThrows;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Base64;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ExportServiceTest {

    private final static Inbox INBOX;
    private final static Message MESSAGE;
    private final static Whitelist WHITELIST;
    private static final String MY_PUBLIC_KEY = "my_public_key";
    private static final String MY_TOKEN = "my_token";
    private static final String MY_MESSAGE = "my_message";
    private static final String MY_SENDER_PUBLIC_KEY = "my_send_public_key";
    private static final String MY_WHITELIST_PUBLIC_KEY = "my_whitelist_public_key";
    private static final String MY_DATA_EXPORT = "My data export";
    private static final String MESSAGES = "My messages";
    private static final String TEXT_MESSAGE = "Text: ";
    private static final String SENDER = "Sender: ";
    private static final String PUBLIC_KEY = "Public key: ";
    private static final String WHITELIST_TITLE = "Your approved/disapproved inboxes";
    private static final String FIREBASE_TOKEN = "Notification token: ";
    private final InboxService inboxService = mock(InboxService.class);

    private final ExportService exportService = new ExportService(
            inboxService
    );

    static {
        MESSAGE = new Message();
        MESSAGE.setMessage(MY_MESSAGE);
        MESSAGE.setSenderPublicKey(MY_SENDER_PUBLIC_KEY);

        WHITELIST = new Whitelist();
        WHITELIST.setPublicKey(MY_WHITELIST_PUBLIC_KEY);

        INBOX = new Inbox();
        INBOX.setPublicKey(MY_PUBLIC_KEY);
        INBOX.setToken(MY_TOKEN);
        INBOX.setMessages(List.of(MESSAGE));
        INBOX.setWhitelists(Set.of(WHITELIST));
    }

    @Test
    @SneakyThrows
    void createPdFile_shouldBeCreated() {
        Mockito.when(inboxService.findInbox(MY_PUBLIC_KEY)).thenReturn(INBOX);
        final String base64Data = exportService.exportMyData(MY_PUBLIC_KEY);
        final byte[] decode = Base64.getDecoder().decode(base64Data);
        final PDDocument loadedFile = PDDocument.load(decode);
        final PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);
        final PDFTextStripper tStripper = new PDFTextStripper();
        final String pdfFileInText = tStripper.getText(loadedFile);
        final String lines[] = pdfFileInText.split("\\r?\\n");

        final String stringBuilderPdf = MY_DATA_EXPORT +
                PUBLIC_KEY +
                INBOX.getPublicKey() +
                FIREBASE_TOKEN +
                INBOX.getToken() +
                MESSAGES +
                TEXT_MESSAGE +
                MESSAGE.getMessage() +
                SENDER +
                MESSAGE.getSenderPublicKey() +
                WHITELIST_TITLE +
                WHITELIST.getPublicKey();

        StringBuilder stringBuilderLines = new StringBuilder();
        for (String line : lines) {
            stringBuilderLines.append(line);
        }
        assertThat(stringBuilderLines.toString()).isEqualTo(stringBuilderPdf);
    }
}
