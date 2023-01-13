package com.cleevio.vexl.common.convertor;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import static com.cleevio.vexl.common.service.CryptoService.createSha256Hash;

@Slf4j
@Converter
public class Sha256HashConvertor implements AttributeConverter<String, String> {

    /**
     * Public key is sha256_hash in base64. Base64 is 6 bits per character (2^6 = 64). And that obviously ends as 44 due to padding.
     * So if it's an already created entity, we don't want to create hash256 from hash256 again.
     */
    private static final int LENGTH = 44;

    @Override
    public String convertToDatabaseColumn(String publicKey) {
        log.debug("Converting publicKey to database {}", publicKey);

        if (publicKey.length() == LENGTH) {
            log.debug("No need to convert");
            return publicKey;
        }

        String sha256Hash = createSha256Hash(publicKey);
        log.debug("Public key [{}] has been converted to [{}]", publicKey, sha256Hash);
        return sha256Hash;
    }

    @Override
    public String convertToEntityAttribute(String publicKey) {
        return publicKey;
    }
}
