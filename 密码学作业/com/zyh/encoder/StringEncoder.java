package com.zyh.encoder;

import com.zyh.pojo.State;
import com.zyh.utils.MyUtils;

import java.sql.SQLOutput;

/**
 * @program: AES
 * @description:
 * @author: Zdadua
 * @create: 2023-11-28 21:35
 **/
public class StringEncoder {

    private String key;

    private State state;

    public StringEncoder(String key) {
        setKey(key);
    }

    public StringEncoder() {}

    // 字符串转十六进制字符串
    public static String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            // 返回的总是2 * n 长度
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            // sb.append(' ');
        }
        return sb.toString().trim();
    }

    // 十六进制字符串转字符串
    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;
        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    /*
     * @Description 将明文加密为十六进制字符串
     * @param String str 明文
     * @return String 加密后所得十六进制字符串
     * @date 2023/11/29 11:22
     * @user Those_Pigs
     */
    public String encode(String str) {
        state = new State(key);
        String hexStr = str2HexStr(str);

        // 如果分组长度不足，则填充n个0x0n 长度足够则填充16个 0x0f
        int app = (32 - hexStr.length() % 32) / 2;

        app = app == 0 ? 16 : app;

        StringBuilder builder = new StringBuilder(hexStr);
        String a = "0" + Integer.toHexString(app - 1);
        while(app-- != 0) {
            builder.append(a);
        }

        hexStr = builder.toString();
        StringBuilder res = new StringBuilder();

        for(int i = 0; i < hexStr.length() / 32; i++) {
            state.setStateBox(hexStr.substring(i * 32, i * 32 + 32));
            state.encode();
            res.append(state.getStateBox());
        }

        return res.toString();
    }

    public String decode(String str) {
        state = new State(key);
        int len = str.length();

        if(len % 32 != 0) {
            System.out.println("长度有误!");
            return null;
        }

        StringBuilder res = new StringBuilder();
        for(int i = 0; i < len / 32; i++) {
            state.setStateBox(str.substring(i * 32, i * 32 + 32));
            state.decode();
            res.append(state.getStateBox());
        }

        int end = Character.digit(res.charAt(len - 1), 16);

        return hexStr2Str(res.substring(0, res.length() - (end + 1) * 2));
    }

    /*
     * @Description 赋值密钥：密钥过长则截断，密钥过短则填充0
     * @param String key 输入密钥
     * @return void
     * @date 2023/11/29 10:37
     * @user Those_Pigs
     */
    public void setKey(String key) {
        String hexKey = str2HexStr(key);
        if(hexKey.length() < 32) {
            StringBuilder builder = new StringBuilder(hexKey);
            int index = 32 - hexKey.length();
            while(index-- != 0) {
                builder.append("0");
            }

            hexKey = builder.toString();
        }
        else {
            hexKey = hexKey.substring(0, 32);
        }

        this.key = hexKey;
    }

}
