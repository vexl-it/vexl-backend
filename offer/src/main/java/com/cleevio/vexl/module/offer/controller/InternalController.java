package com.cleevio.vexl.module.offer.controller;

import com.cleevio.vexl.module.offer.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/offers")
public class InternalController {
    private final OfferService offerService;

    @PostMapping(value="/clean-reported-records")
    @ResponseBody
    public String cleanReportedRecords() {
        offerService.cleanReportRecords();
        return "Success";
    }

    @PostMapping(value="/remove-expired-offers")
    @ResponseBody
    public String cleanExpiredOffers() {
        offerService.removeExpiredOffers();
        return "Success";
    }
}
