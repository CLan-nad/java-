package com.zyh.pojo;

import com.zyh.utils.DecodingUtils;
import com.zyh.utils.EncodingUtils;
import com.zyh.utils.MyUtils;

/**
 * @program: AES
 * @description: 状态矩阵
 * @author: Zda
 * @create: 2023-10-15 15:25
 **/
public class State {

    private Integer[][] stateBox;

    private Integer[][] keys;

    public State(String key) {
        keys = new Key(key).getKeys();
    }

    public State(String str, String key) {

        setStateBox(str);
        keys = new Key(key).getKeys();
    }

    public void byteReplace(int model) {
        if(model == 0) {
            EncodingUtils.byteReplace(stateBox);
        }
        else {
            DecodingUtils.byteReplace(stateBox);
        }

    }

    public void moveLine(int model) {
        if(model == 0) {
            EncodingUtils.moveLine(stateBox);
        }
        else {
            DecodingUtils.moveLine(stateBox);
        }

    }

    public void confuse(int model) {
        if(model == 0) {
            EncodingUtils.confuse(stateBox);
        }
        else {
            DecodingUtils.confuse(stateBox);
        }

    }

    public void add(int n) {
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                Integer a = stateBox[i][j];
                Integer b = keys[i][n * 4 + j];
                stateBox[i][j] ^= keys[i][n * 4 + j];
                Integer c = stateBox[i][j];
            }
        }

    }

    public void encode() {
        add(0);

        for(int i = 1; i <= 9; i++) {
            byteReplace(0);
            moveLine(0);
            confuse(0);
            add(i);
        }

        byteReplace(0);
        moveLine(0);
        add(10);
    }

    public void decode() {
        add(10);

        for(int i = 9; i >= 1; i--) {
            moveLine(1);
            byteReplace(1);
            add(i);
            confuse(1);
        }

        moveLine(1);
        byteReplace(1);
        add(0);
    }

    public void setStateBox(String str) {
        Integer[] nums = MyUtils.stringToHex(str);
        stateBox = new Integer[4][4];
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                stateBox[j][i] = nums[i * 4 +  j];
            }
        }
    }

    public void setStateBox(Integer[] box) {
        stateBox = new Integer[4][4];
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                stateBox[j][i] = box[i * 4 + j];
            }
        }
    }

    public String getStateBox() {
        return MyUtils.hexToString(stateBox);
    }

    public Integer[] getState() {
        Integer[] res = new Integer[16];
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                res[i * 4 + j] = stateBox[j][i];
            }
        }

        return res;
    }
}
