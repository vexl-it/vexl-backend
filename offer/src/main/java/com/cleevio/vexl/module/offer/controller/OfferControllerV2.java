package com.cleevio.vexl.module.offer.controller;

import com.cleevio.vexl.common.dto.ErrorResponse;
import com.cleevio.vexl.common.security.filter.SecurityFilter;
import com.cleevio.vexl.module.offer.constant.OfferType;
import com.cleevio.vexl.module.offer.dto.v2.request.CreateOfferPrivatePartRequest;
import com.cleevio.vexl.module.offer.dto.v2.request.OfferCreateRequest;
import com.cleevio.vexl.module.offer.dto.v2.request.OffersRefreshRequest;
import com.cleevio.vexl.module.offer.dto.v2.request.UpdateOfferRequest;
import com.cleevio.vexl.module.offer.dto.v2.response.OfferUnifiedAdminResponse;
import com.cleevio.vexl.module.offer.dto.v2.response.OfferUnifiedResponse;
import com.cleevio.vexl.module.offer.dto.v2.response.OffersUnifiedResponse;
import com.cleevio.vexl.module.offer.service.OfferService;
import io.micrometer.core.instrument.Counter;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.List;

@Tag(name = "Offer v2")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/offers")
public class OfferControllerV2 {

    private final OfferService offerService;

    private final Counter offerUpdateCounter;
    @Autowired
    public OfferControllerV2(MeterRegistry registry, OfferService offerService) {
        this.offerService = offerService;

        Gauge.builder("analytics.offers.count_all_time", offerService, OfferService::retrieveAllTimeOffersCount)
                .description("All time number of offers")
                .register(registry);

        Gauge.builder("analytics.offers.sell.count_active", () -> offerService.retrieveActiveOffersCount(OfferType.SELL))
                .description("Number of offers")
                .register(registry);

        Gauge.builder("analytics.offers.buy.count_active", () -> offerService.retrieveActiveOffersCount(OfferType.BUY))
                .description("Number of offers")
                .register(registry);

        offerUpdateCounter = Counter.builder("analytics.offers.update")
                .description("Number of updates made to offers")
                .register(registry);
    }

    @PostMapping
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200")
    })
    @Operation(summary = "Create a new offer")
    OfferUnifiedAdminResponse createOffer(@RequestBody OfferCreateRequest request,
                                          @RequestHeader(SecurityFilter.HEADER_PUBLIC_KEY) String publicKey) {
        return new OfferUnifiedAdminResponse(this.offerService.createOffer(request, publicKey));
    }

    @PutMapping
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404 (100101)", description = "Offer for update not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400 (100108)", description = "Cannot update an offer. Missing private part encrypted by offer owner's public key.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400 (100110)", description = "There is more than one private part with the same public key. This is not allowed.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @Operation(summary = "Update an offer")
    OfferUnifiedResponse updateOffer(@RequestBody UpdateOfferRequest request,
                                     @RequestHeader(SecurityFilter.HEADER_PUBLIC_KEY) String publicKey) {
        OfferUnifiedResponse response = new OfferUnifiedResponse(this.offerService.updateOffer(request, publicKey));
        offerUpdateCounter.increment();

        return response;
    }

    @GetMapping("/me")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponse(responseCode = "200")
    @Operation(summary = "Get my offers by my public key.")
    OffersUnifiedResponse getMyOffers(@RequestHeader(SecurityFilter.HEADER_PUBLIC_KEY) String publicKey) {
        return new OffersUnifiedResponse(
                this.offerService.findOffersByPublicKey(publicKey)
                        .stream()
                        .map(OfferUnifiedResponse::new)
                        .toList()
        );
    }

    @GetMapping("/me/modified")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400 (100109)", description = "Wrong date format. Correct date format example - 2022-04-09T09:42:53.000Z. Date MUST be in UTC.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    })
    @Operation(summary = "Get new or modified offers.", description = "Date MUST be in UTC. Correct date format example - 2022-04-09T09:42:53.000Z.")
    OffersUnifiedResponse getNewOrModifiedOffers(@RequestHeader(SecurityFilter.HEADER_PUBLIC_KEY) String publicKey,
                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime modifiedAt) {
        return new OffersUnifiedResponse(
                this.offerService.getNewOrModifiedOffers(modifiedAt.toLocalDate(), publicKey)
                        .stream()
                        .map(OfferUnifiedResponse::new)
                        .toList()
        );
    }

    @GetMapping
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponse(responseCode = "200")
    @Operation(summary = "Get offer by offer id")
    List<OfferUnifiedResponse> getOffer(@RequestParam List<String> offerIds,
                                        @RequestHeader(SecurityFilter.HEADER_PUBLIC_KEY) String publicKey) {
        return this.offerService.findOffersByIdsAndPublicKey(offerIds, publicKey)
                .stream()
                .map(OfferUnifiedResponse::new)
                .toList();
    }

    @PostMapping("/private-part")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponse(responseCode = "204")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Create a private part of the Offer.")
    void addPrivatePartsOffer(@RequestBody CreateOfferPrivatePartRequest request) {
        this.offerService.addPrivatePartsOffer(request);
    }

    @PostMapping("/refresh")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponse(responseCode = "200")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Refresh the offers.", description = "You should always send all your offers. Offers which are not refreshed will be deleted after set period.")
    List<String> refreshOffers(@RequestBody OffersRefreshRequest request,
                       @RequestHeader(SecurityFilter.HEADER_PUBLIC_KEY) String publicKey) {
        return this.offerService.refreshOffers(request, publicKey);
    }
}
