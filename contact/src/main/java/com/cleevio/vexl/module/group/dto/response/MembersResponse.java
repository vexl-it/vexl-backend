package com.cleevio.vexl.module.group.dto.response;

import com.cleevio.vexl.module.group.dto.MemberModel;

import java.util.List;

public record MembersResponse(

        List<MemberModel> newMembers

) {
}
