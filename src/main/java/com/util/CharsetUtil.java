package com.util;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;

public class CharsetUtil {
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static Logger log = LoggerFactory.getLogger(CharsetUtil.class);

    /**
     * 字节buffer转为字符串
     * 
     * @param byteBuf
     * @param length
     * @return String
     * @date: 2020年3月24日 上午10:27:49
     */
    public static String byteToString(ByteBuf byteBuf, int length) {
        byte[] bytes = byteBuf.array();
        return byteToString(bytes);
    }

    /**
     * 字节数组转为字符串
     * 
     * @param src
     * @return String
     * @date: 2020年3月24日 上午10:30:02
     */
    public static String byteToString(byte[] src) {
        return new String(src, UTF_8);
    }

    /**
     * 字符串转为字节数组
     * 
     * @param src
     * @return byte[]
     * @date: 2020年3月24日 上午10:31:35
     */
    public static byte[] stringToByte(String src) {
        return src.getBytes(UTF_8);
    }
}
