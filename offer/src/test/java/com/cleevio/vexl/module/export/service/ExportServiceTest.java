package com.cleevio.vexl.module.export.service;

import com.cleevio.vexl.common.service.AdvisoryLockService;
import com.cleevio.vexl.module.offer.entity.OfferPrivatePart;
import com.cleevio.vexl.module.offer.entity.OfferPublicPart;
import com.cleevio.vexl.module.offer.service.OfferService;
import lombok.SneakyThrows;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;

import java.util.Base64;
import java.util.List;

import static com.cleevio.vexl.util.CreateOfferRequestTestUtil.USER_PUBLIC_KEY_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ExportServiceTest {
    private static final OfferPublicPart OFFER_PUBLIC_PART;
    private static final OfferPrivatePart OFFER_PRIVATE_PART;
    private static final String DUMMY_STRING_VALUE = "dummy_value";
    private static final String MY_PUBLIC_KEY = "dummy_public_key";
    private static final String PUBLIC_KEY = "public_key";
    private static final String MY_DATA_EXPORT = "My data export";
    private static final String OFFERS = "Your offers";
    private final OfferService offerService = mock(OfferService.class);
    private final AdvisoryLockService advisoryLockService = mock(AdvisoryLockService.class);
    private final ExportService exportService = new ExportService(
            offerService,
            advisoryLockService
    );

    static {
        OFFER_PUBLIC_PART = new OfferPublicPart();
        OFFER_PUBLIC_PART.setId(11L);

        OFFER_PRIVATE_PART = OfferPrivatePart.builder()
                .userPublicKey(USER_PUBLIC_KEY_1)
                .payloadPrivate(DUMMY_STRING_VALUE)
                .build();
    }

    @Test
    @SneakyThrows
    void createPdfFile_shouldBeCreated() {
        Mockito.when(offerService.findOffersByPublicKey(MY_PUBLIC_KEY)).thenReturn(List.of(OFFER_PRIVATE_PART));
        final String base64Data = exportService.exportMyData(MY_PUBLIC_KEY);
        final byte[] decode = Base64.getDecoder().decode(base64Data);
        final PDDocument loadedFile = PDDocument.load(decode);
        final PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);
        final PDFTextStripper tStripper = new PDFTextStripper();
        final String pdfFileInText = tStripper.getText(loadedFile);
        final String lines[] = pdfFileInText.split("\\r?\\n");

        assertThat(lines[0]).isEqualTo(MY_DATA_EXPORT);
        assertThat(lines[1]).isEqualTo(String.format("%s: ", PUBLIC_KEY));
        assertThat(lines[2]).isEqualTo(MY_PUBLIC_KEY);
        assertThat(lines[3]).isEqualTo(MY_DATA_EXPORT);

        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String line : lines) {
            i++;
            if (i > 4) {
                sb.append(line);
            }
        }
        assertThat(sb.toString()).isEqualTo(String.format("%s: %s", OFFERS, OFFER_PRIVATE_PART));
    }
}
