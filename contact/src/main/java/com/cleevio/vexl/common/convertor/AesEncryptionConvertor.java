package com.cleevio.vexl.common.convertor;

import com.cleevio.vexl.common.config.SecretKeyConfig;
import com.cleevio.vexl.common.cryptolib.CryptoLibrary;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
@RequiredArgsConstructor
public class AesEncryptionConvertor implements AttributeConverter<String, String> {

    private final SecretKeyConfig secretKeyConfig;

    @Nullable
    @Override
    public String convertToDatabaseColumn(@Nullable String value) {
        if (value == null) {
            return null;
        }
        return CryptoLibrary.instance.aesEncryptIgnoreTag(secretKeyConfig.aesKey(), value);
    }

    @Nullable
    @Override
    public String convertToEntityAttribute(@Nullable String value) {
        if (value == null) {
            return null;
        }
        return CryptoLibrary.instance.aesDecryptIgnoreTag(secretKeyConfig.aesKey(), value);
    }
}
