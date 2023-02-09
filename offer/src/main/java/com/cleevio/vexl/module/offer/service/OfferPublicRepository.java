package com.cleevio.vexl.module.offer.service;

import com.cleevio.vexl.module.offer.constant.OfferType;
import com.cleevio.vexl.module.offer.dto.MedianPercentageDto;
import com.cleevio.vexl.module.offer.entity.OfferPublicPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

interface OfferPublicRepository extends JpaRepository<OfferPublicPart, Long>, JpaSpecificationExecutor<OfferPublicPart> {

    @Modifying
    @Query("delete from OfferPublicPart o where o.adminId in :adminIds ")
    int deleteByAdminIds(List<String> adminIds);

    Optional<OfferPublicPart> findByAdminId(String adminId);

    @Modifying
    @Query("delete from OfferPublicPart p where p.refreshedAt < :expiration")
    int deleteAllExpiredPublicParts(LocalDate expiration);

    @Query("select count(p) from OfferPublicPart p where p.offerType = :type and p.modifiedAt > :period ")
    int getModifiedOffersCount(LocalDate period, OfferType type);

    @Query("select count(p) from OfferPublicPart p where p.offerType = :type ")
    int getActiveOffersCount(OfferType type);

    @Query(value = "SELECT last_value from offer_public_id_seq", nativeQuery = true)
    int getAllTimeCount();

    @Query(value = """
            SELECT
            PERCENTILE_CONT(0.05)
            within group (order by pocet
            ) as Percentage5,
            PERCENTILE_CONT(0.5)
            within group (order by pocet
            ) as Percentage50,
            PERCENTILE_CONT(0.95)
            within group (order by pocet
            ) as Percentage95
            from
            (select count(*) as pocet
            from offer_private op
            join offer_public op2 on op.offer_id = op2.id
            where op2.offer_type = :type
            GROUP BY op.offer_id
            ) x;
            """, nativeQuery = true)
    MedianPercentageDto getMedianWithPercentageCount(@Param("type") String type);

    @Query("select p from OfferPublicPart p where p.offerId = :offerId")
    Optional<OfferPublicPart> findByOfferId(String offerId);

    @Modifying
    @Query("""
            update OfferPublicPart p set p.refreshedAt = CURRENT_DATE 
            where p.adminId in :adminIds
            """)
    void refreshOffers(List<String> adminIds);
}
