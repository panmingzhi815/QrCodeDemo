package org.pan;

import com.google.common.base.Strings;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;
import java.util.Arrays;

/**
 * @author panmingzhi
 * 生成二维必要的加密与转换工具
 */
public class CommonUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtil.class);
    public static boolean initialized = false;
    public static final String ALGORITHM = "AES/ECB/ZeroBytePadding";

    /**
     * @param bytes 要被加密的字节
     * @param key   加/解密要用的长度为32的字节数组（256位）密钥
     * @return byte[] 加密后的字节数组
     */
    public static byte[] aes256Encode(byte[] bytes, byte[] key) {
        LOGGER.debug("aes256Encode key len:{} key content:{}", key.length, hex2byte(key));
        LOGGER.debug("aes256Encode bytes len:{} bytes content:{}", bytes.length, hex2byte(bytes));
        initialize();
        byte[] result = null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM, "BC");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            int length = bytes.length / 16 * 16 + (bytes.length % 16 > 1 ? 16 : 0);
            result = cipher.doFinal(bytes);
            byte[] bs = new byte[length];
            System.arraycopy(result, 0, bs, 0, length);
            result = bs;
            LOGGER.debug("aes256Encode result len:{} result content:{}", result.length, hex2byte(result));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        Security.addProvider(new BouncyCastleProvider());
        initialized = true;
    }

    public static String arr2str(char[] chars, int start, int len) {
        char[] charArr = Arrays.copyOfRange(chars, start, start + len);
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : charArr) {
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    public static byte[] crc16(byte[] data, int start, int len) {
        int bitLen = 8;
        byte[] bytes = Arrays.copyOfRange(data, start, start + len);
        LOGGER.debug("CRC len:{} content:{}", bytes.length, hex2byte(bytes));
        if (len > 0) {
            long crc = 0xFFFF;

            for (int i = 0; i < len; i++) {
                crc = crc ^ Byte.toUnsignedInt(bytes[i]);
                for (int j = 0; j < bitLen; j++) {
                    crc = (crc & 1) != 0 ? ((crc >> 1) ^ 0xA001) : (crc >> 1);
                }
            }
            byte hi = (byte) ((crc & 0xFF00) >> 8);
            byte lo = (byte) (crc & 0x00FF);

            byte[] result = new byte[]{lo, hi};
            LOGGER.debug("CRC result:{}", hex2byte(result));
            return result;
        }
        return new byte[]{0, 0};
    }

    public static byte[] hex2byte(String hex) {
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return result;
    }

    public static String hex2byte(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte aByte : bytes) {
            stringBuilder.append(Strings.padStart(Integer.toHexString(Byte.toUnsignedInt(aByte)), 2, '0'));
        }
        return stringBuilder.toString().toUpperCase();
    }

    public static byte[] reverse(byte[] bytes) {
        int length = bytes.length;
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[length - i - 1] = bytes[i];
        }
        return result;
    }

    public static byte[] copyOf(byte[] bytes, int len) {
        byte[] result = new byte[len];
        System.arraycopy(bytes, 0, result, result.length - bytes.length, bytes.length);
        return result;
    }
}
