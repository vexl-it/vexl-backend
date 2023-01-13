package com.cleevio.vexl.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
class AdvisoryLockRepository {

    private final static String LOCK_EXCLUSIVELY_QUERY = "SELECT 1 FROM pg_advisory_xact_lock(hashtext(:lockName))";

    private final EntityManager entityManager;

    public void lockExclusively(String lockName) {
        entityManager.createNativeQuery(LOCK_EXCLUSIVELY_QUERY)
                .setParameter("lockName", lockName)
                .getSingleResult();
    }
}
