package com.zyh.utils;

import java.util.Arrays;

/**
 * @program: AES
 * @description: 转换String-Hex工具类
 * @author: Zda
 * @create: 2023-10-17 22:54
 **/
public class MyUtils {

    // 字符串转十六进制
    public static Integer[] stringToHex(String k) {
        int len = k.length();
        Integer[] res = new Integer[len / 2];

        for(int i = 0; i < len; i += 2) {
            res[i / 2] = (Character.digit(k.charAt(i), 16) << 4) + Character.digit(k.charAt(i + 1), 16);
        }

        return  res;
    }

    // 十六进制转字符串
    public static String hexToString(Integer[][] matrix) {
        StringBuffer ans = new StringBuffer();
        String temp = "";

        for(int i = 0; i< matrix[0].length; i++) {
            for(int j = 0; j < matrix.length; j++) {
                temp = Integer.toHexString(matrix[j][i]);
                if(temp.length() < 2) {
                    ans.append("0");
                }
                ans.append(temp);
            }
        }

        return ans.toString().toUpperCase();
    }

    // 列混淆
    public static void confuse(Integer[][] confuseMatrix, Integer[][] stateBox) {
        Integer[][] res = new Integer[stateBox.length][stateBox[0].length];

        for(int i = 0; i < confuseMatrix.length; i++) {
            for(int j = 0; j < stateBox[0].length; j++) {
                Integer temp = 0;

                for(int k = 0; k < stateBox.length; k++) {
                    temp = temp ^ cal(stateBox[k][j], confuseMatrix[i][k]);
                }

                res[i][j] = temp;
            }
        }

        for(int i = 0; i < stateBox.length; i++) {
            stateBox[i] = Arrays.copyOf(res[i], res[i].length);
        }
    }

    // 计算 num * x
    private static Integer cal(Integer num, Integer x) {
        if(x == 1) {
            return num;
        }

        Integer ans = 0;
        int index;

        while(x != 0) {
            index = 0;
            while(Math.pow(2, index + 1) <= x) {
                index++;
            }

            x = x - (int)Math.pow(2, index);

            if(index == 0) {
                ans = ans ^ num;
                break;
            }

            Integer temp = num;
            while(index-- != 0) {
                if(((temp >>> 7) & 0x01) == 0) {
                    temp = temp << 1;
                }
                else {
                    temp = ((temp << 1) & 0xFF) ^ 0x1B;
                }
            }

            ans = ans ^ temp;

        }

        return ans;
    }

    // 打印矩阵
    public static void printMatrix(Integer[][] m) {

        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                System.out.print(Integer.toHexString(m[i][j]).toUpperCase() + ", ");
            }
            System.out.println();
        }

    }

}
