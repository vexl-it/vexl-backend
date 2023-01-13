package com.cleevio.vexl.module.offer.controller;

import com.cleevio.vexl.common.BaseControllerTest;
import com.cleevio.vexl.common.security.filter.SecurityFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OfferController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OfferControllerTest extends BaseControllerTest {

    private static final String BASE_URL = "/api/v1/offers";
    private static final String PRIVATE_PART_EP = BASE_URL + "/private-part";
    private static final String DELETE_PRIVATE_PART_REQUEST = """
            {
                "adminId": "dummy_admin_id",
                "publicKey": ["dummy_public_key_1", "dummy_public_key_2"]
            }
            """;

    @Test
    public void testDeleteOffer_validInput_shouldReturn200() throws Exception {
        mvc.perform(delete(PRIVATE_PART_EP)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, PHONE_HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DELETE_PRIVATE_PART_REQUEST))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeletePrivatePart_validInput_shouldReturn200() throws Exception {
        mvc.perform(delete(BASE_URL + "?adminIds=1")
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, PHONE_HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}
