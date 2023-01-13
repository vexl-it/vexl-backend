package com.cleevio.vexl.module.group.dto.mapper;

import com.cleevio.vexl.module.group.dto.GroupModel;
import com.cleevio.vexl.module.group.dto.request.CreateGroupRequest;
import com.cleevio.vexl.module.group.dto.response.GroupsResponse;
import com.cleevio.vexl.module.group.entity.Group;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GroupMapper {

    public Group mapSingleToGroup(CreateGroupRequest request) {
        return Group.builder()
                .name(request.name())
                .expirationAt(request.expirationAt())
                .closureAt(request.closureAt())
                .build();
    }

    public List<GroupsResponse.GroupResponse> mapGroupModelToGroupResponse(List<GroupModel> groups) {
        return groups.stream()
                .map(this::mapSingleToGroupResponse)
                .toList();
    }

    public GroupsResponse.GroupResponse mapSingleToGroupResponse(GroupModel groupModel) {
        return new GroupsResponse.GroupResponse(groupModel.group(), groupModel.countMembers());
    }

}
