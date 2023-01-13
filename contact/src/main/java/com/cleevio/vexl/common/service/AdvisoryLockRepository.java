package com.cleevio.vexl.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;

@Repository
@RequiredArgsConstructor
class AdvisoryLockRepository {

    private final static String LOCK_EXCLUSIVELY_QUERY = "SELECT 1 FROM pg_advisory_xact_lock(hashtext(:lockName))";
    private final static String TRY_LOCK_EXCLUSIVELY_QUERY = "SELECT pg_try_advisory_xact_lock(hashtext(:lockName))";

    private final EntityManager entityManager;

    public void lockExclusively(String lockName) {
        entityManager.createNativeQuery(LOCK_EXCLUSIVELY_QUERY)
                .setParameter("lockName", lockName)
                .getSingleResult();
    }

    public boolean tryLockExclusively(String lockName) {
        final Query query = entityManager.createNativeQuery(TRY_LOCK_EXCLUSIVELY_QUERY)
                .setParameter("lockName", lockName);
        return (boolean) query.getSingleResult();
    }
}
