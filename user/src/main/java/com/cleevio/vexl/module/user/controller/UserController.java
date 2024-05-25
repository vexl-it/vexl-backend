package com.cleevio.vexl.module.user.controller;

import com.cleevio.vexl.common.dto.ErrorResponse;
import com.cleevio.vexl.common.security.filter.SecurityFilter;
import com.cleevio.vexl.common.util.NumberUtils;
import com.cleevio.vexl.module.user.dto.UserData;
import com.cleevio.vexl.module.user.dto.request.ChallengeRequest;
import com.cleevio.vexl.module.user.dto.request.CodeConfirmRequest;
import com.cleevio.vexl.module.user.dto.request.PhoneConfirmRequest;
import com.cleevio.vexl.module.user.dto.response.PhoneConfirmResponse;
import com.cleevio.vexl.module.user.dto.response.ConfirmCodeResponse;
import com.cleevio.vexl.module.user.dto.response.SignatureResponse;
import com.cleevio.vexl.module.user.entity.User;
import com.cleevio.vexl.module.user.service.DashboardNotificationService;
import com.cleevio.vexl.module.user.service.SignatureService;
import com.cleevio.vexl.module.user.service.UserService;
import com.cleevio.vexl.module.user.service.UserVerificationService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User")
@RestController
@RequestMapping(value = "/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserVerificationService userVerificationService;
    private final SignatureService signatureService;
    private final DashboardNotificationService dashboardNotificationService;

    @Autowired
    public UserController(UserService userService, UserVerificationService userVerificationService, SignatureService signatureService, MeterRegistry meterRegistry, DashboardNotificationService dashboardNotificationService) {
        this.userService = userService;
        this.userVerificationService = userVerificationService;
        this.signatureService = signatureService;
        this.dashboardNotificationService = dashboardNotificationService;

        Gauge.builder("analytics.users.count", userService, UserService::getUsersCount)
                .description("Number of users")
                .register(meterRegistry);
    }

    @PostMapping("/confirmation/phone")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400 (100110)", description = "User phone number is invalid", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @Operation(summary = "Phone number confirmation")
    PhoneConfirmResponse requestConfirmPhone(@RequestBody PhoneConfirmRequest phoneConfirmRequest) {
        return new PhoneConfirmResponse(this.userVerificationService.requestConfirmPhone(phoneConfirmRequest));
    }

    @PostMapping("/confirmation/code")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "409 (100101)", description = "User already exists", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500 (100106)", description = "Challenge couldn't be generated", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404 (100104)", description = "Verification not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @Operation(
            summary = "Code number confirmation.",
            description = "If code number is valid, we will generate challenge for user. Challenge is used to verify that the public key is really his. "
    )
    ConfirmCodeResponse confirmCodeAndGenerateCodeChallenge(@RequestBody CodeConfirmRequest codeConfirmRequest) {
        return new ConfirmCodeResponse(this.userVerificationService.requestConfirmCodeAndGenerateCodeChallenge(codeConfirmRequest));
    }

    @PostMapping("/confirmation/challenge")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404 (100103)", description = "User not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404 (100104)", description = "Verification not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400 (100105)", description = "Signature could not be generated", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "406 (100108)", description = "Server could not create message for signature. Public key or hash is invalid.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @Operation(summary = "Verify challenge.", description = "If challenge is verified successfully, we will create certificate for user.")
    SignatureResponse verifyChallengeAndGenerateSignature(@RequestBody ChallengeRequest challengeRequest,
                                                          @RequestHeader(value = SecurityFilter.HEADER_CRYPTO_VERSION, defaultValue = "") final String clientVersionRaw) {
        final int clientVersion = NumberUtils.parseIntOrFallback(clientVersionRaw, 1);
        final UserData userData = this.userService.findValidUserWithChallenge(challengeRequest);

        this.dashboardNotificationService.sendNoticeOnNewUserCreated();

        return new SignatureResponse(this.signatureService.createSignature(userData, clientVersion));
    }

    @GetMapping("/signature/{facebookId}")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400 (100105)", description = "Signature could not be generated", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "406 (100108)", description = "Server could not create message for signature. Public key or hash is invalid.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @Operation(summary = "Generate signature for Facebook.")
    SignatureResponse generateSignature(@AuthenticationPrincipal User user,
                                        @PathVariable String facebookId, @RequestHeader(value = SecurityFilter.HEADER_CRYPTO_VERSION,
            defaultValue = "") final String cryptoVersionRaw) {
        final int cryptoVersion = NumberUtils.parseIntOrFallback(cryptoVersionRaw, 1);

        return new SignatureResponse(this.signatureService.createSignature(
                user.getPublicKey(),
                facebookId,
                false,
                cryptoVersion
        ));
    }

    @DeleteMapping("/me")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponse(responseCode = "200")
    @Operation(summary = "Remove an user")
    void removeMe(@AuthenticationPrincipal User user) {
        this.userService.remove(user);
    }

}
