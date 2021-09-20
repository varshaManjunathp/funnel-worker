package com.pharmeasy.funnel.config.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTTokenUtil {

    private static final String BEARER = "Bearer";
    @Value("${app.secret.key}")
    private String secretKey;

    private static String parseToken(String header) {
        int start = header.indexOf(BEARER);
        if (start < 0) {
            return header;
        } else {
            return header.substring(start + BEARER.length() + 1);
        }
    }

    public DecodedJWT verifyToken(String token) {
        Algorithm algorithm = Algorithm.HMAC512(secretKey);
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(parseToken(token));

    }
}
