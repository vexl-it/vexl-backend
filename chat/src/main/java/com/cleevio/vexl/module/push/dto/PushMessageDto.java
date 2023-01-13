package com.cleevio.vexl.module.push.dto;

import com.cleevio.vexl.module.message.constant.MessageType;
import com.cleevio.vexl.module.inbox.constant.Platform;

public record PushMessageDto (

        String title,
        String text,
        String token,
        Platform platform,
		MessageType messageType,
        String receiverPublicKey,
        String senderPublicKey

) {}
