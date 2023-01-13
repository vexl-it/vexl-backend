package com.cleevio.vexl.module.group.dto;

import java.util.List;

public record MemberModel(

        String groupUuid,

        List<String> newPublicKeys,

        List<String> removedPublicKeys

) {
}
