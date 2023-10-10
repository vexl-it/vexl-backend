package com.cleevio.vexl.module.contact.service;

import com.cleevio.vexl.module.contact.constant.ConnectionLevel;
import com.cleevio.vexl.module.contact.dto.CommonFriendsDto;
import com.cleevio.vexl.module.contact.entity.UserContact;
import com.cleevio.vexl.module.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

interface ContactRepository extends JpaRepository<UserContact, Long>, JpaSpecificationExecutor<UserContact> {

    @Transactional
    @Modifying
    @Query("delete from UserContact uc where uc.hashFrom in (select u.hash from User u where u.publicKey = :publicKey) ")
    void deleteAllByPublicKey(String publicKey);

    @Modifying
    @Query("delete from UserContact uc where uc.hashFrom in (select u.hash from User u where u.publicKey = :publicKey) ")
    void deleteAllByPublicKeyNotTransactional(String publicKey);

    @Transactional
    @Modifying
    @Query("""
            delete from UserContact uc 
            where uc.hashFrom = :hash and 
            uc.hashTo in (:hashes)
            """)
    void deleteContactsByHashes(String hash, Iterable<String> hashes);

    @Query("select count(distinct uc) from UserContact uc where uc.hashFrom = :hash ")
    int countContactsByHashFrom(String hash);

    @Query("select count(distinct uc) from UserContact uc where uc.hashTo = :hash")
    int countContactsByHashTo(String hash);

    @Query(value = """
            select u2.public_key as FriendPublicKey, array_agg(distinct uc.hash_to) as CommonFriends from user_contact uc
            inner join user_contact uc2 on uc.hash_to = uc2.hash_to
            inner join users u on uc2.hash_from  = u.hash
            inner join user_contact uc3 on uc3.hash_to = uc.hash_to
            inner join users u2 on u2.hash = uc3.hash_from
            where u.public_key = :ownerPublicKey
            and u2.public_key in (:publicKeys)
            group by u2.public_key
            """, nativeQuery = true)
    List<CommonFriendsDto> retrieveCommonContacts(@Param("ownerPublicKey") String ownerPublicKey, @Param("publicKeys") Set<String> publicKeys);

    @Query("""
            select distinct uc.hashTo from UserContact uc
            inner join Group g on g.uuid = uc.hashTo
            where uc.hashFrom = :hash and g.expirationAt > (extract(epoch from now()))
            """
    )
    List<String> getGroupsUuidsByHash(String hash);

    @Transactional
    @Modifying
    @Query("delete from UserContact uc where uc.hashFrom = :hash and uc.hashTo = :contactHash")
    void deleteContactByHash(String hash, String contactHash);

    @Query("""
            select distinct u.publicKey from User u 
            inner join UserContact uc on u.hash = uc.hashFrom 
            where uc.hashTo = :groupUuidHash and u.publicKey not in (:publicKeys)
            """
    )
    List<String> retrieveNewGroupMembers(String groupUuidHash, List<String> publicKeys);

    @Query("""
            select distinct u.publicKey from User u 
            inner join UserContact uc on u.hash = uc.hashFrom 
            where uc.hashTo = :groupUuidHash
            """
    )
    List<String> retrieveAllGroupMembers(String groupUuidHash);

    @Query("select uc.hashTo from UserContact uc where uc.hashFrom = :hash ")
    Set<String> retrieveExistingContacts(String hash);

    @Query("select case when (count(uc) > 0) then true else false end from UserContact uc where uc.hashFrom = :hash and uc.hashTo = :trimContact ")
    boolean existsContact(String hash, String trimContact);

    @Query("""
                select u.firebaseToken from User u 
                inner join UserContact uc on uc.hashFrom = u.hash 
                where u.publicKey <> :publicKey and u.firebaseToken is not null 
                and uc.hashTo = :hash
            """)
    Set<String> retrieveGroupMembersFirebaseTokens(String hash, String publicKey);

    @Query("""
            select distinct u from User u 
            JOIN UserContact uc on u.hash = uc.hashFrom 
            where u.firebaseToken is not null and :newUserHash in (uc.hashTo, concat('next:', uc.hashTo))
            """)
    Set<User> retrieveFirebaseTokensByHashes(String newUserHash);

    @Query(value = """
            select
                distinct(second_degree_friend.firebase_token)
            from user_contact connections_to_imported_contacts
                     inner join users second_degree_friend
                                on second_degree_friend.hash = connections_to_imported_contacts.hash_from
            where true
              and connections_to_imported_contacts.hash_to in (:importedHashes)
              and connections_to_imported_contacts.hash_to != connections_to_imported_contacts.hash_from
              and second_degree_friend.firebase_token is not null and second_degree_friend.hash != :newUserHash;
            """, nativeQuery = true)
    Set<String> retrieveSecondDegreeFirebaseTokensByHashes(@Param("newUserHash") String newUserHash, @Param("importedHashes") Set<String> importedHashes);

    @Query("select count(uc) from UserContact uc")
    int getConnectionsCount();

    @Query(value = "select count(distinct uc.hash_from) from user_contact uc", nativeQuery = true)
    int getCountOfUsers();

    @Query(value = "select count(distinct uc.hash_to) from user_contact uc", nativeQuery = true)
    int getCountOfContacts();
}
