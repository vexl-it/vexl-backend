package com.cleevio.vexl.module.group.dto.response;

import com.cleevio.vexl.module.group.entity.Group;
import org.springframework.lang.Nullable;

public record GroupCreatedResponse(

        String uuid,

        String name,

        @Nullable
        String logoUrl,

        String qrCodeUrl,

        long createdAt,

        long expirationAt,

        long closureAt,

        int code

) {

    public GroupCreatedResponse(Group group) {
        this(
                group.getUuid(),
                group.getName(),
                group.getLogoUrl(),
                group.getQrCodeUrl(),
                group.getCreatedAt(),
                group.getExpirationAt(),
                group.getClosureAt(),
                group.getCode()
        );
    }
}
