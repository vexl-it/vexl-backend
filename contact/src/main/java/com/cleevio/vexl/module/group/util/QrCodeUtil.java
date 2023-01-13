package com.cleevio.vexl.module.group.util;

import com.cleevio.vexl.module.file.dto.request.ImageRequest;
import com.cleevio.vexl.module.group.exception.QrCodeException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QrCodeUtil {

    private static final String PNG = "PNG";
    private static final int DEFAULT_WIDTH = 500;
    private static final int DEFAULT_HEIGHT = 500;
    private static final int ON_COLOR = 0xFF000002;
    private static final int OFF_COLOR = 0xFFFFC041;

    public static ImageRequest getQRCodeImageRequest(String dynamicLink) {
        try {
            final QRCodeWriter qrCodeWriter = new QRCodeWriter();
            final BitMatrix bitMatrix = qrCodeWriter.encode(dynamicLink, BarcodeFormat.QR_CODE, DEFAULT_WIDTH, DEFAULT_HEIGHT);

            final ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            final MatrixToImageConfig con = new MatrixToImageConfig(ON_COLOR, OFF_COLOR);

            MatrixToImageWriter.writeToStream(bitMatrix, PNG, pngOutputStream, con);

            return new ImageRequest(PNG, Base64.getEncoder().encodeToString(pngOutputStream.toByteArray()));
        } catch (WriterException | IOException e) {
            log.error("Error occurred while generating QR code: " + e.getMessage(), e);
            throw new QrCodeException();
        }
    }
}
