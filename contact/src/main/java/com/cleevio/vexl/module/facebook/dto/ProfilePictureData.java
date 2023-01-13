package com.cleevio.vexl.module.facebook.dto;

import com.restfb.Facebook;
import lombok.Data;

@Data
public class ProfilePictureData {

    @Facebook
    private String url;

    @Facebook("is_silhouette")
    private Boolean isSilhouette;
}
