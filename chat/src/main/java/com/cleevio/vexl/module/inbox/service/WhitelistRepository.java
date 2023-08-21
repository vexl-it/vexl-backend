package com.cleevio.vexl.module.inbox.service;

import com.cleevio.vexl.module.inbox.entity.Inbox;
import com.cleevio.vexl.module.inbox.entity.Whitelist;
import com.cleevio.vexl.module.inbox.constant.WhitelistState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

interface WhitelistRepository extends JpaRepository<Whitelist, Long>, JpaSpecificationExecutor<Whitelist> {

    @Query("select case when (count(w) > 0) then true else false end from Whitelist w where w.inbox = :receiverInbox and w.publicKey = :publicKeySenderHash" +
            "  and w.state = :state")
    boolean isSenderInWhitelist(String publicKeySenderHash, Inbox receiverInbox, WhitelistState state);

    @Query("select case when (count(w) > 0) then true else false end from Whitelist w where w.inbox = :receiverInbox and w.publicKey = :publicKeySenderHash")
    boolean isSenderInWhitelist(String publicKeySenderHash, Inbox receiverInbox);

    @Query("select w from Whitelist w where w.inbox = :inbox and w.publicKey = :publicKeyToBlockHash")
    Optional<Whitelist> findOnWhitelist(Inbox inbox, String publicKeyToBlockHash);

    @Query("delete from Whitelist w where w.inbox = :inbox and w.publicKey = :publicKey")
    int deleteFromWhitelist(Inbox inbox, String publicKey);

    @Query("select w from Whitelist w where w.inbox = :inbox and w.publicKey = :publicKey and w.state = :state")
    Optional<Whitelist> findByPublicKey(Inbox inbox, String publicKey, WhitelistState state);

    @Query("""
            select case when (count(w) = :size) then true else false end from Whitelist w
            INNER JOIN w.inbox i
            where i.publicKey in :receiverPublicKeysHashes
            and w.publicKey in :publicKeySenderHashes
            and w.state = :state
            """)
    boolean areAllSendersInReceiversWhitelistApproved(List<String> publicKeySenderHashes, List<String> receiverPublicKeysHashes, WhitelistState state, Long size);
}
