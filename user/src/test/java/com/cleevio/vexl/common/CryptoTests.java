package com.cleevio.vexl.common;

import com.cleevio.vexl.common.cryptolib.CryptoLibrary;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * It is not pretty to test multiple operations in one function but for the sake of developer's sanity let's keep it like this.
 */
public class CryptoTests {
    private final String publicKey1 = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUU0d0VBWUhLb1pJemowQ0FRWUZLNEVFQUNFRE9nQUVObVJGZkdtL1JqbFdxZ2tPYWx6NS9YSXlBWEFSVjdLSQpUbHlBUjBHcVNWODVRK0VjTkZJR2N3K3RXTFE3Vkp0SnZ0OStJa2RMTUl3PQotLS0tLUVORCBQVUJMSUMgS0VZLS0tLS0K";
    private final String privateKey1 = "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1IZ0NBUUF3RUFZSEtvWkl6ajBDQVFZRks0RUVBQ0VFWVRCZkFnRUJCQng5a3MyMmlWMFl2bzRrVndCQW5sQ3YKOUQyZlVzQmdNbHhQUHBiL29Ud0RPZ0FFTm1SRmZHbS9SamxXcWdrT2FsejUvWEl5QVhBUlY3S0lUbHlBUjBHcQpTVjg1UStFY05GSUdjdyt0V0xRN1ZKdEp2dDkrSWtkTE1Jdz0KLS0tLS1FTkQgUFJJVkFURSBLRVktLS0tLQo=";
    private final String publicKey2 = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUU0d0VBWUhLb1pJemowQ0FRWUZLNEVFQUNFRE9nQUVuY2VIZXYydUdzeXowVS9CZFdOY21DYVBOUzhFTGJxawpoUzhDd3lyQnBmUkVLUmZkbVFuVHpWTERpRnByYlY4Tngxay9CQkJleHZBPQotLS0tLUVORCBQVUJMSUMgS0VZLS0tLS0K";
    private final String privateKey2 = "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1IZ0NBUUF3RUFZSEtvWkl6ajBDQVFZRks0RUVBQ0VFWVRCZkFnRUJCQnlGb01ZN3FYa2NFcUtiWEtmSnpHaXEKR0kwRjAwa1FJU0JCaUdWTW9Ud0RPZ0FFbmNlSGV2MnVHc3l6MFUvQmRXTmNtQ2FQTlM4RUxicWtoUzhDd3lyQgpwZlJFS1JmZG1RblR6VkxEaUZwcmJWOE54MWsvQkJCZXh2QT0KLS0tLS1FTkQgUFJJVkFURSBLRVktLS0tLQo=";

    @Test
    void testEcdsaV1() {

        // Test variable lengths (some messages have empty bites at the end).
        // Those strings were generated with old C library vexl-crypto.
        String[] signatures = {
                "MD0CHQCih1jfMMppW3uowseThqY/lyPWpNoup2+WrkSoAhxnjjBHSSCO1QcFtq0+sBKxF0wtRpzKU71ZjQA6AA==",
                "MD0CHQDeQMLEgiXmftcm5XSklmS/KY/MzbSELJB/TiwNAhwOeHzGx4ZkM+Ur7awaQ9Emh+zm7uKFQ4CNIRbsAA==",
                "MD4CHQDWi6+i2YDdZCiG/8YJ3psHHAVx850xk/OCnKKAAh0AkfZAy85jWCf2hj1I0JpRkG2kEqGMlBJrZbZphA==",
                "MDwCHE3QNGys4lPi4S1KtZogCE+cyLr0/n+wZ6mbwVgCHFqMpGf9ugKzI0l32Kf2XkV79mEm3cIKCeWLItEAAA==",
                "MD4CHQCfRwkW8yGfoVDvMpHwWM44LzvHaPAycDbqTVoiAh0An/ueuCrUWh+QfBMZCsV1YgSNALem8HTbOK9D9A==",
                "MD4CHQDPOzN7PayCFX7ESfDNmTVXEc34t5gLkJBvpbEeAh0AhAnKfMTirz3ERJAkd6xBDLk/yCDa4mUcSf/YSQ==",
        };

        var data = "someData";
        for (String signature : signatures) {
            var valid = CryptoLibrary.instance.ecdsaVerifyV1(
                    publicKey1,
                    data,
                    signature
            );
            assertThat(valid).isTrue();
        }

        var data2 = "Another data";
        var signature = CryptoLibrary.instance.ecdsaSignV1(privateKey2, data2);
        assertThat(
                CryptoLibrary.instance.ecdsaVerifyV1(publicKey2, data2, signature)
        ).isTrue();
    }

