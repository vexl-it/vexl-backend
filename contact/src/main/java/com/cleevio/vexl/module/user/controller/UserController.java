package com.cleevio.vexl.module.user.controller;

import com.cleevio.vexl.common.security.filter.SecurityFilter;
import com.cleevio.vexl.module.user.constant.Platform;
import com.cleevio.vexl.module.user.dto.request.CreateUserRequest;
import com.cleevio.vexl.module.user.dto.request.FirebaseTokenUpdateRequest;
import com.cleevio.vexl.module.user.dto.request.RefreshUserRequest;
import com.cleevio.vexl.module.user.entity.User;
import com.cleevio.vexl.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.vexl.common.constants.ClientVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Tag(name = "User")
@RestController
@RequestMapping(value = "/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User has been created"),
    })
    @Operation(
            summary = "Create a new user",
            description = "This endpoint must be called first. If you call other endpoints without a user created, it will return Unauthorized."
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_NEW_USER')")
    void createUser(@RequestHeader(name = SecurityFilter.HEADER_PUBLIC_KEY) String publicKey,
                    @RequestHeader(name = SecurityFilter.HEADER_HASH) String hash,
                    @RequestHeader(name = ClientVersion.CLIENT_VERSION_HEADER, defaultValue = "0") String clientVersion,
                    @RequestBody(required = false) @Nullable CreateUserRequest request) {

        this.userService.createUser(
                publicKey,
                ClientVersion.getHashWithPrefixBasedOnClientVersion(hash, clientVersion),
                request == null ? new CreateUserRequest(null) : request
        );
    }

    @PostMapping("/refresh")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User has been refreshed."),
    })
    @Operation(
            summary = "Refresh an user",
            description = "Call this endpoint always when you open app."
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_USER')")
    void refresh(@RequestHeader(name = SecurityFilter.HEADER_PUBLIC_KEY) String publicKey,
                 @RequestHeader(name = SecurityFilter.HEADER_HASH) String hash,
                 @RequestHeader(name = SecurityFilter.X_PLATFORM) Platform platform,
                 @RequestHeader(name = ClientVersion.CLIENT_VERSION_HEADER, defaultValue = "0") String clientVersion,
                 @RequestBody RefreshUserRequest request) {
        this.userService.refreshUser(publicKey, ClientVersion.getHashWithPrefixBasedOnClientVersion(hash, clientVersion), platform, request);
    }

    @PutMapping
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Firebase Token has been updated"),
    })
    @Operation(
            summary = "Update Firebase Token",
            description = "If your Firebase token has been expired, or you want to add new one, call this endpoint."
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_USER')")
    void updateFirebaseToken(@RequestHeader(name = SecurityFilter.HEADER_PUBLIC_KEY) String publicKey,
                             @RequestHeader(name = SecurityFilter.HEADER_HASH) String hash,
                             @RequestHeader(name = ClientVersion.CLIENT_VERSION_HEADER, defaultValue = "0") String clientVersion,
                             @RequestBody FirebaseTokenUpdateRequest request) {
        this.userService.updateFirebaseToken(
                publicKey,
                ClientVersion.getHashWithPrefixBasedOnClientVersion(hash, clientVersion),
                request
        );
    }

    @DeleteMapping("/me")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponse(responseCode = "200")
    @Operation(summary = "Delete a user and his contacts.")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_USER')")
    void deleteMe(@AuthenticationPrincipal User user) {
        this.userService.removeUserAndContacts(user);
    }
}
