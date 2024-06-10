package com.cleevio.vexl.module.contact.controller;

import com.cleevio.vexl.common.dto.ErrorResponse;
import com.cleevio.vexl.common.dto.PaginatedResponse;
import com.cleevio.vexl.common.security.filter.SecurityFilter;
import com.cleevio.vexl.module.contact.dto.request.CommonContactsRequest;
import com.cleevio.vexl.module.contact.dto.request.DeleteContactsRequest;
import com.cleevio.vexl.module.contact.dto.request.ImportRequest;
import com.cleevio.vexl.module.contact.dto.request.NewContactsRequest;
import com.cleevio.vexl.module.contact.dto.response.CommonContactsResponse;
import com.cleevio.vexl.module.contact.dto.response.ContactsCountResponse;
import com.cleevio.vexl.module.contact.dto.response.NewContactsResponse;
import com.cleevio.vexl.module.contact.dto.response.UserContactResponse;
import com.cleevio.vexl.module.contact.dto.response.ImportResponse;
import com.cleevio.vexl.module.contact.constant.ConnectionLevel;
import com.cleevio.vexl.module.contact.service.ContactService;
import com.cleevio.vexl.module.contact.service.DashboardNotificationService;
import com.cleevio.vexl.module.user.entity.User;
import com.cleevio.vexl.module.contact.service.ImportService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Tag(name = "Contact")
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/contacts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_USER')")
public class ContactController {

    private final ImportService importService;
    private final ContactService contactService;
    private final DashboardNotificationService dashboardNotificationService;

    @Autowired
    public ContactController(ImportService importService, ContactService contactService, MeterRegistry registry, DashboardNotificationService dashboardNotificationService) {
        this.importService = importService;
        this.contactService = contactService;
        this.dashboardNotificationService = dashboardNotificationService;

        Gauge.builder("analytics.contacts.count_unique_users_2", contactService, ContactService::retrieveCountOfUniqueUsers)
                .description("Number of unique users")
                .register(registry);

        Gauge.builder("analytics.contacts.count_unique_contacts_2", contactService, ContactService::retrieveCountOfUniqueContacts)
                .description("Number of unique contacts")
                .register(registry);

        Gauge.builder("analytics.contacts.count_of_connections", contactService, ContactService::retrieveTotalCountOfConnections)
                .description("Total number of connections")
                .register(registry);
    }

    @PostMapping("/import")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400 (101102)", description = "Import list is empty. Nothing to import.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Operation(
            summary = "Import contacts.",
            description = "Contacts have to be sent encrypted with HMAC-SHA256."
    )
    ImportResponse importContacts(@AuthenticationPrincipal User user,
                                  @RequestBody ImportRequest importRequest) {
        return this.importService.importContacts(user, importRequest);
    }

    @PostMapping("/import/replace")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400 (101102)", description = "Import list is empty. Nothing to import.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Operation(
            summary = "Import contacts. Replaces all contact of current user with new ones.",
            description = "Contacts have to be sent encrypted with HMAC-SHA256."
    )
    ImportResponse replaceContacts(@AuthenticationPrincipal User user, @RequestBody ImportRequest importRequest) {
        ImportResponse result = this.importService.importContacts(user, importRequest, true);
        dashboardNotificationService.sendNoticeOnContactsImported();
        return result;
    }

    @GetMapping("/me")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400 (101103)", description = "Invalid connection level. Options - first, second, all. No case sensitive.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all public keys of my contacts.")
    PaginatedResponse<UserContactResponse> getContacts(@AuthenticationPrincipal User user,
                                                       @RequestParam(required = false, defaultValue = "0") int page,
                                                       @RequestParam(required = false, defaultValue = "10") int limit,
                                                       @RequestParam(required = false) ConnectionLevel level,
                                                       HttpServletRequest request) {
        return new PaginatedResponse<>(
                request,
                this.contactService.retrieveContactsByUser(user, page, limit, level)
                        .map(UserContactResponse::new)
        );
    }

    @GetMapping("/count")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponse(responseCode = "200")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get count of contacts by hash.",
            description = "If you send facebookId hash, you will get count of facebook connections. If you send phoneHash, you will get count of phone connections."
    )
    ContactsCountResponse getContactsCount(@AuthenticationPrincipal User user) {
        return new ContactsCountResponse(this.contactService.getContactsCountByHashFrom(user.getHash()));
    }

    @DeleteMapping
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponse(responseCode = "204")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove contacts by hashes.", description = """
            Hashes are deleted for user according hash in headers. 
            If you want to delete phone contacts, you have to have your phone hash in headers.
            If you want to delete facebook contacts, you have to have your facebook hash in headers.
            """)
    void deleteContacts(@AuthenticationPrincipal User user,
                        @RequestBody DeleteContactsRequest deleteContactsRequest) {
        this.contactService.deleteContacts(user, deleteContactsRequest);
    }

    @PostMapping("/not-imported")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponse(responseCode = "200")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Retrieve phone contacts which have not been imported yet",
            description = "You have to send all user's phone contacts. Endpoint then will return only contacts, which are not imported yet."
    )
    NewContactsResponse getNewPhoneContacts(@AuthenticationPrincipal User user,
                                            @RequestBody NewContactsRequest contactsRequest) {
        return new NewContactsResponse(this.contactService.retrieveNewContacts(user, contactsRequest));
    }

    @PostMapping("/common")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponse(responseCode = "200")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Retrieve common connections.",
            description = "Send public keys and common contacts will be returned for every sent public key."
    )
    CommonContactsResponse retrieveCommonContacts(@RequestBody CommonContactsRequest request,
                                                  @RequestHeader(name = SecurityFilter.HEADER_PUBLIC_KEY) String ownerPublicKey) {
        return this.contactService.retrieveCommonContacts(ownerPublicKey, request);
    }
}
