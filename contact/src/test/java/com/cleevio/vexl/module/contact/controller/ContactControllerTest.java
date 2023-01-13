package com.cleevio.vexl.module.contact.controller;

import com.cleevio.vexl.common.BaseControllerTest;
import com.cleevio.vexl.common.security.filter.SecurityFilter;
import com.cleevio.vexl.module.contact.dto.request.CommonContactsRequest;
import com.cleevio.vexl.module.contact.dto.request.DeleteContactsRequest;
import com.cleevio.vexl.module.contact.dto.request.ImportRequest;
import com.cleevio.vexl.module.contact.dto.request.NewContactsRequest;
import com.cleevio.vexl.module.contact.dto.response.CommonContactsResponse;
import com.cleevio.vexl.module.contact.dto.response.ImportResponse;
import com.cleevio.vexl.module.user.entity.User;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ContactControllerTest extends BaseControllerTest {

    private static final String BASE_URL = "/api/v1/contacts";
    private static final String COMMON_EP = BASE_URL + "/common";
    private static final String IMPORT_EP = BASE_URL + "/import";
    private static final String ME_EP = BASE_URL + "/me";
    private static final String COUNT_EP = BASE_URL + "/count";
    private static final String NOT_IMPORTED_EP = BASE_URL + "/not-imported";

    private static final String IMPORT_MESSAGE = "dummy_import_message";
    private static final String PHONE_NUMBER_1 = "dummy_phone_number_1";
    private static final String PHONE_NUMBER_2 = "dummy_phone_number_2";
    private static final List<String> PHONES = List.of(PHONE_NUMBER_1, PHONE_NUMBER_2);
    private static final int COUNT = 10;
    private static final ImportRequest IMPORT_REQUEST;
    private static final NewContactsRequest NEW_CONTACTS_REQUEST;
    private static final DeleteContactsRequest DELETE_CONTACTS_REQUEST;
    private static final CommonContactsResponse COMMON_CONTACTS_RESPONSE;
    private static final CommonContactsRequest COMMON_CONTACTS_REQUEST;

    @BeforeEach
    @SneakyThrows
    public void setup() {
        super.setup();
    }

    static {
        IMPORT_REQUEST = new ImportRequest(PHONES);

        NEW_CONTACTS_REQUEST = new NewContactsRequest(PHONES);

        DELETE_CONTACTS_REQUEST = new DeleteContactsRequest(PHONES);

        COMMON_CONTACTS_RESPONSE = new CommonContactsResponse(
                List.of(new CommonContactsResponse.Contacts(PUBLIC_KEY, new CommonContactsResponse.Contacts.CommonContacts(Set.of(HASH)))));

        COMMON_CONTACTS_REQUEST = new CommonContactsRequest(Set.of(PUBLIC_KEY));
    }


    @Test
    public void importContacts_validInput_shouldReturn200() throws Exception {
        when(importService.importContacts(any(User.class), any(ImportRequest.class))).thenReturn(new ImportResponse(true, IMPORT_MESSAGE));

        mvc.perform(post(IMPORT_EP)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(IMPORT_REQUEST)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imported", is(true)))
                .andExpect(jsonPath("$.message", is(IMPORT_MESSAGE)));
    }

    @Test
    public void getMyContacts_validInput_shouldReturn200() throws Exception {
        when(contactService.retrieveContactsByUser(any(User.class), anyInt(), anyInt(), any())).thenReturn(new PageImpl<>(Collections.singletonList(PUBLIC_KEY)));

        mvc.perform(get(ME_EP)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].publickey", notNullValue()));
    }

    @Test
    public void getContactsCount_validInput_shouldReturn200() throws Exception {
        when(contactService.getContactsCountByHashFrom(HASH)).thenReturn(COUNT);

        mvc.perform(get(COUNT_EP)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(COUNT)));
    }

    @Test
    public void getNotImportedContacts_validInput_shouldReturn200() throws Exception {
        when(contactService.retrieveNewContacts(any(User.class), any(NewContactsRequest.class))).thenReturn(Collections.singletonList(PHONE_NUMBER_2));

        mvc.perform(post(NOT_IMPORTED_EP)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(NEW_CONTACTS_REQUEST)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newContacts", is(List.of(PHONE_NUMBER_2))));
    }

    @Test
    public void deleteContacts_validInput_shouldReturn204() throws Exception {
        mvc.perform(delete(BASE_URL)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(DELETE_CONTACTS_REQUEST)))
                .andExpect(status().isNoContent());
    }

    @Test
    public void getCommonFriends_validInput_shouldReturn200() throws Exception {
        when(contactService.retrieveCommonContacts(any(), any())).thenReturn(COMMON_CONTACTS_RESPONSE);

        mvc.perform(post(COMMON_EP)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(COMMON_CONTACTS_REQUEST)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commonContacts[0].publicKey", is(PUBLIC_KEY)))
                .andExpect(jsonPath("$.commonContacts[0].common.hashes", is(List.of(HASH))));
    }

}
