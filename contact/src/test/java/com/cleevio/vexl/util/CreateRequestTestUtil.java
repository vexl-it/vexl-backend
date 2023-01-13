package com.cleevio.vexl.util;

import com.cleevio.vexl.module.contact.dto.request.CommonContactsRequest;
import com.cleevio.vexl.module.contact.dto.request.DeleteContactsRequest;
import com.cleevio.vexl.module.contact.dto.request.ImportRequest;
import com.cleevio.vexl.module.group.dto.request.CreateGroupRequest;
import com.cleevio.vexl.module.group.dto.request.JoinGroupRequest;
import com.cleevio.vexl.module.group.dto.request.MemberRequest;
import com.cleevio.vexl.module.user.dto.request.CreateUserRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateRequestTestUtil {

    public static CreateGroupRequest createCreateGroupRequest() {
        return new CreateGroupRequest(
                "dummy_name",
                null,
                9223372036854777L,
                9223372036854777L
        );
    }

    public static CreateGroupRequest createCreateGroupRequestExpired() {
        return new CreateGroupRequest(
                "dummy_name",
                null,
                65465465,
                654654648
        );
    }

    public static JoinGroupRequest createJoinGroupRequest(int groupUuid) {
        return new JoinGroupRequest(
                groupUuid
        );
    }

    public static ImportRequest createImportRequest(List<String> contacts) {
        return new ImportRequest(
                contacts
        );
    }

    public static CommonContactsRequest createCommonContactsRequest(Set<String> contacts) {
        return new CommonContactsRequest(
                contacts
        );
    }

    public static List<MemberRequest.GroupRequest> createGroupRequestList(String groupUuid, List<String> publicKeys) {
        return List.of(new MemberRequest.GroupRequest(
                        groupUuid,
                        publicKeys
                )
        );
    }

    public static MemberRequest.GroupRequest createGroupRequest(String groupUuid, List<String> publicKeys) {
        return new MemberRequest.GroupRequest(
                groupUuid,
                publicKeys
        );
    }

    public static DeleteContactsRequest createDeleteContactsRequest(List<String> contactsToDelete) {
        return new DeleteContactsRequest(
                contactsToDelete
        );
    }

    public static CreateUserRequest createCreateUserRequest(String firebaseToken) {
        return new CreateUserRequest(
                firebaseToken
        );
    }

}
