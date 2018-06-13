package com.boomi.proserv.httpd.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Iterator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * HTTPServer Class: this is a lightweight HTTP server used to Mock endpoints which require only a JRE/JDK installed (JDK 6+)
 * Default port is 8008 and default uri is /rnif/partner1 <br/>
 * To override these values use -D with PORT or URI as follow:
 * java -DPORT=8009 -DURI="/rnif/partner3" -DREPLY="<ok/>" -jar httpdlight-0.1.jar
 * @author anthony.rabiaza@gmail.com
 *
 */
public class HTTPServer {
	
	private static String URI 	= "/rnif/partner1"; // Should start with "/" 
	private static Integer PORT = 8008;
	private static String REPLY = "";

    public static void main(String[] args) throws Exception {
    	if(args!=null && args.length>0) {
    		System.out.println("HTTPServer Class, this is a lightweight HTTP server which require only a JRE/JDK installed (JDK 6+).");
    		System.out.println("Default port is 8008 and default uri is /rnif/partner1");
			System.out.println("To override these values use -D with PORT or URI as follow:");
			System.out.println("java -DPORT=8001 -DURI=\"/root\" -DREPLY=\"/<ok/>\" -jar httpdlight-0.1.jar");
    	} else {
	    	String uri = URI;
	    	int port = PORT;
	    	String reply = REPLY;
	    	if(System.getProperty("URI")!=null && !System.getProperty("URI").equals("")) {
	    		uri = System.getProperty("URI");
	    		System.out.println("Found overridden uri: " + uri);
	    	}
	    	if(System.getProperty("PORT")!=null && !System.getProperty("PORT").equals("")) {
	    		port = Integer.parseInt(System.getProperty("PORT"));
	    		System.out.println("Found overridden port: " + port);
	    	}
	    	if(System.getProperty("REPLY")!=null && !System.getProperty("REPLY").equals("")) {
	    		reply = System.getProperty("REPLY");
	    		System.out.println("Found overridden reply: " + reply);
	    	}
	    	System.out.println("Starting HTTPServer at port "+ port + ", URI " + uri);
	        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
	        server.createContext(uri, new StandardHandler(reply));
	        server.setExecutor(null); // creates a default executor
	        server.start();
	        System.out.println("Started!");
    	}
    }

    static class StandardHandler implements HttpHandler {
    	String reply;
        @Override
        public void handle(HttpExchange t) throws IOException {
        	System.out.println("########## " + new Date() + " Receiving request" + " ##########");
            String response = reply;
            t.sendResponseHeaders(200, response.length());
            System.out.println("Receiving request " + t.getRequestURI());
            System.out.println("Headers");
            for (Iterator<String> iterator = t.getRequestHeaders().keySet().iterator(); iterator.hasNext();) {
				String header = iterator.next();
				System.out.println("\t" + header + ":" + t.getRequestHeaders().get(header));
			}
            System.out.println("Body :\n"+inputStreamToString(t.getRequestBody()));
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            System.out.println("########## " + new Date() + " End of Receiving request" + " ##########");
        }
        
        public StandardHandler(String reply) {
        	this.reply = reply;
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

}
