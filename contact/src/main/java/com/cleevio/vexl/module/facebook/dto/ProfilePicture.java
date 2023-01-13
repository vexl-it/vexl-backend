package com.cleevio.vexl.module.facebook.dto;

import com.restfb.Facebook;
import lombok.Data;

@Data
public class ProfilePicture {

    @Facebook("data")
    private ProfilePictureData data;
}
