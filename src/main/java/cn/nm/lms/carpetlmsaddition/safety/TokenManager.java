/*
 * Copyright (C) 2025  Carpet-LMS-Addition contributors
 * https://github.com/Citrus-Union/Carpet-LMS-Addition

 * Carpet LMS Addition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.

 * Carpet LMS Addition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Carpet LMS Addition.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.nm.lms.carpetlmsaddition.safety;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public class TokenManager {
    public static final String TOKEN_INVALID_MESSAGE = "Invalid token";
    public static final String TOKEN_EXPIRED_MESSAGE = "Token expired";
    private static final int KEY_LENGTH = 32;

    private static long dayToMs(int day) throws IllegalArgumentException {
        if (day <= 0) {
            return 5000L;
        }
        return (long)day * 24 * 60 * 60 * 1000L;
    }

    public static String generateToken(Path path, String username, int expireDay) throws RuntimeException {
        try {
            long now = System.currentTimeMillis();

            long expireMs = dayToMs(expireDay);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject(username).issueTime(new Date(now))
                .expirationTime(new Date(now + expireMs)).build();

            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

            byte[] secret = getOrCreateSecret(path);

            jwt.sign(new MACSigner(secret));
            return jwt.serialize();
        } catch (IOException e) {
            throw new RuntimeException("io");
        } catch (JOSEException e) {
            throw new RuntimeException("jose");
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("run");
        }
    }

    public static String verifyToken(Path path, String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);

            byte[] secret = getOrCreateSecret(path);

            boolean valid = jwt.verify(new MACVerifier(secret));
            if (!valid) {
                throw new RuntimeException(TOKEN_INVALID_MESSAGE);
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();

            if (claims.getExpirationTime().before(new Date())) {
                throw new RuntimeException(TOKEN_EXPIRED_MESSAGE);
            }

            String tokenUsername = jwt.getJWTClaimsSet().getSubject();
            if (tokenUsername == null || tokenUsername.isBlank()) {
                throw new RuntimeException(TOKEN_INVALID_MESSAGE);
            }

            return tokenUsername;
        } catch (IOException e) {
            throw new RuntimeException("io");
        } catch (ParseException e) {
            throw new RuntimeException("parse error");
        } catch (JOSEException e) {
            throw new RuntimeException("jose");
        }
    }

    private static byte[] getOrCreateSecret(Path path) throws IOException {
        if (Files.exists(path)) {
            String secret = Files.readString(path).trim();
            if (!secret.isBlank()) {
                try {
                    byte[] secretBytes = Base64.getDecoder().decode(secret);
                    if (secretBytes.length == KEY_LENGTH) {
                        return secretBytes;
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        byte[] newSecret = generateSecret();

        String base64 = Base64.getEncoder().encodeToString(newSecret);

        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(path, base64);
        return newSecret;
    }

    private static byte[] generateSecret() {
        byte[] bytes = new byte[KEY_LENGTH];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}
