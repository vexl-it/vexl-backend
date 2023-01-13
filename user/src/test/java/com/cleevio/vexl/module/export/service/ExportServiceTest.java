package com.cleevio.vexl.module.export.service;

import com.cleevio.vexl.module.user.entity.User;
import com.cleevio.vexl.module.user.entity.UserVerification;
import lombok.SneakyThrows;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class ExportServiceTest {

    private static final User USER;
    private static final User USER_WITH_VERIFICATION;
    private static final UserVerification USER_VERIFICATION;
    private static final String PUBLIC_KEY = "public_key";
    private static final String PHONE_NUMBER = "phone_number";

    private static final String MY_PUBLIC_KEY = "dummy_public_key";

    private static final String MY_PHONE_NUMBER = "dummy_phone_number";

    private static final String MY_DATA_EXPORT = "My data export";
    private final ExportService exportService = new ExportService();

    static {
        USER = new User();
        USER.setPublicKey(MY_PUBLIC_KEY);

        USER_VERIFICATION = new UserVerification();
        USER_VERIFICATION.setPhoneNumber(MY_PHONE_NUMBER);

        USER_WITH_VERIFICATION = new User();
        USER_WITH_VERIFICATION.setPublicKey(MY_PUBLIC_KEY);
        USER_WITH_VERIFICATION.setUserVerification(USER_VERIFICATION);
    }

    @Test
    @SneakyThrows
    void createPdfFile_shouldBeCreated() {
        final String base64Data = exportService.exportMyData(USER);
        final byte[] decode = Base64.getDecoder().decode(base64Data);
        final PDDocument loadedFile = PDDocument.load(decode);
        final PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);
        final PDFTextStripper tStripper = new PDFTextStripper();
        final String pdfFileInText = tStripper.getText(loadedFile);
        final String lines[] = pdfFileInText.split("\\r?\\n");

        assertThat(lines[0]).isEqualTo(MY_DATA_EXPORT);
        assertThat(lines[1]).isEqualTo(String.format("%s:", PUBLIC_KEY));
        assertThat(lines[2]).isEqualTo(MY_PUBLIC_KEY);
    }

    @Test
    @SneakyThrows
    void createPdfFile_userHasVerification_shouldBeCreatedWithPhoneNumber() {
        final String base64Data = exportService.exportMyData(USER_WITH_VERIFICATION);
        final byte[] decode = Base64.getDecoder().decode(base64Data);
        final PDDocument loadedFile = PDDocument.load(decode);
        final PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);
        final PDFTextStripper tStripper = new PDFTextStripper();
        final String pdfFileInText = tStripper.getText(loadedFile);
        final String lines[] = pdfFileInText.split("\\r?\\n");

        assertThat(lines[0]).isEqualTo(MY_DATA_EXPORT);
        assertThat(lines[1]).isEqualTo(String.format("%s:", PUBLIC_KEY));
        assertThat(lines[2]).isEqualTo(MY_PUBLIC_KEY);
        assertThat(lines[3]).isEqualTo(String.format("%s: %s", PHONE_NUMBER, MY_PHONE_NUMBER));
    }
}
