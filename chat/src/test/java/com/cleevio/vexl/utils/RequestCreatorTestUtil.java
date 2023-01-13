package com.cleevio.vexl.utils;

import com.cleevio.vexl.module.challenge.dto.request.CreateChallengeRequest;
import com.cleevio.vexl.module.inbox.dto.SignedChallenge;
import com.cleevio.vexl.module.inbox.dto.request.CreateInboxRequest;
import com.cleevio.vexl.module.message.constant.MessageType;
import com.cleevio.vexl.module.message.dto.request.SendMessageBatchRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestCreatorTestUtil {

    public final static String MESSAGE_TO_SENDER_1 = "dummy_message_to_sender_1";
    public final static String MESSAGE_TO_SENDER_2 = "dummy_message_to_sender_2";

    public static CreateInboxRequest createInboxRequest(String publicKey) {
        return new CreateInboxRequest(
                publicKey,
                null,
                new SignedChallenge("dummy_challenge", "dummy_signature")
        );
    }

    public static CreateChallengeRequest createChallengeRequest(String publicKey) {
        return new CreateChallengeRequest(publicKey);
    }

    public static SendMessageBatchRequest createSendMessageBatchRequest(String senderPublicKey1, String receiverPublicKey1,
                                                                        SignedChallenge signedChallenge1, String senderPublicKey2,
                                                                        String receiverPublicKey2, SignedChallenge signedChallenge2) {
        List<SendMessageBatchRequest.BatchData> batchData = createBatchData(senderPublicKey1, senderPublicKey2, receiverPublicKey1, receiverPublicKey2, signedChallenge1, signedChallenge2);
        return new SendMessageBatchRequest(
                batchData
        );
    }

    private static List<SendMessageBatchRequest.BatchData> createBatchData(String senderPublicKey1, String senderPublicKey2,
                                                                           String receiverPublicKey1, String receiverPublicKey2,
                                                                           SignedChallenge signedChallenge1, SignedChallenge signedChallenge2) {
        return List.of(
                new SendMessageBatchRequest.BatchData(
                        senderPublicKey1,
                        createMessageRequest(receiverPublicKey1, MESSAGE_TO_SENDER_1),
                        signedChallenge1

                ),
                new SendMessageBatchRequest.BatchData(
                        senderPublicKey2,
                        createMessageRequest(receiverPublicKey2, MESSAGE_TO_SENDER_2),
                        signedChallenge2

                )
        );
    }

    private static List<SendMessageBatchRequest.BatchData.Messages> createMessageRequest(String receiverPublicKey, String message) {
        return List.of(new SendMessageBatchRequest.BatchData.Messages(receiverPublicKey, message, MessageType.MESSAGE));
    }
}
