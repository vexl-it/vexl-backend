package com.cleevio.vexl.module.challenge.controller;

import com.cleevio.vexl.common.security.filter.SecurityFilter;
import com.cleevio.vexl.module.challenge.config.ChallengeConfig;
import com.cleevio.vexl.module.challenge.dto.request.CreateBatchChallengeRequest;
import com.cleevio.vexl.module.challenge.dto.request.CreateChallengeRequest;
import com.cleevio.vexl.module.challenge.dto.response.ChallengeCreatedResponse;
import com.cleevio.vexl.module.challenge.dto.response.ChallengesBatchCreatedResponse;
import com.cleevio.vexl.module.challenge.mapper.ChallengeMapper;
import com.cleevio.vexl.module.challenge.service.ChallengeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;

@Tag(name = "Challenge")
@RestController
@RequestMapping("/api/v1/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;
    private final ChallengeMapper challengeMapper;
    private final ChallengeConfig config;

    @PostMapping
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new challenge.", description = "Verify that a user actually have the private key to the public key he claims is his")
    ChallengeCreatedResponse createChallenge(@RequestBody CreateChallengeRequest request) {
        return new ChallengeCreatedResponse(
                this.challengeService.createChallenge(request),
                ZonedDateTime.now().plusMinutes(config.expiration())
        );
    }

    @PostMapping("/batch")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new challenges for more public keys.", description = "Verify that a user actually have the private key to the public key he claims is his")
    ChallengesBatchCreatedResponse createChallengeBatch(@RequestBody CreateBatchChallengeRequest request) {
        return new ChallengesBatchCreatedResponse(
                challengeMapper.mapList(
                        this.challengeService.createBatchChallenge(request)
                ),
                ZonedDateTime.now().plusMinutes(config.expiration())
        );
    }
}
