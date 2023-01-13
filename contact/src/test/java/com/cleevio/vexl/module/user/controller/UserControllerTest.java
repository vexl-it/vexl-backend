package com.cleevio.vexl.module.user.controller;

import com.cleevio.vexl.common.BaseControllerTest;
import com.cleevio.vexl.common.security.filter.SecurityFilter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest extends BaseControllerTest {

    private static final String BASE_URL = "/api/v1/users";
    private static final String DELETE_ME_EP = BASE_URL + "/me";

    private static final String UPDATE_FIREBASE_TOKEN = """
             {
                  "firebaseToken": "123"
              }
            """;

    @BeforeEach
    @SneakyThrows
    public void setup() {
        super.setup();
    }

    @Test
    void testCreateNewUser_validInput_shouldReturn204() throws Exception {
        when(userService.existsByPublicKeyAndHash(PUBLIC_KEY, HASH)).thenReturn(false);

        mvc.perform(post(BASE_URL)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteMe_validInput_shouldReturn200() throws Exception {
        mvc.perform(delete(DELETE_ME_EP)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateFirebaseToken_validInput_shouldReturn204() throws Exception {
        mvc.perform(put(BASE_URL)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_FIREBASE_TOKEN))
                .andExpect(status().isNoContent());
    }

}
