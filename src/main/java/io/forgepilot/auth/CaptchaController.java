package io.forgepilot.auth;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/captcha")
public class CaptchaController {
    static final String ANSWER = "FORGEPILOT_CAPTCHA_ANSWER";
    static final String VERIFIED = "FORGEPILOT_CAPTCHA_VERIFIED";
    static final String CREATED_AT = "FORGEPILOT_CAPTCHA_CREATED_AT";
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long MAX_AGE_SECONDS = 300;

    @GetMapping(value = "/image", produces = MediaType.IMAGE_PNG_VALUE)
    public void image(HttpSession session, HttpServletResponse response) throws IOException {
        String code = randomCode();
        session.setAttribute(ANSWER, code);
        session.setAttribute(VERIFIED, false);
        session.setAttribute(CREATED_AT, Instant.now().getEpochSecond());

        BufferedImage image = new BufferedImage(190, 58, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(248, 249, 252));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setColor(new Color(45, 55, 72));
        for (int i = 0; i < 8; i++) {
            g.drawLine(RANDOM.nextInt(190), RANDOM.nextInt(58), RANDOM.nextInt(190), RANDOM.nextInt(58));
        }
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(25 + RANDOM.nextInt(70), 25 + RANDOM.nextInt(70), 25 + RANDOM.nextInt(70)));
            g.drawString(String.valueOf(code.charAt(i)), 18 + i * 27, 39 + RANDOM.nextInt(7) - 3);
        }
        g.dispose();
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        ImageIO.write(image, "png", response.getOutputStream());
    }

    @PostMapping("/verify")
    public Map<String, Object> verify(@RequestBody Map<String, String> body, HttpSession session) {
        Object answer = session.getAttribute(ANSWER);
        Object created = session.getAttribute(CREATED_AT);
        String supplied = body.getOrDefault("code", "").trim();
        boolean fresh = created instanceof Long timestamp && Instant.now().getEpochSecond() - timestamp <= MAX_AGE_SECONDS;
        boolean valid = fresh && answer != null && answer.toString().equalsIgnoreCase(supplied);
        session.setAttribute(VERIFIED, valid);
        if (valid) session.removeAttribute(ANSWER);
        return Map.of("verified", valid, "expired", !fresh);
    }

    private String randomCode() {
        StringBuilder value = new StringBuilder(6);
        for (int i = 0; i < 6; i++) value.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        return value.toString();
    }
}
