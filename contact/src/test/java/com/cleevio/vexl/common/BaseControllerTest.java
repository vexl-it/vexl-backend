package com.cleevio.vexl.common;

import com.cleevio.vexl.common.service.SignatureService;
import com.cleevio.vexl.common.service.query.CheckSignatureValidityQuery;
import com.cleevio.vexl.module.contact.service.ContactService;
import com.cleevio.vexl.module.export.service.ExportService;
import com.cleevio.vexl.module.facebook.service.FacebookService;
import com.cleevio.vexl.module.contact.service.ImportService;
import com.cleevio.vexl.module.user.entity.User;
import com.cleevio.vexl.module.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

public class BaseControllerTest {

    protected static final String PUBLIC_KEY = "dummy_public_key";
    protected static final String PUBLIC_KEY_2 = "dummy_public_key_2";
    protected static final String HASH = "dummy_hash";
    protected static final String SIGNATURE = "dummy_signature";


    @Autowired
    protected MockMvc mvc;

    @MockBean
    protected UserService userService;

    @MockBean
    protected ExportService exportService;

    @MockBean
    protected ContactService contactService;

    @MockBean
    protected FacebookService facebookService;

    @MockBean
    protected ImportService importService;

    protected final static User USER;

    @MockBean
    protected SignatureService signatureService;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    @SneakyThrows
    public void setup() {
        when(signatureService.isSignatureValid(any(CheckSignatureValidityQuery.class), anyInt())).thenReturn(true);
        when(userService.findByPublicKeyAndHash(any(String.class), any(String.class))).thenReturn(Optional.of(USER));
        when(userService.existsByPublicKeyAndHash(any(String.class), any(String.class))).thenReturn(true);
    }

    static {
        USER = new User();
        USER.setId(1L);
        USER.setPublicKey(PUBLIC_KEY);
        USER.setHash(HASH);
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
