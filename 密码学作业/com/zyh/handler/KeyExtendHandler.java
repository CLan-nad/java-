package com.zyh.handler;

import com.zyh.utils.EncodingUtils;

/**
 * @program: AES
 * @description: 密钥扩展
 * @author: Zda
 * @create: 2023-10-15 17:07
 **/
public class KeyExtendHandler {

    private Integer[] Rcon = {
            0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36
    };

    public Integer[][] extendKeys(Integer[][] key) {

        Integer[][] res = new Integer[key.length][10 * 4 + 4];

        for(int i = 0; i < key.length; i++) {
            for(int j = 0; j < key[0].length; j++) {
                res[i][j] = key[i][j];
            }
        }


        for(int i = 4; i < 4 * 10 + 4; i++) {
            if((i + 1) % 4 == 0) {
                // ByteSub(RotByte(w[i - 1]))
                res[3][i] = res[0][i - 1];
                for(int j = 0; j < 3; j++) {
                    res[j][i] = EncodingUtils.inBox(res[j + 1][i - 1]);
                }

                res[0][i] = res[0][i] ^ Rcon[(i / 4) - 1];
            }
            else {
                for(int j = 0; j < 4; j++) {
                    res[j][i] = res[j][i - 1] ^ res[j][i - 4];
                }
            }
        }

        return res;
    }

    //万一要重新设置
    public void setRcon(Integer[] rcon) {
        Rcon = rcon;
    }
}
