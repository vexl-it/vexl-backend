package com.cleevio.vexl.module.group.service;

import com.cleevio.vexl.module.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

interface GroupRepository extends JpaRepository<Group, Long>, JpaSpecificationExecutor<Group> {

    @Query("select g from Group g where g.uuid in (:uuids) and g.expirationAt > extract(epoch from now())")
    List<Group> findGroupsByUuids(List<String> uuids);

    @Query("select g.uuid from Group g where g.code = :code and g.expirationAt > extract(epoch from now()) and g.closureAt > extract(epoch from now())")
    Optional<String> findGroupUuidByCode(int code);

    @Query("select g from Group g where g.uuid in (:groupUuids) and g.expirationAt < extract(epoch from now())")
    List<Group> retrieveExpiredGroups(List<String> groupUuids);

    @Query("select g from Group g where g.code = :code and g.expirationAt > extract(epoch from now())")
    Optional<Group> findGroupsByCode(int code);
}
