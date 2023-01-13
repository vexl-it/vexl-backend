package com.cleevio.vexl.module.challenge.mapper;

import com.cleevio.vexl.module.challenge.dto.response.ChallengesBatchCreatedResponse;
import com.cleevio.vexl.module.challenge.entity.Challenge;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChallengeMapper {

    public ChallengesBatchCreatedResponse.ChallengePublicKeyResponse mapSingle(Challenge challenge) {
        return new ChallengesBatchCreatedResponse.ChallengePublicKeyResponse(
                challenge.getPublicKey(),
                challenge.getChallenge()
        );
    }

    public List<ChallengesBatchCreatedResponse.ChallengePublicKeyResponse> mapList(List<Challenge> messages) {
        return messages.stream()
                .map(this::mapSingle)
                .collect(Collectors.toList());
    }
}
