package com.cleevio.vexl.module.contact.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Set;

public record CommonContactsResponse(

        List<Contacts> commonContacts

) {

    public record Contacts(

            @Schema(description = "Public key of your contact.")
            String publicKey,

            @Schema(description = "Common contacts.")
            CommonContacts common

    ) {

        public record CommonContacts(

                @Schema(description = "Hashes of the contacts. Hash is HMAC-SHA256. Can be phone number or facebook id.")
                Set<String> hashes

        ) {
        }

    }


}
