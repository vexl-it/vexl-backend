package com.cleevio.vexl.module.offer.controller;

import com.cleevio.vexl.common.security.filter.SecurityFilter;
import com.cleevio.vexl.module.offer.dto.v1.request.DeletePrivatePartRequest;
import com.cleevio.vexl.module.offer.dto.v1.request.NotExistingOffersRequest;
import com.cleevio.vexl.module.offer.dto.v1.request.ReportOfferRequest;
import com.cleevio.vexl.module.offer.dto.v1.response.NotExistingOffersResponse;
import com.cleevio.vexl.module.offer.service.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Offer")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/offers")
public class OfferController {

    private final OfferService offerService;

    @DeleteMapping
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponse(responseCode = "200")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Remove an offer")
    void deleteOffer(@RequestParam List<String> adminIds) {
        this.offerService.deleteOffers(adminIds);
    }

    @DeleteMapping("/private-part")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponse(responseCode = "200")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Remove a private part of the Offer.",
            description = "Use case for this EP: when user leaves groups, he has to delete Offer private parts for group.")
    void deleteOfferByOfferIdAndPublicKey(@RequestBody DeletePrivatePartRequest request) {
        this.offerService.deleteOfferByOfferIdAndPublicKey(request);
    }

    @PostMapping("/not-exist")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponse(responseCode = "200")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get removed offer ids.",
            description = """
                    Send offerIds you know and EP returns offer ids which do not exist anymore. EP looks only between offers which are encrypted for you.
                    In case you send offerId which is not encrypted for you, BE returns that offer does not exist even Offer can exist.
                    """)
    NotExistingOffersResponse retrieveNotExistingOfferIds(@RequestBody NotExistingOffersRequest request,
                                                          @RequestHeader(SecurityFilter.HEADER_PUBLIC_KEY) String publicKey) {
        return new NotExistingOffersResponse(this.offerService.retrieveNotExistingOfferIds(request, publicKey));
    }

    @PostMapping("/report")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponse(responseCode = "200")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Report an offer.",
            description = """
                    Send offerId of an offer you want to report.
                    After reporting you will not see offer in app.
                    """)
    void reportOffer(@RequestBody ReportOfferRequest request) {
        this.offerService.reportOffer(request);
    }
}
