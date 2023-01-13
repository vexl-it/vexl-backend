package com.cleevio.vexl.module.group.controller;

import com.cleevio.vexl.common.security.filter.SecurityFilter;
import com.cleevio.vexl.module.group.dto.GroupModel;
import com.cleevio.vexl.module.group.dto.mapper.GroupMapper;
import com.cleevio.vexl.module.group.dto.request.CreateGroupRequest;
import com.cleevio.vexl.module.group.dto.request.ExpiredGroupsRequest;
import com.cleevio.vexl.module.group.dto.request.JoinGroupRequest;
import com.cleevio.vexl.module.group.dto.request.LeaveGroupRequest;
import com.cleevio.vexl.module.group.dto.request.MemberRequest;
import com.cleevio.vexl.module.group.dto.response.GroupCreatedResponse;
import com.cleevio.vexl.module.group.dto.response.GroupsResponse;
import com.cleevio.vexl.module.group.dto.response.MembersResponse;
import com.cleevio.vexl.module.group.service.GroupService;
import com.cleevio.vexl.module.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Group")
@RestController
@RequestMapping(value = "/api/v1/groups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_USER')")
public class GroupController {

    private final GroupService groupService;
    private final GroupMapper groupMapper;

    @PostMapping
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new group",
            description = "Each user can create a new group."
    )
    GroupCreatedResponse createGroup(@AuthenticationPrincipal User user,
                                     @RequestBody CreateGroupRequest request) {
        return new GroupCreatedResponse(this.groupService.createGroup(user, request));
    }

    @PostMapping("/join")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Join to a group",
            description = "For joining to a group, you need QR code of a group."
    )
    void joinGroup(@AuthenticationPrincipal User user,
                   @RequestBody JoinGroupRequest request) {
        this.groupService.joinGroup(user, request);
    }

    @PostMapping("/members")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get group members.",
            description = """
                    EP returns group members. It is a POST because of needed payload.
                    You should have public keys of users you already know. Send them within this request.
                    BE will return diff - that means BE will return public keys you do not have and public keys which are no longer in the group.
                    If you want to find all members, you can send only group uuids.
                    """
    )
    MembersResponse retrieveMembers(@AuthenticationPrincipal User user,
                                    @RequestBody MemberRequest request) {
        return new MembersResponse(this.groupService.retrieveMembers(request.groups(), user));
    }

    @GetMapping("/me")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Find my groups.",
            description = "EP returns the groups the user is in."
    )
    GroupsResponse retrieveMyGroups(@AuthenticationPrincipal User user) {
        return new GroupsResponse(
                groupMapper.mapGroupModelToGroupResponse(
                        this.groupService.retrieveMyGroups(user)
                )
        );
    }

    @GetMapping
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get group by code.",
            description = "Put group code you're interested in into request params."
    )
    GroupsResponse.GroupResponse retrieveGroupsByCode(@RequestParam int code) {
        final GroupModel groupModel = this.groupService.retrieveGroupByCode(code);
        return new GroupsResponse.GroupResponse(
                groupModel.group(),
                groupModel.countMembers()
        );
    }

    @PostMapping("/expired")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get expired groups.",
            description = "Put group UUID hashes you know about into request params and EP will return which of them are expired."
    )
    GroupsResponse retrieveExpiredGroups(@RequestBody ExpiredGroupsRequest request) {
        return new GroupsResponse(
                groupMapper.mapGroupModelToGroupResponse(
                        this.groupService.retrieveExpiredGroups(request)
                )
        );
    }

    @PutMapping("/leave")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Leave group.",
            description = "If user want to leave group, send hash 256 of group uuid in payload."
    )
    void leaveGroup(@AuthenticationPrincipal User user,
                    @RequestBody LeaveGroupRequest request) {
        this.groupService.leaveGroup(user, request);
    }

}
