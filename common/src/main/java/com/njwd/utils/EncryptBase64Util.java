package com.njwd.utils;

import com.njwd.exception.ResultCode;
import com.njwd.exception.ServiceException;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * BASE64
 *
 * @author zhuzs
 * @date 2019-11-13 16:56
 */
public class EncryptBase64Util {
    /**
     * 加密
     *
     * @param: [key]
     * @return: java.lang.String
     * @author: zhuzs
     * @date: 2019-11-13
     */
    public static String encryptBASE64(byte[] key) {

        try {
            return (new BASE64Encoder()).encodeBuffer(key).trim();
        } catch (Exception e) {
            throw new ServiceException(ResultCode.ENCRYPT_FAILED);
        }
    }

    /**
     * 解密
     *
     * @param: [key]
     * @return: byte[]
     * @author: zhuzs
     * @date: 2019-11-13
     */
    public static byte[] decryptBASE64(String key) throws Exception {
        return (new BASE64Decoder()).decodeBuffer(key);
    }
}

