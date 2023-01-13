package com.cleevio.vexl.common.integration.firebase.dto.request;

public record LinkRequest(

        LinkInfoRequest dynamicLinkInfo

) {

    public LinkRequest(String uri, String link, String iosBundle, String iosStore, String androidBundle) {
        this(
                new LinkInfoRequest(uri, link, iosBundle, iosStore, androidBundle)
        );
    }
}
