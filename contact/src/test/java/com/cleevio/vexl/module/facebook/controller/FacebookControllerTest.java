package com.cleevio.vexl.module.facebook.controller;

import com.cleevio.vexl.common.BaseControllerTest;
import com.cleevio.vexl.common.security.filter.SecurityFilter;
import com.cleevio.vexl.module.facebook.dto.FacebookUser;
import com.cleevio.vexl.module.facebook.dto.NewFacebookFriends;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.util.Collections;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FacebookController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FacebookControllerTest extends BaseControllerTest {

    private static final String BASE_URL = "/api/v1/facebook/";
    private static final String NOT_IMPORTED = "/not-imported";
    private static final String TOKEN_URL = "/token/";
    private static final String FB_ID = "dummy_fb_id";
    private static final String TOKEN = "dummy_fb_id";

    private static final NewFacebookFriends NEW_FACEBOOK_FRIENDS;
    private static final FacebookUser FACEBOOK_USER;
    private static final String FACEBOOK_USER_ID = "dummy_id";
    private static final String FACEBOOK_USER_NAME = "Davidoff Tilseroz";

    @BeforeEach
    @SneakyThrows
    public void setup() {
        super.setup();
    }

    static {
        FACEBOOK_USER = new FacebookUser();
        FACEBOOK_USER.setId(FACEBOOK_USER_ID);
        FACEBOOK_USER.setName(FACEBOOK_USER_NAME);

        NEW_FACEBOOK_FRIENDS = new NewFacebookFriends(FACEBOOK_USER, Collections.emptyList());
    }

    @Test
    public void testGetFacebookFriends_validInput_shouldReturn200() throws Exception {
        when(facebookService.retrieveContacts(any(String.class), any(String.class))).thenReturn(FACEBOOK_USER);

        mvc.perform(get(BASE_URL + FB_ID + TOKEN_URL + TOKEN)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facebookUser", notNullValue()));
    }

    @Test
    public void testGetNotImported_validInput_shouldReturn200() throws Exception {
        when(facebookService.retrieveFacebookNotImportedConnection(any(), any(), any())).thenReturn(NEW_FACEBOOK_FRIENDS);

        mvc.perform(get(BASE_URL + FB_ID + TOKEN_URL + TOKEN + NOT_IMPORTED)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facebookUser", notNullValue()));
    }

}
