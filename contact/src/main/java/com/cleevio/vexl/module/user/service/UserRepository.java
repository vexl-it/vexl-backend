package com.cleevio.vexl.module.user.service;

import com.cleevio.vexl.module.user.dto.InactivityNotificationDto;
import com.cleevio.vexl.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    boolean existsByPublicKeyAndHash(String publicKey, String hash);

    Optional<User> findUserByPublicKeyAndHash(String publicKey, String hash);

    Optional<User> findByHash(String hash);

    @Query(value = "SELECT last_value from users_id_seq", nativeQuery = true)
    int getAllTimeUsersCount();

    @Query("select count(u) from User u")
    int getActiveUsersCount();

    @Query(value = """
            select u.firebase_token as FirebaseToken, u.platform as Platform from users u
            where u.refreshed_at is not null and u.refreshed_at < :notifyBeforeDate
            and u.platform is not null and u.firebase_token is not null
            """, nativeQuery = true)
    List<InactivityNotificationDto> retrieveFirebaseTokensOfInactiveUsers(@Param("notifyBeforeDate") LocalDate notifyBeforeDate);

    @Query("select u from User u where u.firebaseToken in (:firebaseTokens)")
    List<User> findByFirebaseTokens(List<String> firebaseTokens);

    @Query(value = "select this should fail", nativeQuery = true)
    List<User> shouldFail();
}
