package com.cleevio.vexl.module.export.controller;

import com.cleevio.vexl.common.BaseControllerTest;
import com.cleevio.vexl.common.security.filter.SecurityFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExportController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExportControllerTest extends BaseControllerTest {

    private static final String DEFAULT_EP = "/api/v1/export";
    private static final String ME_EP = DEFAULT_EP + "/me";
    private static final String RESULT_ME_EP = "dummy_result";

    @Test
    void testExportMyData_validInput_shouldReturn200() throws Exception {
        when(exportService.exportMyData(any())).thenReturn(RESULT_ME_EP);

        mvc.perform(get(ME_EP)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pdfFile", is(RESULT_ME_EP)));
    }
}
