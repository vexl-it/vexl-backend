package com.cleevio.vexl.module.push.dto;

import com.cleevio.vexl.module.inbox.constant.Platform;
import com.cleevio.vexl.module.message.constant.MessageType;
import org.springframework.lang.Nullable;

/**
 * If title or text is null, no notification's contents will be sent. This means that if you want to send only data, you must set title and/or text to null.
 *
 * @param title
 * @param text
 * @param token
 * @param platform
 * @param messageType
 * @param receiverPublicKey
 * @param senderPublicKey
 */
public record PushMessageDto (
        String token,
        Platform platform,
		int clientVersion,
		String messageType,
        String receiverPublicKey,
        String senderPublicKey,
		@Nullable
		String messagePreview
) {}
