package com.zyh.test;

import com.zyh.encoder.FileEncoder;
import com.zyh.encoder.StringEncoder;

import java.io.IOException;
import java.util.Scanner;

/**
 * @program: AES
 * @description: 主测试类
 * @author: Zda
 * @create: 2023-10-17 11:54
 **/
public class MainTest {

    public static Scanner myScanner = new Scanner(System.in);

    public static void main() {

        Boolean flag = true;

        while(flag) {
            System.out.println("------------  AES  ------------");
            System.out.println("------------1. 加密 ------------");
            System.out.println("------------2. 解密 ------------");
            System.out.println("------------3. 退出 ------------");
            System.out.print("请输入选择: ");
            myScanner = new Scanner(System.in);
            int choice = myScanner.nextInt();
            myScanner.nextLine();

            switch(choice) {
                case 1:
                    encode();
                    break;
                case 2:
                    decode();
                    break;
                case 3:
                    flag = false;
                    break;
                default:
                    System.out.println("选择有误！");
            }
        }
    }

    public static void encode() {
        Boolean flag = true;

        while(flag) {
            System.out.print("请输入密钥: ");
            String key = myScanner.nextLine();

            StringEncoder stringEncoder = new StringEncoder(key);
            FileEncoder  fileEncoder = new FileEncoder(key);
            boolean flag02 = true;
            while(flag02) {
                System.out.println("------------ 加密类型 ------------");
                System.out.println("------------1. 字符串 ------------");
                System.out.println("------------2.  文件 ------------");
                System.out.println("------------3.  退出 ------------");
                System.out.print("请输入选择: ");
                int choice = myScanner.nextInt();
                myScanner.nextLine();

                String str = null;
                switch(choice) {
                    case 1:

                        System.out.print("请输入需要加密的字符串: ");
                        str = myScanner.nextLine();
                        String encoded = stringEncoder.encode(str);
                        System.out.println("密文: " + encoded);
                        break;
                    case 2:
                        System.out.print("请输入需要加密的文件路径: ");
                        str = myScanner.nextLine();
                        try {
                            fileEncoder.fileEncode(str);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case 3:
                        flag02 = false;
                        break;
                    default:
                        System.out.println("选择有误!");
                }
            }


            flag = false;
        }

    }

    /*
     * @Description 检查是否为十六进制
     * @param [str]
     * @return boolean
     */

    public static void decode() {
        Boolean flag = true;

        while(flag) {
            System.out.print("请输入密钥: ");
            String key = myScanner.nextLine();
            StringEncoder stringEncoder = new StringEncoder(key);
            FileEncoder  fileEncoder = new FileEncoder(key);

            String str = null;
            boolean flag02 = true;
            while(flag02) {
                System.out.println("------------ 解密类型 ------------");
                System.out.println("------------1. 字符串 ------------");
                System.out.println("------------2.  文件 ------------");
                System.out.println("------------3.  退出 ------------");
                System.out.print("请输入选择: ");
                int choice = myScanner.nextInt();
                myScanner.nextLine();

                switch(choice) {
                    case 1:
                        System.out.print("请输入需要解密的字符串: ");
                        str = myScanner.nextLine();
                        String decoded = stringEncoder.decode(str);
                        System.out.println("密文: " + decoded);
                        break;
                    case 2:
                        System.out.print("请输入需要解密的文件路径: ");
                        str = myScanner.nextLine();
                        try {
                            fileEncoder.fileDecode(str);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case 3:
                        flag02 = false;
                        break;
                    default:
                        System.out.println("选择有误!");
                }
            }

            flag = false;
        }

    }

}
