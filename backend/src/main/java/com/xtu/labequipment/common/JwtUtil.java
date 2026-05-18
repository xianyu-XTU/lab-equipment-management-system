package com.xtu.labequipment.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Component
public class JwtUtil {
    @Value("${app.jwt-secret}")
    private String secret;
    @Value("${app.token-hours:24}")
    private long tokenHours;

    public String createToken(Long userId, String username, String roleCode) {
        long exp = Instant.now().getEpochSecond() + tokenHours * 3600;
        String payload = userId + ":" + username + ":" + roleCode + ":" + exp;
        String body = b64(payload);
        return body + "." + sign(body);
    }

    public TokenUser parse(String token) {
        if (token == null || !token.contains(".")) throw new BusinessException("invalid token");
        String[] arr = token.split("\\.", 2);
        if (!sign(arr[0]).equals(arr[1])) throw new BusinessException("invalid token signature");
        String payload = new String(Base64.getUrlDecoder().decode(arr[0]), StandardCharsets.UTF_8);
        String[] p = payload.split(":", 4);
        if (p.length != 4) throw new BusinessException("invalid token payload");
        long exp = Long.parseLong(p[3]);
        if (Instant.now().getEpochSecond() > exp) throw new BusinessException("token expired");
        return new TokenUser(Long.parseLong(p[0]), p[1], p[2]);
    }

    private String b64(String s) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
