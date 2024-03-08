package cn.com.seckill.threads;

/**
Copyright (c) 2011 IETF Trust and the persons identified as
authors of the code. All rights reserved.
Redistribution and use in source and binary forms, with or without
modification, is permitted pursuant to, and subject to the license
terms contained in, the Simplified BSD License set forth in Section
4.c of the IETF Trust's Legal Provisions Relating to IETF Documents
(http://trustee.ietf.org/license-info).
 */
 
import java.lang.reflect.UndeclaredThrowableException;
import java.security.GeneralSecurityException;
import java.util.Date;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
 
/**
 * TOTP算法(Time-based One-time Password algorithm)是一种从共享密钥和当前时间计算一次性密码的算法。
 * 它已被采纳为Internet工程任务组标准RFC 6238，是Initiative for Open Authentication（OATH）的基石，并被用于许多双因素身份验证系统。
 * 
 * TOTP是基于散列的消息认证码（HMAC）的示例。 它使用加密哈希函数将密钥与当前时间戳组合在一起以生成一次性密码。 
 * 由于网络延迟和不同步时钟可能导致密码接收者必须尝试一系列可能的时间来进行身份验证，因此时间戳通常以30秒的间隔增加，从而减少了潜在的搜索空间。
 * 
 * 该算法是网银动态口令实现的基石，如中国银行的动态口令就是基于此算法实现
 * @author Ziang Xu
 *
 */
public class Totp {
 
    private Totp() {
    }
 
    /**
     * This method uses the JCE to provide the crypto algorithm. HMAC computes a
     * Hashed Message Authentication Code with the crypto hash algorithm as a
     * parameter.
     * 
     * @param crypto
     *            : the crypto algorithm (HmacSHA1, HmacSHA256, HmacSHA512)
     * @param keyBytes
     *            : the bytes to use for the HMAC key
     * @param text
     *            : the message or text to be authenticated
     */
    private static byte[] hmac_sha(String crypto, byte[] keyBytes, byte[] text) {
        try {
            Mac hmac;
            hmac = Mac.getInstance(crypto);
            SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }
 
    /**
     * This method converts a HEX string to Byte[]
     * 
     * @param hex
     *            : the HEX string
     * 
     * @return: a byte array
     */
 
    private static byte[] hexStr2Bytes(String hex) {
        // Adding one byte to get the right conversion
        // Values starting with "0" can be converted
        byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();
 
        // Copy all the REAL bytes, not the "first"
        byte[] ret = new byte[bArray.length - 1];
        for (int i = 0; i < ret.length; i++)
            ret[i] = bArray[i + 1];
        return ret;
    }
 
    private static final int[] DIGITS_POWER
    // 0 1 2 3 4 5 6 7 8
    = { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000 };
 
    /**
     * This method generates a TOTP value for the given set of parameters.
     * 
     * @param key
     *            : the shared secret, HEX encoded
     * @param time
     *            : a value that reflects a time
     * @param returnDigits
     *            : number of digits to return
     * 
     * @return: a numeric String in base 10 that includes
     *          {@link truncationDigits} digits
     */
 
    public static String generateTOTP(String key, String time,
            String returnDigits) {
        return generateTOTP(key, time, returnDigits, "HmacSHA1");
    }
 
    /**
     * This method generates a TOTP value for the given set of parameters.
     * 
     * @param key
     *            : the shared secret, HEX encoded
     * @param time
     *            : a value that reflects a time
     * @param returnDigits
     *            : number of digits to return
     * 
     * @return: a numeric String in base 10 that includes
     *          {@link truncationDigits} digits
     */
 
    public static String generateTOTP256(String key, String time,
            String returnDigits) {
        return generateTOTP(key, time, returnDigits, "HmacSHA256");
    }
 
    /**
     * This method generates a TOTP value for the given set of parameters.
     * 
     * @param key
     *            : the shared secret, HEX encoded
     * @param time
     *            : a value that reflects a time
     * @param returnDigits
     *            : number of digits to return
     * 
     * @return: a numeric String in base 10 that includes
     *          {@link truncationDigits} digits
     */
 
    public static String generateTOTP512(String key, String time,
            String returnDigits) {
        return generateTOTP(key, time, returnDigits, "HmacSHA512");
    }
 
    /**
     * This method generates a TOTP value for the given set of parameters.
     * 
     * @param key
     *            : the shared secret, HEX encoded
     * @param time
     *            : a value that reflects a time
     * @param returnDigits
     *            : number of digits to return
     * @param crypto
     *            : the crypto function to use
     * 
     * @return: a numeric String in base 10 that includes
     *          {@link truncationDigits} digits
     */
 
    public static String generateTOTP(String key, String time,
            String returnDigits, String crypto) {
        int codeDigits = Integer.decode(returnDigits).intValue();
        String result = null;

        // Using the counter
        // First 8 bytes are for the movingFactor
        // Compliant with base RFC 4226 (HOTP)
        while (time.length() < 16)
            time = "0" + time;

        // Get the HEX in a Byte[]
        byte[] msg = hexStr2Bytes(time);
        byte[] k = hexStr2Bytes(key);
        byte[] hash = hmac_sha(crypto, k, msg);

        // put selected bytes into result int
        int offset = hash[hash.length - 1] & 0xf;

        int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);

        int otp = binary % DIGITS_POWER[codeDigits];

        result = Integer.toString(otp);
        while (result.length() < codeDigits) {
            result = "0" + result;
        }
        return result;
    }
    
    public static void main(String[] args) {
        try {

            for (int j = 0; j < 10; j++) {
            	long now = new Date().getTime();
            	System.out.println(now);
                String totp = generateTOTP("123456", Long.toString(now), "8", "HmacSHA256");
                System.out.println(String.format("加密后: %s", totp));
                //Thread.sleep(1000);
            }

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}