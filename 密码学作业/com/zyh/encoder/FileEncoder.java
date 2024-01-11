package com.zyh.encoder;

import com.zyh.pojo.State;
import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.util.Arrays;

/**
 * @program: AES
 * @description:
 * @author: Zdadua
 * @create: 2023-11-29 16:30
 **/
public class FileEncoder {

    private String key;

    private State state;

    public FileEncoder(String key) {
        setKey(key);
    }

    public FileEncoder() {
    }

    public void fileEncode(String filePath) throws IOException {
        state = new State(key);

        File file = new File(filePath);

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("encoded.txt"));
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        byte[] buffer = new byte[8192];
        int len = 0;
        int front_len = 0;
        while((len = bis.read(buffer)) > 0) {
            front_len = len;
            buffer = encode(buffer, len);
            len = buffer.length;
            bos.write(buffer, 0, len);
            bos.flush();

        }


        if(front_len % 16 == 0) {
            byte[] tmp = new byte[16];
            Integer[] box = new Integer[16];
            for(int i = 0; i < 16; i++) box[i] = 15;
            state.setStateBox(box);
            state.encode();
            box = state.getState();

            for(int i = 0; i < 16; i++) {
                tmp[i] = box[i].byteValue();
            }

            bos.write(tmp);
        }

        bos.close();
        bis.close();

    }

    public byte[] encode(byte[] buffer, int len) {
        if(len % 16 != 0) {
            int app = 16 - len % 16;

            for(int i = len; i < len + 16 - len % 16; i++) {
                buffer[i] = (byte) (app - 1);
            }
        }

        len = len + 16 - len % 16;

        byte[] ans = new byte[len];
        int index = 0;
        for(int i = 0; (i + 1) * 16 <= len; i++) {
            Integer[] box =  new Integer[16];
            for(int j = 0; j < 16; j++) {
                box[j] = buffer[i * 16 + j] & 0x0ff;
            }

            state.setStateBox(box);
            state.encode();

            box = state.getState();
            for(int j = 0; j < 16; j++) {
                ans[index++] = box[j].byteValue();
            }
        }

        return ans;

    }

    public void fileDecode(String filePath) throws IOException {
        state = new State(key);

        File file = new File(filePath);

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("decoded.txt"));
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

        byte[] buffer = new byte[8192];
        int len = 0;
        int front_len = 0;
        while((len = bis.read(buffer)) > 0) {
            buffer = decode(buffer, len);
            bos.write(buffer, 0, len);
            bos.flush();
            front_len = len;
        }

        RandomAccessFile pointFile = new RandomAccessFile("decoded.txt", "rw");
        len = buffer[front_len - 1] + 1;
        buffer = new byte[len];
        pointFile.seek(pointFile.length() - len);
        pointFile.write(buffer, 0, len);

        bos.close();
        bis.close();
    }

    private byte[] decode(byte[] buffer, int len) {
        byte[] ans = new byte[8192];
        int index = 0;
        for(int i = 0; (i + 1) * 16 <= len; i++) {
            Integer[] box =  new Integer[16];
            for(int j = 0; j < 16; j++) {
                box[j] = buffer[i * 16 + j] & 0x0ff;
            }

            state.setStateBox(box);
            state.decode();

            box = state.getState();
            for(int j = 0; j < 16; j++) {
                ans[index++] = box[j].byteValue();
            }
        }



        return ans;
    }


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

    public static void main(String[] args) {
    }

}
