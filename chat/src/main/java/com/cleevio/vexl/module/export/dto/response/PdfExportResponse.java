package com.cleevio.vexl.module.export.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PdfExportResponse(

        @Schema(description = "Byte array in Base64 encoding")
        String pdfFile

) {
}