    @Test
    void testEcdsaV2() {
        var data = "some random data";

        // generated using c library
        String[] signatures = {
                "MDwCHGQhAomPjqoj7jf0j/wuGWHinalumUwZ+2zhv88CHFk6yyg5nSCM4LRWssLZ+JRPt0hwxAXco6r4jpU=",
                "MDwCHFL30g1vZrPVlJ4ry5/UeBsCZkhy3G6xhVn+sDYCHA1QrzY9GHFhM8zlwHSyNPp5C/NcpmVTiVrE4eM=",
                "MD4CHQDC0NKkxiahMtfO3wZXHOBqBi4cc9LZ19KwmORwAh0A80yfM0Ce4Ro1/mY/RT16WX0/z9X5cAm0DfmdZw==",
                "MD4CHQC28K7jpYz2Z9VQSmBXelW7ccMS45Tut0ni7gBTAh0AhLIYYEe23VJVaQFyLY2IKaYI4yLInh+17JmyPg==",
                "MD0CHQDVsmpq0JGszswU0m1sBNMT7C34luU0ISPQMgiqAhx93B0BOBgnKe6pBrFIQmEPWFjUHxuhS6/Ub7Fh",
                "MD0CHFwvtpGPN8jKhdI6r56IwVoklyLcaHbt8BEtE0ICHQDDx6NucHc/wDOu+bRz2/3qfvxfARX06BmalGVT",
                "MD0CHDYMnsO6ENum9KrYbVczl6M5qAeRT3Pqf4cNBqQCHQCejfu3sdXEQanGbTdLu3zdDi/mFjqtJENe7sFV",
                "MD4CHQC/iHM4A796Q3Ulo+0eJL6jMIjGcGPLgo+w+50AAh0A1DUlX2dt0w7G/QbDHSU4Kg1rON/oyhlDrreJOg==",
                "MDwCHDEcmJ449RWPxQMz5O3BcoQb3D261wVR7SQTY9kCHFtXATCV/0mQB0dmwK3TIm7Xy6DWiLIgGkVXY/c=",
        };

        for (String signature: signatures) {
            assertThat(CryptoLibrary.instance.ecdsaVerifyV2(publicKey2, data, signature)).isTrue();
        }

        var data2 = "another asdkfja";
        var signature = CryptoLibrary.instance.ecdsaSignV2(privateKey1, data2);
        assertThat(
                CryptoLibrary.instance.ecdsaVerifyV2(publicKey1, data2, signature)
        ).isTrue();
    }

    @Test
    void testSHA256() {
        String digest = "askldjf klasjdkl fjakls jfklajs kfd";
        String hash = CryptoLibrary.instance.sha256(digest);
        // Generated using C library
        assertThat(hash).isEqualTo("vf2OV4eGYQHe0HkcNeIy+pdo3tq859sncwobDEvXXPY=");
    }

    @Test
    void testHmac() {
        String data = "something here";
        String pass = "something else";
        String mac = CryptoLibrary.instance.hmacDigest(pass, data);
        assertThat(mac).isEqualTo("elk+GV5rnzaXk4v8pG0vDVm8Un6ARKrS2bakmy6QMiA=");
        assertThat(CryptoLibrary.instance.hmacVerify(pass, data, mac)).isTrue();
    }

    @Test
    void testAesIgnoreTag() {
        String data = "asdfasdf this must be longer than long yes, sesakejaf lksdjlf kajs dlfkjalksd jflkasj dflkajskdlůfj aslkdjf klaůsjdfkl ajsdlkfj aklůsjdfk aůsjdf aklsdjf klajs dklfa";
        String pass="asdfasdfa else aklsdj fklajskldfj klasjd klfj alksd jfklajs kldfj aklsjdflkajs kldfj alksjdf lkajslk djfklajsd kfkas jdfklaj skldfjakls jdflkaj slkdfjasl kdfjlkasjdf lkajsdf";

        String cipher = CryptoLibrary.instance.aesEncryptIgnoreTag(pass, data);
        assertThat(CryptoLibrary.instance.aesDecryptIgnoreTag(pass, cipher)).isEqualTo(data);
    }
}
