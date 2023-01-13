package com.cleevio.vexl.module.contact.service;

import com.cleevio.vexl.module.contact.entity.VContact;
import com.cleevio.vexl.module.contact.constant.ConnectionLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.EnumSet;

interface VContactRepository extends JpaRepository<VContact, Long>, JpaSpecificationExecutor<VContact> {

    @Query("""
            select distinct v.publicKey from VContact v 
            where v.myPublicKey = :myPublicKey AND v.level in (:level) 
            """)
    Page<String> findPublicKeysByMyPublicKeyAndLevel(String myPublicKey, EnumSet<ConnectionLevel> level, Pageable pageable);
}