package com.cleevio.vexl.module.offer.service;

import com.cleevio.vexl.module.offer.entity.OfferPrivatePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

interface OfferPrivateRepository extends JpaRepository<OfferPrivatePart, Long>, JpaSpecificationExecutor<OfferPrivatePart> {

    @Modifying
    @Query("delete from OfferPrivatePart p where p.offerPublicPart.id in (select o.id from OfferPublicPart o where o.adminId in :adminIds)")
    int deleteAllPrivatePartsByAdminIds(List<String> adminIds);

    @Query("""
            select p from OfferPrivatePart p 
            where p.userPublicKey = :userPublicKey AND p.payloadPrivate is not null
            """)
    List<OfferPrivatePart> findAllByUserPublicKey(String userPublicKey);

    @Query("select p from OfferPrivatePart p where p.offerPublicPart.offerId = :offerId and p.userPublicKey = :userPublicKey")
    Optional<OfferPrivatePart> findByUserPublicKeyAndPublicPartId(String userPublicKey, String offerId);

    @Query("""
            select p from OfferPrivatePart p 
            where p.userPublicKey= :userPublicKey AND p.offerPublicPart.modifiedAt >= :modifiedAt 
            AND p.payloadPrivate is not null
            order by p.offerPublicPart.id asc
            """)
    List<OfferPrivatePart> findAllByUserPublicKeyAndModifiedAt(String userPublicKey, LocalDate modifiedAt);

    @Modifying
    @Query("""
                 delete from OfferPrivatePart p where 
                 exists (select pub from OfferPublicPart pub where pub = p.offerPublicPart and pub.refreshedAt < :expiration)
            """)
    int deleteAllExpiredPrivateParts(LocalDate expiration);

    @Query("""
            select p from OfferPrivatePart p 
            where p.offerPublicPart.offerId in (:offerIds) and p.userPublicKey = :publicKey
            """)
    List<OfferPrivatePart> findOfferByPublicKeyAndPublicPartIds(String publicKey, List<String> offerIds);

    @Modifying
    @Query("delete from OfferPrivatePart p where p.userPublicKey in (:publicKeys) and p.offerPublicPart.id in (select o.id from OfferPublicPart o where o.adminId in (:adminIds))")
    int deletePrivatePartOfferByAdminIdsAndPublicKeys(List<String> adminIds, List<String> publicKeys);

    @Modifying
    @Query("delete from OfferPrivatePart p where p.userPublicKey in (:publicKeys) and p.offerPublicPart.id = (select o.id from OfferPublicPart o where o.adminId = :adminId)")
    int deletePrivatePartOfferByAdminIdAndPublicKeys(String adminId, Set<String> publicKeys);

    @Query("select case when (count(p) > 0) then true else false end from OfferPrivatePart p where p.userPublicKey in :userPublicKey and p.offerPublicPart.adminId = :adminId")
    boolean existsByUserPublicKeysAndAdminId(Set<String> userPublicKey, String adminId);

    @Query("select pp.offerId from OfferPrivatePart p join p.offerPublicPart pp where p.userPublicKey = :publicKey and pp.offerId in (:offerIds)")
    List<String> findExistingOfferIds(List<String> offerIds, String publicKey);
}
