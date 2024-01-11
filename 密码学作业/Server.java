import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Base64;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;

import com.zyh.encoder.StringEncoder;

public class Server extends JFrame {

    Socket clientSocket;
    ServerSocket serversocket;
    Thread thread = null;
    DataInputStream inputFromClient;
    DataOutputStream outputToClient;
    TextField inframe = new TextField(); // 输入框
    TextArea outframe = new TextArea(); // 显示框
    
    public static void main(String[] args){
        new Server();
    }
    
    public Server() {  
        try {
           // 生成RSA密钥对
           KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
           keyPairGen.initialize(2048);
           KeyPair keyPair = keyPairGen.generateKeyPair();
           PublicKey serverPublicKey = keyPair.getPublic();
           PrivateKey serverPrivateKey = keyPair.getPrivate();

           // 保存公钥到文件
           FileOutputStream fos = new FileOutputStream("server_public.key");
           ObjectOutputStream oos = new ObjectOutputStream(fos);
           oos.writeObject(serverPublicKey);
           oos.close();
           fos.close();

           // 保存私钥到文件
           fos = new FileOutputStream("server_private.key");
           oos = new ObjectOutputStream(fos);
           oos.writeObject(serverPrivateKey);
           oos.close();
           fos.close();
            
            // 创建RSA数字签名器
            Signature signature = Signature.getInstance("SHA256withRSA");

            //设置窗口
            this.setTitle("服务端"); 
            outframe.setEditable(false); 
            this.setLocation(350, 200); 
            this.add(inframe, BorderLayout.SOUTH); 
            this.add(outframe, BorderLayout.CENTER); 
            inframe.addActionListener(new inframeListener(signature, serverPrivateKey)); // 输入框增加监听器,用于发送消息给客户端
            this.addWindowListener(new CloseClient()); // 监听处理窗口关闭事件
            this.pack(); // 自动匹配窗体大小
            this.setVisible(true);           

            // 创建ServerSocket并监听端口5500
            ServerSocket serverSocket = new ServerSocket(5500);
            System.out.println("服务端已启动，等待客户端连接...");
            outframe.append("服务端已启动，等待客户端连接...\n");
            clientSocket = serverSocket.accept();
            System.out.println("客户端已连接");
            outframe.append("客户端已连接\n");
            inputFromClient = new DataInputStream(clientSocket.getInputStream());
            outputToClient = new DataOutputStream(clientSocket.getOutputStream());

             // 加载客户端公钥
            FileInputStream fis = new FileInputStream("client_public.key");
            ObjectInputStream ois = new ObjectInputStream(fis);
            PublicKey clientPublicKey = (PublicKey) ois.readObject();            
            ois.close();
            fis.close();

            // 加载服务端私钥
            fis = new FileInputStream("server_private.key");
            ois = new ObjectInputStream(fis);
            serverPrivateKey = (PrivateKey) ois.readObject();
            ois.close();
            fis.close();


            //新开一个线程类，接收信息
            MessageReceiver messageReceiver = new MessageReceiver(signature, clientPublicKey); 		
            thread = new Thread(messageReceiver);
            thread.start(); 

           
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //负责发送信息
    private class inframeListener implements ActionListener {

        private Signature signature;
        private PrivateKey serverPrivateKey;

        public inframeListener(Signature signature, PrivateKey serverPrivateKey) {
            this.signature = signature;
            this.serverPrivateKey = serverPrivateKey;
        }

		public void actionPerformed(ActionEvent e) {
			String tempStr = inframe.getText().trim(); // 得到输入框输入的字符串
			inframe.setText(""); 
			try {
                //AES加密
                    String key= AESusing.KeyGenerator.generateRandomKey();
                    FileOutputStream Fis = new FileOutputStream("AES.key");
                    ObjectOutputStream Ois = new ObjectOutputStream(Fis);
                    Ois.writeObject(key);
                    Ois.close();
                    Fis.close();
                    StringEncoder stringEncoder = new StringEncoder(key);
                    String encoded = stringEncoder.encode(tempStr);               	
                // 发送数字签名和消息                                 
                signature.initSign(serverPrivateKey);
                signature.update(encoded.getBytes());
                byte[] serverSignatureBytes = signature.sign();
                outputToClient.writeUTF(encoded);
                outputToClient.writeUTF(Base64.getUrlEncoder().encodeToString(serverSignatureBytes));
                outputToClient.flush();
                System.out.println("消息已发送");	
                outframe.append("发送：" + tempStr + "\n");			
				 
			} catch (IOException | SignatureException | InvalidKeyException e1) {
				e1.printStackTrace();
			} 
		}
	}

    //负责接收信息
    public class MessageReceiver implements Runnable {
        private Signature signature;
        private PublicKey clientPublicKey;
    
        public MessageReceiver(Signature signature, PublicKey clientPublicKey) {
            this.signature = signature;
            this.clientPublicKey = clientPublicKey;
        }
    
        public void run() {
            try {
                boolean goon = true;
                while (goon) {              
                    // 接收客户端发送的数字签名和消息
                    String receivedMessage = inputFromClient.readUTF();                   
                    String receivedSignature = inputFromClient.readUTF();
                    // System.out.println("信息为："  + receivedMessage);
                    // System.out.println("签名为："  + receivedSignature);

                    if (!receivedMessage.equals("bye")) {
                       // 验证客户端数字签名
                        signature.initVerify(clientPublicKey);
                        signature.update(receivedMessage.getBytes());
                        boolean verified = signature.verify(Base64.getUrlDecoder().decode(receivedSignature));
                        //AES解密
                        FileInputStream FIs = new FileInputStream("AES.key");
                        ObjectInputStream OIs = new ObjectInputStream(FIs);
                        String Key=(String)OIs.readObject();
                        OIs.close();
                        FIs.close();
                        StringEncoder StringEncoder = new StringEncoder(Key);
                        String decoded =StringEncoder.decode(receivedMessage);
                        if (verified) {
                            System.out.println("客户端数字签名验证成功");
                            System.out.println("签名为："  + receivedSignature);
                            System.out.println("密文为："  + receivedMessage);
                            System.out.println("明文为：" + decoded);
                            outframe.append("接收：" + decoded + "\n");
                        } else {
                            outframe.append("服务端数字签名验证失败,通讯中断\n");
                            System.out.println("客户端数字签名验证失败，通讯中断");                          
                            System.exit(0);
                        }
                    }
                }
            } catch (IOException | SignatureException | InvalidKeyException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    

    private class CloseClient extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			try {
				if (clientSocket != null) {
					outputToClient.writeUTF("bye");
                    outputToClient.flush();
				}
                inputFromClient.close();
                outputToClient.close();
                // serverSocket.close();
                clientSocket.close();
                System.exit(0);
			} catch (IOException e1) {
				e1.printStackTrace();
			}    
		}
	}

}