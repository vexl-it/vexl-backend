package com.cleevio.vexl.module.facebook.controller;

import com.cleevio.vexl.common.dto.ErrorResponse;
import com.cleevio.vexl.common.security.filter.SecurityFilter;
import com.cleevio.vexl.module.contact.exception.InvalidFacebookToken;
import com.cleevio.vexl.module.facebook.dto.response.FacebookContactResponse;
import com.cleevio.vexl.module.contact.exception.FacebookException;
import com.cleevio.vexl.module.facebook.service.FacebookService;
import com.cleevio.vexl.module.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Facebook")
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/facebook")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_USER')")
public class FacebookController {

    private final FacebookService facebookService;

    @GetMapping("/{facebookId}/token/{accessToken}")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400 (101102)", description = "Bad request to Facebook", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400 (101103)", description = "Invalid Facebook token", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get Facebook contacts who use the app.",
            description = "We return the current user, in the friends' field we return his friends who use the application " +
                    "and in the friends.friends field we return mutual friends who use the application. " +
                    "WARNING - the user himself will also be in the mutual friends.")
    FacebookContactResponse getFacebookContacts(@PathVariable String facebookId,
                                                @PathVariable String accessToken)
            throws FacebookException, InvalidFacebookToken {
        return new FacebookContactResponse(this.facebookService.retrieveContacts(facebookId, accessToken));
    }

    @GetMapping("/{facebookId}/token/{accessToken}/not-imported")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400 (101102)", description = "Bad request to Facebook", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400 (101103)", description = "Invalid Facebook token", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))

    })
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Returns contacts from Facebook which have not been imported yet.")
    FacebookContactResponse getNewFacebookContacts(@AuthenticationPrincipal User user,
                                                   @PathVariable String facebookId,
                                                   @PathVariable String accessToken)
            throws FacebookException, InvalidFacebookToken {
        return new FacebookContactResponse(this.facebookService.retrieveFacebookNotImportedConnection(user, facebookId, accessToken));
    }
}
