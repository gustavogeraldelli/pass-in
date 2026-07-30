package dev.gustavo.passin.service;

import dev.gustavo.passin.exception.InvalidCheckInTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class CheckInTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String PAYLOAD_SEPARATOR = ":";
    private static final String TOKEN_SEPARATOR = ".";

    private final String secret;
    private final Duration ttl;
    private final Clock clock;

    @Autowired
    public CheckInTokenService(@Value("${app.check-in.token-secret}") String secret,
                               @Value("${app.check-in.token-ttl}") Duration ttl) {
        this(secret, ttl, Clock.systemUTC());
    }

    CheckInTokenService(String secret, Duration ttl, Clock clock) {
        this.secret = secret;
        this.ttl = ttl;
        this.clock = clock;
    }

    public String generateToken(String attendeeId) {
        long expiresAt = Instant.now(clock).plus(ttl).getEpochSecond();
        String payload = attendeeId + PAYLOAD_SEPARATOR + expiresAt;
        String encodedPayload = encode(payload);
        return encodedPayload + TOKEN_SEPARATOR + sign(encodedPayload);
    }

    public String getAttendeeId(String token) {
        String[] tokenParts = token.split("\\.", -1);
        if (tokenParts.length != 2 || tokenParts[0].isBlank() || tokenParts[1].isBlank())
            throw new InvalidCheckInTokenException("Invalid check-in token");

        String encodedPayload = tokenParts[0];
        String signature = tokenParts[1];

        if (!MessageDigest.isEqual(signature.getBytes(StandardCharsets.UTF_8), sign(encodedPayload).getBytes(StandardCharsets.UTF_8)))
            throw new InvalidCheckInTokenException("Invalid check-in token");

        String payload = decode(encodedPayload);
        String[] payloadParts = payload.split(PAYLOAD_SEPARATOR, -1);
        if (payloadParts.length != 2 || payloadParts[0].isBlank() || payloadParts[1].isBlank())
            throw new InvalidCheckInTokenException("Invalid check-in token");

        long expiresAt = parseExpiration(payloadParts[1]);
        if (Instant.now(clock).isAfter(Instant.ofEpochSecond(expiresAt)))
            throw new InvalidCheckInTokenException("Expired check-in token");

        return payloadParts[0];
    }

    private long parseExpiration(String value) {
        try {
            return Long.parseLong(value);
        }
        catch (NumberFormatException exception) {
            throw new InvalidCheckInTokenException("Invalid check-in token");
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        }
        catch (Exception exception) {
            throw new IllegalStateException("Could not sign check-in token", exception);
        }
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        try {
            return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
        }
        catch (IllegalArgumentException exception) {
            throw new InvalidCheckInTokenException("Invalid check-in token");
        }
    }

}
