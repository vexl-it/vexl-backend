package com.cleevio.vexl.common.integration.firebase.dto.request;

public record LinkInfoRequest(

        String domainUriPrefix,
        String link,
        LinkIosRequest iosInfo,
        LinkAndroidRequest androidInfo

) {

    public LinkInfoRequest(String uri, String link, String iosBundle, String iosStore, String androidBundle) {
        this(
                uri,
                link,
                new LinkIosRequest(iosBundle, iosStore),
                new LinkAndroidRequest(androidBundle)
        );
    }
}
