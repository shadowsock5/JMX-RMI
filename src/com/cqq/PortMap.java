package com.cqq;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class PortMap extends Thread {
    InputStream is;
    OutputStream os;
    static final Logger logger = Logger.getLogger(com.cqq.PortMap.class.getName());

    public PortMap(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }

    public void run() {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = this.is;
            out = this.os;
            byte[] buffer = new byte[8192];

            int a;
            while((a = in.read(buffer)) > 0) {
                out.write(buffer, 0, a);
                out.flush();
            }
        } catch (Exception var13) {
            var13.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

                if (out != null) {
                    out.close();
                }
            } catch (Exception var12) {
                logger.info(var12.toString());
            }

        }

    }

    public static void forward(int localPort, String remoteHost, int remotePort) {
        int port = localPort;
        String forwardHost = remoteHost;
        int forwardPort = remotePort;

        try {
            ServerSocket ss = new ServerSocket(port);
            logger.info("监听端口:" + port);

            while(true) {
                while(true) {
                    try {
                        Socket s = ss.accept();
                        logger.info("Host:" + s.getInetAddress().getHostAddress() + ",连接成功!");
                        Socket socket = new Socket(forwardHost, forwardPort);
                        logger.info("转发到:" + forwardHost + ",端口:" + forwardPort);
                        (new com.cqq.PortMap(socket.getInputStream(), s.getOutputStream())).start();
                        (new com.cqq.PortMap(s.getInputStream(), socket.getOutputStream())).start();
                    } catch (Exception var9) {
                        logger.info(var9.toString());
                    }
                }
            }
        } catch (Exception var10) {
            var10.printStackTrace();
        }
    }
}

