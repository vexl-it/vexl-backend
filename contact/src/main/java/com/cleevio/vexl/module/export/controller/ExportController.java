package com.cleevio.vexl.module.export.controller;

import com.cleevio.vexl.common.security.filter.SecurityFilter;
import com.cleevio.vexl.module.export.dto.response.PdfExportResponse;
import com.cleevio.vexl.module.export.service.ExportService;
import com.cleevio.vexl.module.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Export")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/export")
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/me")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponse(responseCode = "200")
    @Operation(summary = "Export all my data.")
    PdfExportResponse exportMyData(@AuthenticationPrincipal User user) {
        return new PdfExportResponse(exportService.exportMyData(user));
    }

}
