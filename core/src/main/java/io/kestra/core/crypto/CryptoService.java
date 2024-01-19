package io.kestra.core.crypto;

import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

import javax.annotation.PostConstruct;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

/**
 * Service for encryption and decryption of secrets.
 * It will not work if the configuration 'kestra.crypto.secret-key' is not set.
 */
@Singleton
public class CryptoService {
    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";

    @Value("${kestra.crypto.secret-key}")
    private Optional<String> secretKey;

    private SecretKey key;
    private GCMParameterSpec ivParameter;

    @PostConstruct
    void loadPublicKey() {
        secretKey.ifPresent(s -> {
            this.key = new SecretKeySpec(s.getBytes(), "AES");
            this.ivParameter = generateIv();
        });
    }

    /**
     * Encrypt a String using the AES/GCM/NoPadding algorithm and the ${kestra.crypto.secret-key} key
     */
    public String encrypt(String input) throws GeneralSecurityException {
        ensureConfiguration();

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivParameter);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }

    /**
     * Decrypt a String using the AES/GCM/NoPadding algorithm and the ${kestra.crypto.secret-key} key
     */
    public String decrypt(String cipherText) throws GeneralSecurityException {
        ensureConfiguration();

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, ivParameter);
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(plainText);
    }

    private void ensureConfiguration() {
        if (secretKey.isEmpty()) {
            throw new IllegalArgumentException("You must configure a base64 encoded AES 256 bit secret key in the 'kestra.crypto.secret-key' " +
                "configuration property to be able to use encryption and decryption facilities" );
        }
    }

    private GCMParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new GCMParameterSpec(128, iv);
    }
}
