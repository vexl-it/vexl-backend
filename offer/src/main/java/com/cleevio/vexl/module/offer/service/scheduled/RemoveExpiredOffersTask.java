package com.cleevio.vexl.module.offer.service.scheduled;

import com.cleevio.vexl.module.offer.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoveExpiredOffersTask {

    private final OfferService offerService;

    @Scheduled(cron = "${cron.offer-remove}")
    public void removeExpiredOffers() {
        offerService.removeExpiredOffers();
    }
}
