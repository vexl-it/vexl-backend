package com.cleevio.vexl.module.stats.service;

import com.cleevio.vexl.common.IntegrationTest;
import com.cleevio.vexl.module.message.constant.MessageType;
import com.cleevio.vexl.module.inbox.constant.Platform;
import com.cleevio.vexl.module.inbox.entity.Inbox;
import com.cleevio.vexl.module.message.entity.Message;
import com.cleevio.vexl.module.message.service.MessageService;
import com.cleevio.vexl.module.stats.constant.StatsKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StatsServiceIT {

    private final StatsService statsService;
    private final StatsRepository repository;
    private final MessageService messageService;

    private final static String MESSAGE = "dummy_message";
    private final static String SENDER_PUBLIC_KEY = "dummy_sender_public_key";
    private final static String RANDOM_STRING = "dummy_string";

    @Autowired
    public StatsServiceIT(StatsService statsService, StatsRepository repository,
                          MessageService messageService) {
        this.statsService = statsService;
        this.repository = repository;
        this.messageService = messageService;
    }

    @Test
    void testProcessStats_shouldBeProcessed() {
        final var inbox = new Inbox(1L, RANDOM_STRING, RANDOM_STRING, Platform.ANDROID, null, null);
        messageService.save(new Message(1L, MESSAGE, SENDER_PUBLIC_KEY, false, MessageType.MESSAGE, inbox));
        messageService.save(new Message(2L, MESSAGE, SENDER_PUBLIC_KEY, false, MessageType.MESSAGE, inbox));
        messageService.save(new Message(3L, MESSAGE, SENDER_PUBLIC_KEY, true, MessageType.MESSAGE, inbox));
        messageService.save(new Message(4L, MESSAGE, SENDER_PUBLIC_KEY, false, MessageType.MESSAGE, inbox));
        messageService.save(new Message(5L, MESSAGE, SENDER_PUBLIC_KEY, true, MessageType.MESSAGE, inbox));

        statsService.processStats();

        final var statsList = repository.findAll();
        assertThat(statsList).hasSize(2);

        final var statsAbsolutSum = repository.findAll().stream().filter(s -> s.getKey().equals(StatsKey.MESSAGES_ABSOLUTE_SUM)).findFirst().get();
        final var statsNotPulledSum = repository.findAll().stream().filter(s -> s.getKey().equals(StatsKey.MESSAGES_NOT_PULLED_SUM)).findFirst().get();
        assertThat(statsAbsolutSum.getValue()).isEqualTo(5);
        assertThat(statsNotPulledSum.getValue()).isEqualTo(3);
    }

    @Test
    void testProcessStats_emptyDatabase_shouldBeProcessed() {
        statsService.processStats();

        final var statsList = repository.findAll();
        assertThat(statsList).hasSize(2);

        final var statsNotPulledSum = repository.findAll().stream().filter(s -> s.getKey().equals(StatsKey.MESSAGES_NOT_PULLED_SUM)).findFirst().get();
        assertThat(statsNotPulledSum.getValue()).isEqualTo(0);
    }
}
