package com.cleevio.vexl.module.push.service;

import com.cleevio.vexl.module.push.entity.Push;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

interface PushRepository extends JpaRepository<Push, Long>, JpaSpecificationExecutor<Push> {

    @Query("select p from Push p JOIN Group g ON g.uuid = p.groupUuid ")
    List<Push> findAllPushNotificationsWithExistingGroup();

    @Modifying
    @Query("""
            delete from Push p where p.id in (
                select pu.id from Push pu
                left join Group g on g.uuid = pu.groupUuid
                where g is null
            )
            """)
    void deleteOrphans();
}
