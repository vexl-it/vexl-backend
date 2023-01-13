package com.cleevio.vexl.common.convertor;

import com.cleevio.vexl.common.cryptolib.CryptoLibrary;
import com.cleevio.vexl.module.user.config.SecretKeyConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
@RequiredArgsConstructor
public class AesEncryptionConvertor implements AttributeConverter<String, String> {

	private final SecretKeyConfig secretKey;

    @Override
    @Nullable
    public String convertToDatabaseColumn(@Nullable String value) {
        if (value == null) return null;
        return CryptoLibrary.instance.aesEncryptIgnoreTag(secretKey.aesKey(), value);
    }

    @Override
    @Nullable
    public String convertToEntityAttribute(@Nullable String value) {
        if (value == null) return null;
        return CryptoLibrary.instance.aesDecryptIgnoreTag(secretKey.aesKey(), value);
    }
}
