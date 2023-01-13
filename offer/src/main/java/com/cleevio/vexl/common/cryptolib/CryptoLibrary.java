package com.cleevio.vexl.common.cryptolib;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class CryptoLibrary {
    public static final CryptoLibrary instance;
    private static final byte[] PBKD_SECRET = "vexlvexl".getBytes(StandardCharsets.UTF_8);
    private static final int PBKD_ITERATIONS = 2000;

    static {
        try {
            instance = new CryptoLibrary();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Error initializing crypto library", e);
        }
    }

    private final KeyFactory keyFactory;
    private final SecretKeyFactory keyFactoryPbkdf2WithHmacSHA256;
    private final SecretKeyFactory keyFactoryPbkdf2WithHmacSHA1;

    public CryptoLibrary() throws NoSuchAlgorithmException, NoSuchProviderException {
        Security.addProvider(new BouncyCastleProvider());
        keyFactory = KeyFactory.getInstance("EC", "BC");
        keyFactoryPbkdf2WithHmacSHA256 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256", "BC");
        keyFactoryPbkdf2WithHmacSHA1 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1", "BC");
    }

    private PublicKey base64ToPublicKey(String base64Key) throws InvalidKeySpecException {
        byte[] keyBytes = Base64
                .getDecoder()
                .decode(
                        new String(Base64.getDecoder().decode(base64Key))
                                .replaceAll("-----(BEGIN|END).*", "")
                                .replaceAll("\n", "")
                );

        return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
    }

    private PrivateKey base64ToPrivateKey(String base64Key) throws InvalidKeySpecException {
        byte[] keyBytes = Base64
                .getDecoder()
                .decode(
                        new String(Base64.getDecoder().decode(base64Key))
                                .replaceAll("-----(BEGIN|END).*", "")
                                .replaceAll("\n", "")
                );

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return keyFactory.generatePrivate(spec);
    }

    public boolean ecdsaVerifyV2(String base64PublicKey, String data, String base64Signature) {
        try {
            PublicKey pubKey = base64ToPublicKey(base64PublicKey);
            Signature ecdsaSign = Signature
                    .getInstance("SHA256withECDSA", "BC");
            ecdsaSign.initVerify(pubKey);
            ecdsaSign.update(data.getBytes(StandardCharsets.UTF_8));
            return ecdsaSign.verify(Base64.getDecoder().decode(base64Signature));
        } catch (Exception e) {
           return false;
        }
    }

    public String ecdsaSignV2(String base64PrivateKey, String data) {
        try {
            PrivateKey privateKey = base64ToPrivateKey(base64PrivateKey);
            Signature ecdsaSign = Signature
                    .getInstance("SHA256withECDSA", "BC");
            ecdsaSign.initSign(privateKey);
            ecdsaSign.update(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ecdsaSign.sign());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating ecdsa signature", e);
        }
    }


    public boolean ecdsaVerifyV1(String base64PublicKey, String data, String base64Signature) {
        try {
            PublicKey pubKey = base64ToPublicKey(base64PublicKey);
            Signature ecdsaSign = Signature
                    .getInstance("NoneWithECDSA", "BC");

            byte[] signatureBytes = Base64.getDecoder().decode(base64Signature);
            byte[] dataHash = sha256(data).getBytes(StandardCharsets.UTF_8);

            // In old C library the length was always 64 bites. But ecdsa pem signature length varies between 64 - 62.
            // This is not pretty but it works. Will move clients to ecdsaV2 functions.
            return ecdsaVerifyV1(pubKey, ecdsaSign, signatureBytes, dataHash) ||
                    ecdsaVerifyV1(pubKey, ecdsaSign, Arrays.copyOfRange(signatureBytes, 0, signatureBytes.length-1), dataHash) ||
                    ecdsaVerifyV1(pubKey, ecdsaSign, Arrays.copyOfRange(signatureBytes, 0, signatureBytes.length-2), dataHash);
        } catch (Exception e) {
            return false;
        }
    }
    private boolean ecdsaVerifyV1(PublicKey pubKey, Signature ecdsaSign, byte[] signatureBytes, byte[] dataHash) {
        try {
            ecdsaSign.initVerify(pubKey);
            ecdsaSign.update(dataHash);

            return ecdsaSign.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }

    public String ecdsaSignV1(String base64PrivateKey, String data) {
        try {
            PrivateKey privKey = base64ToPrivateKey(base64PrivateKey);
            Signature ecdsaSign = Signature
                    .getInstance("NoneWithECDSA", "BC");
            ecdsaSign.initSign(privKey);

            ecdsaSign.update(sha256(data).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(ecdsaSign.sign());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating ECDSA signature", e);
        }
    }

    public String sha256(String digest) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(digest.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating hash", e);
        }
    }

    public String hmacDigest(String password, String message){
        try {
            byte[] stretched = pbkd2fSha256(password, 108);
            byte[] sub = Arrays.copyOfRange(stretched, 44, 108);

            HMac hmac = new HMac(new SHA256Digest());
            hmac.init(new KeyParameter(sub));

            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            byte[] result = new byte[hmac.getMacSize()];
            hmac.update(messageBytes, 0, messageBytes.length);
            hmac.doFinal(result, 0);

            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating hmac", e);
        }
    }

    public boolean hmacVerify(String password, String message, String signature) {
        try {
            String hmac = hmacDigest(password, message);
            return hmac.equals(signature);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error verifying hmac", e);
        }
    }

    public String aesEncryptIgnoreTag(String password, String message) {
        try {
            byte[] stretchedPass = pbkd2fSha1(password, 32 + 12);

            byte[] cipherKey = Arrays.copyOfRange(stretchedPass, 0, 32);
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2};
            System.arraycopy(stretchedPass, 32, iv, 0, 12);

            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(cipherKey, "AES"), new IvParameterSpec(iv), new SecureRandom());
            var encrypted = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error AES encrypting message", e);
        }
    }

    public String aesDecryptIgnoreTag(String password, String encrypted) {
        try {
            byte[] stretchedPass = pbkd2fSha1(password, 32 + 12);

            byte[] cipherKey = Arrays.copyOfRange(stretchedPass, 0, 32);
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2};
            System.arraycopy(stretchedPass, 32, iv, 0, 12);

            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding", "BC");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(cipherKey, "AES"), new IvParameterSpec(iv), new SecureRandom());
            return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error AES decrypting message", e);
        }
    }

    private byte[] pbkd2fSha1(String data, int lengthBytes) throws InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(data.toCharArray(), PBKD_SECRET, PBKD_ITERATIONS, lengthBytes * 8);
        return keyFactoryPbkdf2WithHmacSHA1.generateSecret(spec).getEncoded();
    }

    private byte[] pbkd2fSha256(String data, int lengthBytes) throws InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(data.toCharArray(), PBKD_SECRET, PBKD_ITERATIONS, lengthBytes * 8);
        return keyFactoryPbkdf2WithHmacSHA256.generateSecret(spec).getEncoded();
    }
}
