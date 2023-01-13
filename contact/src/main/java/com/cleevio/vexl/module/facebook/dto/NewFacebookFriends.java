package com.cleevio.vexl.module.facebook.dto;

import java.util.List;

public record NewFacebookFriends(

        FacebookUser facebookUser,

        List<FacebookUser> newFriends

) {
}
