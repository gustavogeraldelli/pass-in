package dev.gustavo.passin.service.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gustavo.passin.security.OrganizerPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AccessTokenService {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final byte[] secret;
    private final Duration accessTokenTtl;

    public AccessTokenService(Clock clock,
                              @Value("${app.auth.jwt-secret}") String secret,
                              @Value("${app.auth.access-token-ttl}") Duration accessTokenTtl) {
        this.objectMapper = new ObjectMapper();
        this.clock = clock;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.accessTokenTtl = accessTokenTtl;
    }

    public String generate(OrganizerPrincipal organizer) {
        Instant now = Instant.now(clock);
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", organizer.getId());
        payload.put("email", organizer.getUsername());
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", now.plus(accessTokenTtl).getEpochSecond());

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(payload);
        String signature = sign(encodedHeader + "." + encodedPayload);

        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    public String getSubject(String token) {
        Map<String, Object> payload = parseAndValidate(token);
        return (String) payload.get("sub");
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    private Map<String, Object> parseAndValidate(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3)
            throw new BadCredentialsException("Invalid access token");

        String expectedSignature = sign(parts[0] + "." + parts[1]);
        if (!constantTimeEquals(expectedSignature, parts[2]))
            throw new BadCredentialsException("Invalid access token");

        Map<String, Object> payload = decodeJson(parts[1]);
        Number expiresAt = (Number) payload.get("exp");
        if (expiresAt == null || Instant.now(clock).getEpochSecond() >= expiresAt.longValue())
            throw new BadCredentialsException("Expired access token");

        if (!(payload.get("sub") instanceof String))
            throw new BadCredentialsException("Invalid access token");

        return payload;
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        }
        catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not encode token", exception);
        }
    }

    private Map<String, Object> decodeJson(String value) {
        try {
            byte[] decoded = BASE64_URL_DECODER.decode(value);
            return objectMapper.readValue(decoded, new TypeReference<>() {
            });
        }
        catch (IllegalArgumentException | IOException exception) {
            throw new BadCredentialsException("Invalid access token");
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret, HMAC_SHA256));
            byte[] signature = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return BASE64_URL_ENCODER.encodeToString(signature);
        }
        catch (Exception exception) {
            throw new IllegalStateException("Could not sign token", exception);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = actual.getBytes(StandardCharsets.UTF_8);
        return java.security.MessageDigest.isEqual(expectedBytes, actualBytes);
    }
}
