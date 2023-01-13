package com.cleevio.vexl.module.facebook.dto;

import com.restfb.Facebook;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

@Data
public class FacebookUser {

    @Facebook
    private String id;

    @Facebook
    private String name;

    @Facebook("picture")
    private ProfilePicture profilePicture;

    @Facebook("friends")
    private List<FacebookUser> friends = new ArrayList<>();

    public List<FacebookUser> getFriends() {
        return unmodifiableList(friends);
    }

}
