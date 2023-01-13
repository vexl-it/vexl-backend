package com.cleevio.vexl.module.export.service;

import com.cleevio.vexl.module.contact.constant.ConnectionLevel;
import com.cleevio.vexl.module.contact.service.ContactService;
import com.cleevio.vexl.module.export.exception.ExportFailedException;
import com.cleevio.vexl.module.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final ContactService contactService;

    private static final String MY_DATA_EXPORT = "My data export";
    private static final String PUBLIC_KEY = "Public key: ";
    private static final String CONTACT = "Contact: ";
    private static final String CONTACT_LIST = "Your contact list: ";
    private static final String FIREBASE_TOKEN = "Notification token: ";
    private static final int FONT_HEADLINE = 22;
    private static final int FONT_SIZE = 12;
    private static final float MARGIN = 72;
    private static final int THOUSAND = 1000;
    private static final int ONE = 1;
    private static final int LINES_PER_PAGE = 30;

    public String exportMyData(final User user) {
        int linesPrinted = 0;
        final List<String> contacts = contactService.retrieveContactsByUser(user, 0, 100000000, ConnectionLevel.FIRST).getContent();

        try {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final PDDocument document = new PDDocument();

            final List<String> lines = new ArrayList<>();
            lines.add(PUBLIC_KEY);
            lines.addAll(separateLongTextIntoLines(user.getPublicKey()));
            lines.add(CONTACT);
            lines.addAll(separateLongTextIntoLines(user.getHash()));
            if (user.getFirebaseToken() != null) {
                lines.add(FIREBASE_TOKEN);
                lines.addAll(separateLongTextIntoLines(user.getFirebaseToken()));
            }
            lines.add(CONTACT_LIST);
            for (String contact : contacts) {
                lines.addAll(separateLongTextIntoLines(contact));
            }

            PDPage page = newPage();
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            setContentStream(contentStream);

            for (String line : lines) {
                if (linesPrinted == LINES_PER_PAGE) {
                    contentStream.endText();
                    contentStream.close();
                    document.addPage(page);
                    page = newPage();
                    contentStream = new PDPageContentStream(document, page);
                    setContentStream(contentStream);
                    linesPrinted = 0;
                }
                contentStream.showText(line);
                contentStream.newLine();
                linesPrinted++;
            }

            contentStream.endText();
            contentStream.close();
            document.addPage(page);

            document.save(byteArrayOutputStream);
            document.close();

            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            log.error("Error occurred while exporting pdf file", e);
            throw new ExportFailedException();
        }
    }

    private PDPage newPage() {
        return new PDPage();
    }

    private void setContentStream(PDPageContentStream contentStream) throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset(25, 700);
        contentStream.setLeading(14.5f);
        contentStream.setFont(PDType1Font.COURIER_BOLD, FONT_HEADLINE);
        contentStream.showText(MY_DATA_EXPORT);
        contentStream.newLine();
        contentStream.setFont(PDType1Font.COURIER, FONT_SIZE);
        contentStream.newLine();
    }

    private List<String> separateLongTextIntoLines(String text)
            throws IOException {
        final PDPage page = new PDPage();
        final PDFont pdfFont = PDType1Font.COURIER_BOLD;
        final PDRectangle mediaBox = page.getMediaBox();
        final float width = mediaBox.getWidth() - 2 * MARGIN;
        final List<String> lines = new ArrayList<>();
        int lastSpace = -1;
        while (text.length() > 0) {
            int spaceIndex = text.indexOf(StringUtils.EMPTY, lastSpace + ONE);
            if (spaceIndex < 0)
                spaceIndex = text.length();
            String subString = text.substring(0, spaceIndex);
            float size = FONT_SIZE * pdfFont.getStringWidth(subString) / THOUSAND;
            if (size > width) {
                if (lastSpace < 0)
                    lastSpace = spaceIndex;
                subString = text.substring(0, lastSpace);
                lines.add(subString);
                text = text.substring(lastSpace).trim();
                lastSpace = -1;
            } else if (spaceIndex == text.length()) {
                lines.add(text);
                text = StringUtils.EMPTY;
            } else {
                lastSpace = spaceIndex;
            }
        }
        return lines;
    }
}
