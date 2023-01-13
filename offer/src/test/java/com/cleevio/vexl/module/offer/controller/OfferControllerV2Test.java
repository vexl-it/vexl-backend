package com.cleevio.vexl.module.offer.controller;

import com.cleevio.vexl.common.BaseControllerTest;
import com.cleevio.vexl.common.security.filter.SecurityFilter;
import com.cleevio.vexl.module.offer.dto.v2.request.OfferCreateRequest;
import com.cleevio.vexl.module.offer.dto.v2.request.UpdateOfferRequest;
import com.cleevio.vexl.util.CreateOfferRequestTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

import java.util.Collections;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OfferControllerV2.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OfferControllerV2Test extends BaseControllerTest {

    private static final String BASE_URL = "/api/v2/offers";
    private static final String ME_EP = BASE_URL + "/me";
    private static final String MODIFIED_EP = BASE_URL + "/me/modified";
    private static final String PRIVATE_PART_EP = BASE_URL + "/private-part";
    private static final String REFRESH_EP = BASE_URL + "/refresh";
    private static final String REFRESH_REQUEST = """
            {
                "adminIds": ["7452395b496020f8055d6137aaccc2072d19f473cbce51c64731235e6d87440b", "8b83ac57e565fbb675738319a58e02f9208b5453afe88c3fe17f088bdca81431"]
            }
            """;

    @Test
    void testCreateOffer_validInput_shouldReturn200() throws Exception {
        when(offerService.createOffer(any(OfferCreateRequest.class), any(String.class))).thenReturn(OFFER_PRIVATE_PART);

        mvc.perform(post(BASE_URL)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, PHONE_HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(CreateOfferRequestTestUtil.createUpdateOfferCommand(OFFER_ID))))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetMyOffers_validInput_shouldReturn200() throws Exception {
        when(offerService.findOffersByPublicKey(any(String.class))).thenReturn(Collections.singletonList(OFFER_PRIVATE_PART));

        mvc.perform(get(ME_EP)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, PHONE_HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offers[0].id", notNullValue()))
                .andExpect(jsonPath("$.offers[0].offerId", notNullValue()))
                .andExpect(jsonPath("$.offers[0].publicPayload", notNullValue()))
                .andExpect(jsonPath("$.offers[0].privatePayload", notNullValue()))
                .andExpect(jsonPath("$.offers[0].expiration", notNullValue()))
                .andExpect(jsonPath("$.offers[0].createdAt", notNullValue()))
                .andExpect(jsonPath("$.offers[0].modifiedAt", notNullValue()));
    }

    @Test
    public void testGetOfferById_validInput_shouldReturn200() throws Exception {
        when(offerService.findOffersByIdsAndPublicKey(any(), any(String.class))).thenReturn(Collections.singletonList(OFFER_PRIVATE_PART));

        mvc.perform(get(BASE_URL + "?offerIds=1")
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, PHONE_HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", notNullValue()))
                .andExpect(jsonPath("$.[0].offerId", notNullValue()))
                .andExpect(jsonPath("$.[0].publicPayload", notNullValue()))
                .andExpect(jsonPath("$.[0].privatePayload", notNullValue()))
                .andExpect(jsonPath("$.[0].expiration", notNullValue()))
                .andExpect(jsonPath("$.[0].createdAt", notNullValue()))
                .andExpect(jsonPath("$.[0].modifiedAt", notNullValue()));
    }

    @Test
    public void testUpdateOffer_validInput_shouldReturn200() throws Exception {
        when(offerService.updateOffer(any(UpdateOfferRequest.class), any(String.class))).thenReturn(OFFER_PRIVATE_PART);

        mvc.perform(put(BASE_URL)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, PHONE_HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(CreateOfferRequestTestUtil.createUpdateOfferCommand(OFFER_ID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.offerId", notNullValue()))
                .andExpect(jsonPath("$.publicPayload", notNullValue()))
                .andExpect(jsonPath("$.privatePayload", notNullValue()))
                .andExpect(jsonPath("$.expiration", notNullValue()))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.modifiedAt", notNullValue()));
    }

    @Test
    public void testGetNewOrModifiedOffers_validInput_shouldReturn200() throws Exception {
        when(offerService.getNewOrModifiedOffers(any(), any())).thenReturn(Collections.singletonList(OFFER_PRIVATE_PART));

        mvc.perform(get(MODIFIED_EP + "?modifiedAt=2022-04-01T09:50:53.000Z")
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, PHONE_HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offers[0].id", notNullValue()))
                .andExpect(jsonPath("$.offers[0].offerId", notNullValue()))
                .andExpect(jsonPath("$.offers[0].publicPayload", notNullValue()))
                .andExpect(jsonPath("$.offers[0].privatePayload", notNullValue()))
                .andExpect(jsonPath("$.offers[0].expiration", notNullValue()))
                .andExpect(jsonPath("$.offers[0].createdAt", notNullValue()))
                .andExpect(jsonPath("$.offers[0].modifiedAt", notNullValue()));
    }

    @Test
    public void testPostNewPrivatePart_validInput_shouldReturn204() throws Exception {
        mvc.perform(post(PRIVATE_PART_EP)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, PHONE_HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(CreateOfferRequestTestUtil.createCreateOfferPrivatePartRequest(OFFER_ID, USER_PUBLIC_KEY))))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testRefreshOffers_validInput_shouldReturn204() throws Exception {
        mvc.perform(post(REFRESH_EP)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, PHONE_HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REFRESH_REQUEST))
                .andExpect(status().isNoContent());
    }
}
