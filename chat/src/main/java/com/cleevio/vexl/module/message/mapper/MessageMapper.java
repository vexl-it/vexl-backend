package com.cleevio.vexl.module.message.mapper;

import com.cleevio.vexl.module.message.dto.response.MessagesResponse;
import com.cleevio.vexl.module.message.entity.Message;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MessageMapper {

    public MessagesResponse.MessageResponse mapSingle(Message message) {
        return new MessagesResponse.MessageResponse(
                message.getId(),
                message.getMessage(),
                message.getSenderPublicKey(),
                message.getType()
        );
    }

    public List<MessagesResponse.MessageResponse> mapList(List<Message> messages) {
        return messages.stream()
                .map(this::mapSingle)
                .collect(Collectors.toList());
    }
}
