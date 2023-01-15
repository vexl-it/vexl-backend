package com.cleevio.vexl.common.service;

import it.vexl.common.crypto.CryptoLibrary;
import org.springframework.stereotype.Service;

@Service
public class CryptoService {

    public static String createSha256Hash(String value) {
        return CryptoLibrary.instance.sha256(value);
    }
}
