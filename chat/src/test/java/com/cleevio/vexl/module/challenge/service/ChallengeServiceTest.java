package com.cleevio.vexl.module.challenge.service;

import com.cleevio.vexl.common.service.AdvisoryLockService;
import com.cleevio.vexl.module.challenge.config.ChallengeConfig;
import com.cleevio.vexl.utils.CryptographyTestKeysUtil;
import com.cleevio.vexl.utils.RequestCreatorTestUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ChallengeServiceTest {

    private final ChallengeRepository challengeRepository = mock(ChallengeRepository.class);
    private final AdvisoryLockService advisoryLockService = mock(AdvisoryLockService.class);
    private final ChallengeConfig config = new ChallengeConfig(30);

    private final ChallengeService challengeService = new ChallengeService(
            challengeRepository,
            advisoryLockService,
            config
    );

    @Test
    void createChallenge_shouldBeCreated() {
        String challenge = challengeService.createChallenge(RequestCreatorTestUtil.createChallengeRequest(CryptographyTestKeysUtil.PUBLIC_KEY_USER_A));
        assertThat(challenge).isNotBlank();
    }
}
