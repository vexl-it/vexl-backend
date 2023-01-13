package com.cleevio.vexl.module.facebook.dto.response;

import com.cleevio.vexl.module.facebook.dto.FacebookUser;
import com.cleevio.vexl.module.facebook.dto.NewFacebookFriends;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class FacebookContactResponse {

    private FacebookUser facebookUser;

    public FacebookContactResponse(NewFacebookFriends newConnections) {
        this.facebookUser = newConnections.facebookUser();
        this.facebookUser.setFriends(newConnections.newFriends());
    }
}
