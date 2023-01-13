package com.cleevio.vexl.module.export.service;

import com.cleevio.vexl.common.constant.ModuleLockNamespace;
import com.cleevio.vexl.common.service.AdvisoryLockService;
import com.cleevio.vexl.module.export.constant.ExportAdvisoryLock;
import com.cleevio.vexl.module.export.exception.ExportFailedException;
import com.cleevio.vexl.module.offer.entity.OfferPrivatePart;
import com.cleevio.vexl.module.offer.service.OfferService;
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
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final OfferService offerService;
    private final AdvisoryLockService advisoryLockService;
    private static final String MY_DATA_EXPORT = "My data export";
    private static final String OFFERS = "Your offers";
    private static final String PUBLIC_KEY = "public_key";
    private static final int FONT_HEADLINE = 22;
    private static final int FONT_SIZE = 12;
    private static final float MARGIN = 72;
    private static final int THOUSAND = 1000;
    private static final int ONE = 1;

    @Transactional
    public String exportMyData(final String publicKey) {
        advisoryLockService.lock(
                ModuleLockNamespace.EXPORT,
                ExportAdvisoryLock.EXPORT.name(),
                publicKey
        );

        List<OfferPrivatePart> offers = offerService.findOffersByPublicKey(publicKey);
        try {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final PDDocument document = new PDDocument();
            final PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            final List<String> lines = separateLongTextIntoLines(publicKey, page);

            setContentStream(contentStream);
            contentStream.showText(String.format("%s: ", PUBLIC_KEY));
            contentStream.newLine();
            for (String line : lines) {
                contentStream.showText(line);
                contentStream.newLine();
            }
            contentStream.endText();
            contentStream.close();


            for (OfferPrivatePart offer : offers) {
                final PDPage nextPage = new PDPage();
                PDPageContentStream contentStreamNextPage = new PDPageContentStream(document, nextPage);
                final List<String> offerLines = separateLongTextIntoLines(offer.toString(), nextPage);
                setContentStream(contentStreamNextPage);
                contentStreamNextPage.showText(String.format("%s: ", OFFERS));
                for (String offerLine : offerLines) {
                    contentStreamNextPage.showText(offerLine);
                    contentStreamNextPage.newLine();
                }
                contentStreamNextPage.endText();
                contentStreamNextPage.close();
                document.addPage(nextPage);
            }


            document.save(byteArrayOutputStream);
            document.close();

            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (Exception e) {
            log.error("Error occurred while exporting pdf file", e);
            throw new ExportFailedException();
        }
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

    private List<String> separateLongTextIntoLines(String text, PDPage page)
            throws IOException {
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
