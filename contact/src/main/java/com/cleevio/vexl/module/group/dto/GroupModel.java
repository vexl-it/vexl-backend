package com.cleevio.vexl.module.group.dto;

import com.cleevio.vexl.module.group.entity.Group;

public record GroupModel(

        Group group,

        int countMembers

) {
}
