import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Base64;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;

import com.zyh.encoder.StringEncoder;

public class Client extends JFrame {

    Socket socket;
    Thread thread = null;
    DataInputStream inputFromServer;
    DataOutputStream outputToServer;
    TextField inframe = new TextField(); // 输入框
    TextArea outframe = new TextArea(); // 显示框
    
    public static void main(String[] args){
        new Client();
    }

    public Client() {  
        try {
            // 生成RSA密钥对
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(2048);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            PublicKey clientPublicKey = keyPair.getPublic();
            PrivateKey clientPrivateKey = keyPair.getPrivate();

            // 保存公钥到文件
            FileOutputStream fos = new FileOutputStream("client_public.key");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(clientPublicKey);
            oos.close();
            fos.close();

            // 保存私钥到文件
            fos = new FileOutputStream("client_private.key");
            oos = new ObjectOutputStream(fos);
            oos.writeObject(clientPrivateKey);
            oos.close();
            fos.close();
            
            // 加载服务端公钥
            FileInputStream fis = new FileInputStream("server_public.key");
            ObjectInputStream ois = new ObjectInputStream(fis);
            PublicKey serverPublicKey = (PublicKey) ois.readObject();
            ois.close();
            fis.close();

             // 加载客户端私钥
             fis = new FileInputStream("client_private.key");
             ois = new ObjectInputStream(fis);
             clientPrivateKey = (PrivateKey) ois.readObject();
             ois.close();
             fis.close();

            // 创建RSA数字签名器
            Signature signature = Signature.getInstance("SHA256withRSA");

             //连接服务端
            socket = new Socket("127.0.0.1", 5500); 
            inputFromServer = new DataInputStream(socket.getInputStream());
            outputToServer = new DataOutputStream(socket.getOutputStream());
            System.out.println("连接成功");
            outframe.append("连接成功\n");

            //设置窗口
            this.setTitle("客户端"); 
            outframe.setEditable(false); 
            this.setLocation(350, 200); 
            this.add(inframe, BorderLayout.SOUTH); 
            this.add(outframe, BorderLayout.CENTER); 
            inframe.addActionListener(new inframeListener(signature, clientPrivateKey)); // 输入框增加监听器,用于发送消息给服务器
            this.addWindowListener(new CloseClient()); // 监听处理窗口关闭事件
            this.pack(); // 自动匹配窗体大小
            this.setVisible(true);           

            //新开一个线程类，接收信息
            MessageReceiver messageReceiver = new MessageReceiver(signature, serverPublicKey); 		
            thread = new Thread(messageReceiver);
            thread.start(); 

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //负责发送信息
    private class inframeListener implements ActionListener {

        private Signature signature;
        private PrivateKey clientPrivateKey;

        public inframeListener(Signature signature, PrivateKey clientPrivateKey) {
            this.signature = signature;
            this.clientPrivateKey = clientPrivateKey;
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
                signature.initSign(clientPrivateKey);
                signature.update(encoded.getBytes());
                byte[] clientSignatureBytes = signature.sign();
                outputToServer.writeUTF(encoded);
                outputToServer.writeUTF(Base64.getUrlEncoder().encodeToString(clientSignatureBytes));
                outputToServer.flush();
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
        private PublicKey serverPublicKey;
    
        public MessageReceiver(Signature signature, PublicKey serverPublicKey) {
            this.signature = signature;
            this.serverPublicKey = serverPublicKey;
        }
    
        public void run() {
            try {
                boolean goon = true;
                while (goon) {              
                    // 接收服务端发送的数字签名和消息
                    String receivedMessage = inputFromServer.readUTF();
                    String receivedSignature = inputFromServer.readUTF();

                    // System.out.println("信息为："  + receivedMessage);
                     

                    if (!receivedMessage.equals("bye")) {
                        // 验证服务端数字签名
                        signature.initVerify(serverPublicKey);
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
                            System.out.println("服务端数字签名验证成功");
                            System.out.println("签名为："  + receivedSignature);
                            System.out.println("密文为："  + receivedMessage);
                            System.out.println("明文为：" + decoded);
                            outframe.append("接收：" + decoded + "\n");
                        } else {
                            outframe.append("服务端数字签名验证失败,通讯中断\n");
                            System.out.println("服务端数字签名验证失败,通讯中断");
                            System.exit(0);
                        }
                    }
                }
            } catch (IOException | SignatureException | InvalidKeyException |ClassNotFoundException e) {
                e.printStackTrace();
            } 
        }
    }
    

    private class CloseClient extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			try {
				if (socket != null) {
					outputToServer.writeUTF("bye");
                    outputToServer.flush();
				}
                inputFromServer.close();
                outputToServer.close();
                socket.close();
                System.exit(0);
			} catch (IOException e1) {
				e1.printStackTrace();
			}    
		}
	}

}