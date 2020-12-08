# HTTPdLight

HTTPdLight is a lightweight HTTP Server used to Mock endpoints, create dynamic stub or proxy service with verbose debugging. It requires only to have JRE or a JDK installed. Minimum version or JRE or JDK is 1.6.

## Getting Started

Please download the [jar](jar/httpdlight-0.2.jar?raw=true) from github.
For Testing with SSL, you can also download the self-signed certificate [jks](httpdlight.jks?raw=true). Apart from that, you have:

- the private certificate is available here [pem](httpdlight.pem?raw=true)
- the public certificate is available here [der](httpdlight.der?raw=true)

### Java Check

Make sure that you have java in your path. Check java version:

```
ant@myserver:~$ java -version
java version "1.6.0_45"
Java(TM) SE Runtime Environment (build 1.6.0_45-b06)
Java HotSpot(TM) 64-Bit Server VM (build 20.45-b01, mixed mode)

```

## Run the Server with default arguments

The default port is 8008, the default uri is /rnif/partner1 and the default reply is an empty String ""

```
ant@myserver:~$ java -jar httpdlight-0.3.jar
Starting HTTPServer at port 8008, URI /rnif/partner1
Started!

```

#### Testing


Setup Postman to call the HTTPdLight server, in the following example we are running Postman in the same server.
If we are running Postman in the different server replace localhost with the IP or the name of the server where HTTPdLight is running.

![Alt text](resources/Postman_Default.png?raw=true "HTTPdLight")

```
########## Wed Jun 13 12:38:11 SGT 2018 Receiving request ##########
Receiving request /rnif/partner1
Headers
	Accept-encoding:[gzip, deflate]
	Accept:[*/*]
	Connection:[keep-alive]
	Host:[localhost:8008]
	User-agent:[PostmanRuntime/7.1.5]
	Content-type:[application/xml]
	Postman-token:[a139287c-2ead-46ef-bce9-8500e3c0dd3b]
	Content-length:[32]
	Cache-control:[no-cache]
Body :
<message>
Hello World
</message>
########## Wed Jun 13 12:38:11 SGT 2018 End of Receiving request ##########

```

## Run the Server with specific arguments

You can override the port, the URI or the reply. On the following example, we are overriden all of the elements:

```
ant@myserver:~$ java -DPORT=8009 -DURI="/rnif/partner3" -DREPLY="<ok/>" -jar httpdlight-0.2.jar
Found overridden uri: /rnif/partner3
Found overridden port: 8009
Found overridden reply: <ok/>
Starting HTTPServer at port 8009, URI /rnif/partner3
Started!
```

You can use a specific reply handler to return payload and callback another from a properties file:

```
ant@myserver:~$ java -DPORT=8009 -DURI="/rnif/partner3" -DREPLYHANDLER="replyhandler.properties" -jar httpdlight-0.3.jar
Found overridden uri: /rnif/partner3
Found overridden port: 8009
Found overridden reply: <ok/>
Starting HTTPServer at port 8009, URI /rnif/partner3
Started!
```

### Testing

Setup Postman to call the HTTPdLight server, in the following example we are running Postman in the same server.
If we are running Postman in the different server replace localhost with the IP or the name of the server where HTTPdLight is running.

![Alt text](resources/Postman_Override.png?raw=true "HTTPdLight")

```
########## Wed Jun 13 12:47:58 SGT 2018 Receiving request ##########
Receiving request /rnif/partner3
Headers
        Cache-control:[no-cache]
        Host:[localhost:8009]
        Content-type:[application/xml]
        Accept-encoding:[gzip, deflate]
        Content-length:[32]
        Connection:[keep-alive]
        Postman-token:[dee23f82-0e1f-4ac0-9cdf-508c1210f84a]
        User-agent:[PostmanRuntime/7.1.5]
        Accept:[*/*]
Body :
<message>
Hello World
</message>
########## Wed Jun 13 12:47:58 SGT 2018 End of Receiving request ##########
```

## Run the Server with SSL

You can override the port, the URI or the reply. On the following example, we are overriden all of the elements:

```
ant@myserver:~$ java -DSSLCERT=../httpdlight.jks -DSSLPASSWORD=password -DREPLY="<ok/>" -jar httpdlight-0.2.jar
Found overridden reply: <ok/>
Found overridden SSLCERT: httpdlight.jks
Found overridden SSLPASSWORD: password
Starting HTTPServer at port 8009, URI /rnif/partner1 (with SSL)
Started!
```

### Testing

Setup Postman to call the HTTPdLight server, disable SSL certification verification (Settings>General>Request>SSL certification verification), in the following example we are running Postman in the same server.
If we are running Postman in the different server replace localhost with the IP or the name of the server where HTTPdLight is running.

![Alt text](resources/Postman_SSL.png?raw=true "HTTPdLight")

```
########## Wed Jun 27 11:28:00 SGT 2018 Receiving request ##########
Receiving request /rnif/partner1
Headers
        Accept-encoding:[gzip, deflate]
        Accept:[*/*]
        Connection:[keep-alive]
        Host:[localhost:8008]
        User-agent:[PostmanRuntime/7.1.5]
        Content-type:[application/xml]
        Postman-token:[24ae7914-280f-4c81-8c0c-a3d775c08c5d]
        Content-length:[32]
        Cache-control:[no-cache]
Body :
<message>
Hello World
</message>
########## Wed Jun 27 11:28:00 SGT 2018 End of Receiving request ##########
```

## Using httpdlight as a Mock/Stub for Performance Benchmarks

Use the following command:

```
java -DPORT=8008 -DURI=/ -DREPLY=OKOK -DBENCHMARK=true -jar httpdlight-0.4.jar
```

The server will listen to any request on port 8008 and will reply with a ```OKOK``` and HTTP Status of 200.

Several tests were conducted and the server can provide a throughput of up to 1500 transactions per second on a 2 core machine with 16GB of Memory (15% of the CPU is used and about 512MB or Memory).