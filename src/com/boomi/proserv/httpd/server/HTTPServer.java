package com.boomi.proserv.httpd.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

/**
 * HTTPServer Class: this is a lightweight HTTP server used to Mock endpoints which require only a JRE/JDK installed (JDK 6+)
 * Default port is 8008 and default uri is /rnif/partner1 <br/>
 * To override these values use -D with PORT, URI or REPLY as follow:
 * java -DPORT=8009 -DURI="/rnif/partner3" -DREPLY="<ok/>" -jar httpdlight-0.1.jar
 * @author anthony.rabiaza@gmail.com
 *
 */
public class HTTPServer {
	
	private static String URI 			= "/rnif/partner1"; // Should start with "/" 
	private static Integer PORT 		= 8008;
	private static String SSLCERT 		= "";
	private static String SSLPASSWORD 	= "";

    public static void main(String[] args) throws Exception {
    	if(args!=null && args.length>0) {
    		System.out.println("HTTPServer Class, this is a lightweight HTTP server which require only a JRE/JDK installed (JDK 6+).");
    		System.out.println("Default port is 8008 and default uri is /rnif/partner1");
			System.out.println("To override these values use -D with PORT or URI as follow:");
			System.out.println("java -DPORT=8009 -DURI=\"/rnif/partner3\" -DREPLY=\"/<ok/>\" -DSSLCERT=httpdlight.jks -DSSLPASSWORD=password -jar httpdlight-0.1.jar");
    	} else {
    		HttpServer server;
	    	String uri 			= URI;
	    	int port 			= PORT;
	    	String sslCert 		= SSLCERT;
	    	String sslPassword 	= SSLPASSWORD;
	    	
	    	if(System.getProperty("URI")!=null && !System.getProperty("URI").equals("")) {
	    		uri = System.getProperty("URI");
	    		System.out.println("Found overridden uri: " + uri);
	    	}
	    	if(System.getProperty("PORT")!=null && !System.getProperty("PORT").equals("")) {
	    		port = Integer.parseInt(System.getProperty("PORT"));
	    		System.out.println("Found overridden port: " + port);
	    	}

	    	if(System.getProperty("SSLCERT")!=null && !System.getProperty("SSLCERT").equals("")) {
	    		sslCert = System.getProperty("SSLCERT");
	    		System.out.println("Found overridden SSLCERT: " + sslCert);
	    		if(System.getProperty("SSLPASSWORD")!=null && !System.getProperty("SSLPASSWORD").equals("")) {
	    			sslPassword = System.getProperty("SSLPASSWORD");
	    			System.out.println("Found overridden SSLPASSWORD: " + sslPassword);
	    		}
	    	}
	    	System.out.println("Starting HTTPServer at port "+ port + ", URI " + uri + (!sslCert.equals("")?" (with SSL)":""));
	        
	        if(sslCert.equals("")) {
	        	server = HttpServer.create(new InetSocketAddress(port), 0);
    		} else {
    			server = HttpsServer.create(new InetSocketAddress(port), 0);
    			((HttpsServer)server).setHttpsConfigurator(getSSLconfigurator(sslCert, sslPassword));
    		}
	        String replyHandler = System.getProperty("REPLYHANDLER");
			if(replyHandler!=null && !replyHandler.equals("")) {
	        	System.out.println("Using Reply handler " + replyHandler);
	        	server.createContext(uri, new InternalHandler(replyHandler));
	        } else {
	        	server.createContext(uri, new InternalHandler());
	        	System.out.println("Using default Reply handler");
	        }
	        server.setExecutor(null);
	        server.start();
	        System.out.println("Started!");
    	}
    }
    
    static private HttpsConfigurator getSSLconfigurator(String jks, String password) throws Exception {
    	SSLContext sslContext = SSLContext.getInstance("TLS");
        char[] keystorePassword = password.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(jks), keystorePassword);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, keystorePassword);
        sslContext.init(kmf.getKeyManagers(), null, null);
        HttpsConfigurator configurator = new HttpsConfigurator(sslContext);
        return configurator;
    }
    
    static class InternalHandler implements HttpHandler {
    	String className;
    	HTTPHandler handler;
    	
        @Override
        public void handle(HttpExchange exchange) throws IOException {
        	if(className!=null) {
				try {
					Class<?> c = Class.forName(className);
					handler = (HTTPHandler) c.newInstance();
				} catch (Exception e) {
					e.printStackTrace();
					handler = null;
				}
        	} else { 
        		handler = new HTTPBasicHandler();
        	}
        	
        	URI uri;
        	Map<String, String> query = null;
        	Map<String, String> headers;
        	String method;
        	String body;
        	HTTPResponse response;
        	
        	uri = exchange.getRequestURI();
        	headers = new HashMap<String, String>();
    		for (Entry<String, List<String>> entry : exchange.getRequestHeaders().entrySet()) {
	            headers.put(entry.getKey(), entry.getValue().get(0));
	        }
    		
    		body 		= inputStreamToString(exchange.getRequestBody());
    		method 		= exchange.getRequestMethod();
        	response 	= handler.handle(uri, method, query, headers, body);
        	
        	if(response.getHeaders() != null) {
	        	for (Map.Entry<String, List<String>> entry : response.getHeaders().entrySet()) {
	    			String key = entry.getKey();
	    			if(key!=null) {
						exchange.getResponseHeaders().set(key, entry.getValue().get(0));
	    			}
	    		}
        	}
        	exchange.sendResponseHeaders(handler.getStatusCode(), response.getBody().length());
    		OutputStream os = exchange.getResponseBody();
    		os.write(response.getBody().getBytes());
    		os.close();
        }
        
         
        public InternalHandler(String className) {
        	this.className = className;
        }
        
        public InternalHandler() {
        } 
    }
    
	/**
	 * Utility to convert InputStream to String
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static String inputStreamToString(InputStream is) throws IOException {
		final int bufferSize = 1024;
		final char[] buffer = new char[bufferSize];
		final StringBuilder out = new StringBuilder();
		Reader in = new InputStreamReader(is, "UTF-8");
		for (; ; ) {
		    int rsz = in.read(buffer, 0, buffer.length);
		    if (rsz < 0)
		        break;
		    out.append(buffer, 0, rsz);
		}
		return out.toString();
		
	}
	
	static public Properties getProperties(String filename) throws Exception {
		Properties prop = new Properties();
		System.out.println("INFO: Looking for " + filename + " ...");
		InputStream inputStream = HTTPServer.class.getClassLoader().getResourceAsStream(filename);
		if(inputStream==null) {
			inputStream = new FileInputStream(new File(filename));
		}
		prop.load(inputStream);
		System.out.println("INFO: File found");
		return prop;
	}

}
