package com.cleevio.vexl.common;

import com.cleevio.vexl.common.service.SignatureService;
import com.cleevio.vexl.module.export.service.ExportService;
import com.cleevio.vexl.module.offer.entity.OfferPrivatePart;
import com.cleevio.vexl.module.offer.entity.OfferPublicPart;
import com.cleevio.vexl.module.offer.service.OfferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

public class BaseControllerTest {

    protected static final String PUBLIC_KEY = "dummy_public_key";
    protected static final String PHONE_HASH = "dummy_hash";
    protected static final String SIGNATURE = "dummy_signature";
    protected static final String USER_PUBLIC_KEY = "dummy_public_key";
    protected static final String OFFER_ID = "dummy_offer_id";
    protected static final String ADMIN_ID = "dummy_admin_id";
    protected static final String DUMMY_STRING_VALUE = "dummy_value";
    protected static final OfferPublicPart OFFER_PUBLIC_PART;
    protected static final OfferPrivatePart OFFER_PRIVATE_PART;

    @Autowired
    protected MockMvc mvc;

    @MockBean
    protected OfferService offerService;

    @MockBean
    protected SignatureService signatureService;

    @MockBean
    protected ExportService exportService;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    @SneakyThrows
    public void setup() {
        OfferPrivatePart privatePart = getPrivatePart();
        privatePart.setOfferPublicPart(getPublicOffer());
        Mockito.when(signatureService.isSignatureValid(any(), anyInt())).thenReturn(true);
        Mockito.when(offerService.findOfferByPublicKeyAndPublicPartId(any(String.class), any(String.class))).thenReturn(privatePart);
    }

    static {
        OFFER_PRIVATE_PART = OfferPrivatePart.builder()
                .id(1L)
                .userPublicKey(USER_PUBLIC_KEY)
                .payloadPrivate(DUMMY_STRING_VALUE)
                .build();

        OFFER_PUBLIC_PART = new OfferPublicPart();
        OFFER_PUBLIC_PART.setId(11L);
        OFFER_PUBLIC_PART.setAdminId(ADMIN_ID);
        OFFER_PUBLIC_PART.setOfferId(OFFER_ID);
        OFFER_PUBLIC_PART.setPayloadPublic(DUMMY_STRING_VALUE);
        OFFER_PUBLIC_PART.setCreatedAt(LocalDate.now());
        OFFER_PUBLIC_PART.setModifiedAt(LocalDate.now());
        OFFER_PUBLIC_PART.setOfferPrivateParts(Set.of(OFFER_PRIVATE_PART));

        OFFER_PRIVATE_PART.setOfferPublicPart(OFFER_PUBLIC_PART);
    }

    protected OfferPublicPart getPublicOffer() {
        OfferPublicPart publicPart = new OfferPublicPart();
        publicPart.setId(11L);
        publicPart.setOfferId("testId");
        publicPart.setOfferPrivateParts(Collections.singleton(getPrivatePart()));
        publicPart.setPayloadPublic(DUMMY_STRING_VALUE);
        return publicPart;
    }

    protected OfferPrivatePart getPrivatePart() {
        return OfferPrivatePart.builder()
                .id(1L)
                .userPublicKey(USER_PUBLIC_KEY)
                .payloadPrivate(DUMMY_STRING_VALUE)
                .build();
    }

    /**
     * Entity to json string body helper
     *
     * @param obj Entity
     * @return JSON string
     */
    protected String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
