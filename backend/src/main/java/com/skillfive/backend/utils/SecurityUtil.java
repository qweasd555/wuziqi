package com.skillfive.backend.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 安全工具类
 */
public class SecurityUtil {
    
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 生成UUID
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 密码加密（BCrypt）
     */
    public static String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * 密码匹配（BCrypt）
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    /**
     * MD5加密
     */
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不存在", e);
        }
    }
    
    /**
     * SHA-256加密
     */
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes());
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不存在", e);
        }
    }
    
    /**
     * Base64编码
     */
    public static String base64Encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }
    
    /**
     * Base64解码
     */
    public static String base64Decode(String encoded) {
        return new String(Base64.getDecoder().decode(encoded));
    }
    
    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    /**
     * 生成随机字符串
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return sb.toString();
    }
    
    /**
     * 验证邮箱格式
     */
    public static boolean isValidEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.matches(emailRegex, email);
    }
    
    /**
     * 验证手机号格式（中国大陆）
     */
    public static boolean isValidPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return false;
        }
        String phoneRegex = "^1[3-9]\\d{9}$";
        return Pattern.matches(phoneRegex, phone);
    }
    
    /**
     * 验证密码强度
     * 至少8位，包含大小写字母、数字和特殊字符中的至少三种
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        int strength = 0;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*\\d.*")) strength++;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) strength++;
        
        return strength >= 3;
    }
    
    /**
     * 脱敏处理手机号
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }
    
    /**
     * 脱敏处理邮箱
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        if (parts[0].length() <= 3) {
            return parts[0].charAt(0) + "***@" + parts[1];
        }
        String username = parts[0];
        int showLength = Math.min(3, username.length() / 2);
        StringBuilder masked = new StringBuilder(username.substring(0, showLength));
        for (int i = 0; i < Math.min(5, username.length() - showLength); i++) {
            masked.append("*");
        }
        masked.append("@").append(parts[1]);
        return masked.toString();
    }
    
    /**
     * 清理XSS攻击字符
     */
    public static String cleanXSS(String input) {
        if (input == null) {
            return null;
        }
        return input
                .replaceAll("<script.*?>.*?</script>", "")
                .replaceAll("<iframe.*?>.*?</iframe>", "")
                .replaceAll("javascript:", "")
                .replaceAll("onload=", "")
                .replaceAll("onerror=", "")
                .replaceAll("onclick=", "")
                .replaceAll("<[^>]*>", "");
    }
    
    /**
     * 生成验证码
     */
    public static String generateVerificationCode(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }
}