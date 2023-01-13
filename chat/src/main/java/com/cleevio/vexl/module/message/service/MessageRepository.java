package com.cleevio.vexl.module.message.service;

import com.cleevio.vexl.module.inbox.entity.Inbox;
import com.cleevio.vexl.module.message.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

interface MessageRepository extends JpaRepository<Message, Long>, JpaSpecificationExecutor<Message> {

    @Modifying
    @Query("delete from Message m where m.inbox = :inbox and m.pulled = true")
    void deleteAllPulledMessages(Inbox inbox);

    @Modifying
    @Query("delete from Message m where m.inbox = :inbox")
    void deleteAllMessages(Inbox inbox);

    @Query(value = "SELECT last_value from message_id_seq", nativeQuery = true)
    int getLastValueInSequenceForMessage();

    @Query("select count(m) from Message m where m.pulled = false")
    int getNotPulledMessagesCount();
}