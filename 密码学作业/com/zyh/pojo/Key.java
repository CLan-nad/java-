package com.zyh.pojo;

import com.zyh.handler.KeyExtendHandler;
import com.zyh.utils.MyUtils;

/**
 * @program: AES
 * @description: 128位密钥
 * @author: Zda
 * @create: 2023-10-15 17:02
 **/
public class Key {

    private Integer[][] key;
    private Integer[][] keys;

    private KeyExtendHandler exHandler = new KeyExtendHandler();

    public Key(String nums) {
        Integer[] temp = MyUtils.stringToHex(nums);
        key = new Integer[4][4];
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                key[j][i] = temp[i * 4 +  j];
            }
        }

        extendKeys();
    }

    public void extendKeys() {
        keys = exHandler.extendKeys(key);
    }

    public void setKeys(Integer[][] keys) {
        this.keys = keys;
    }

    public void setExHandler(KeyExtendHandler exHandler) {
        this.exHandler = exHandler;
    }

    public Integer[][] getKeys() {
        return keys;
    }
}
