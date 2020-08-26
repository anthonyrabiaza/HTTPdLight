package com.boomi.proserv.httpd.server;

import java.net.URI;
import java.util.Date;
import java.util.Map;

public class HTTPBasicHandler implements HTTPHandler {

	@Override
	public String handle(URI uri, Map<String, String> query, Map<String, String> headers, String body) {
		log("########## " + new Date() + " Receiving request" + " ##########");
		log("Receiving request: " + uri);
		log("Headers:");
		for (Map.Entry<String, String> entry : headers.entrySet()) {
            log("\t" + entry.getKey() + ":"+ entry.getValue());
        }
		log("Body:\n"+ body);
		log("########## " + new Date() + " End of Receiving request" + " ##########");
		
		String reply = System.getProperty("REPLY");
		if(reply==null) {
			reply = "OK";
		}
		return reply;
	}
	
	public Map<String, String> getHeaders() {
		return null;
	}
	
	public int getStatusCode() {
		return 200;
	}
	
	private void log(String str) {
		if(!"true".equalsIgnoreCase(System.getProperty("BENCHMARK"))) {
			System.out.println(str);
		}
	}

}
