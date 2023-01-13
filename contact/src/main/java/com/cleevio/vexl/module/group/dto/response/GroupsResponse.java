package com.cleevio.vexl.module.group.dto.response;

import com.cleevio.vexl.module.group.entity.Group;
import org.springframework.lang.Nullable;

import java.util.List;

public record GroupsResponse(

        List<GroupResponse> groupResponse

) {

    public record GroupResponse(

            String uuid,

            String name,

            @Nullable
            String logoUrl,

            String qrCodeUrl,

            long createdAt,

            long expirationAt,

            long closureAt,

            int code,

            int memberCount

    ) {

        public GroupResponse(Group group, int memberCount) {
            this(
                    group.getUuid(),
                    group.getName(),
                    group.getLogoUrl(),
                    group.getQrCodeUrl(),
                    group.getCreatedAt(),
                    group.getExpirationAt(),
                    group.getClosureAt(),
                    group.getCode(),
                    memberCount
            );
        }
    }
}
