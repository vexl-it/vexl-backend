package com.cleevio.vexl.module.stats.service;

import com.cleevio.vexl.common.IntegrationTest;
import com.cleevio.vexl.module.contact.service.ImportService;
import com.cleevio.vexl.module.stats.constant.StatsKey;
import com.cleevio.vexl.module.user.service.UserService;
import com.cleevio.vexl.util.CreateRequestTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StatsServiceIT {

    private final StatsService statsService;
    private final StatsRepository repository;
    private final ImportService importService;
    private final UserService userService;

    private static final String PUBLIC_KEY_1 = "dummy_public_key_1";
    private static final String HASH_1 = "hash_public_key_1";
    private static final String PUBLIC_KEY_2 = "dummy_public_key_2";
    private static final String HASH_2 = "hash_public_key_2";
    private static final List<String> contacts = List.of("contact1", "contact2", "contact3", "contact4", "contact5", "contact6", "contact7");

    @Autowired
    public StatsServiceIT(StatsService statsService, StatsRepository repository,
                          UserService messageService, ImportService importService) {
        this.statsService = statsService;
        this.repository = repository;
        this.importService = importService;
        this.userService = messageService;
    }

    @Test
    void testProcessStats_shouldBeProcessed() {
        final var user = userService.createUser(PUBLIC_KEY_1, HASH_1);
        userService.createUser(PUBLIC_KEY_2, HASH_2);

        importService.importContacts(user, CreateRequestTestUtil.createImportRequest(contacts));

        statsService.processStats();

        final var statsList = repository.findAll();
        assertThat(statsList).hasSize(3);

        final var activeUsersCount = repository.findAll().stream().filter(s -> s.getKey().equals(StatsKey.ACTIVE_USERS_COUNT)).findFirst().get();
        final var allTimeUsersCount = repository.findAll().stream().filter(s -> s.getKey().equals(StatsKey.ALL_TIME_USERS_COUNT)).findFirst().get();
        final var contactsCount = repository.findAll().stream().filter(s -> s.getKey().equals(StatsKey.CONTACTS_COUNT)).findFirst().get();
        assertThat(activeUsersCount.getValue()).isEqualTo(2);
        assertThat(allTimeUsersCount.getValue()).isEqualTo(2);
        assertThat(contactsCount.getValue()).isEqualTo(7);
    }

    @Test
    void testProcessStats_emptyDatabase_shouldBeProcessed() {
        statsService.processStats();

        final var statsList = repository.findAll();
        assertThat(statsList).hasSize(3);

        final var activeUsersCount = repository.findAll().stream().filter(s -> s.getKey().equals(StatsKey.ACTIVE_USERS_COUNT)).findFirst().get();
        final var contactsCount = repository.findAll().stream().filter(s -> s.getKey().equals(StatsKey.CONTACTS_COUNT)).findFirst().get();
        assertThat(activeUsersCount.getValue()).isEqualTo(0);
        assertThat(contactsCount.getValue()).isEqualTo(0);
    }
}
