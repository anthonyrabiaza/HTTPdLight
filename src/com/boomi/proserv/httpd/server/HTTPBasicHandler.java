package com.boomi.proserv.httpd.server;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HTTPBasicHandler implements HTTPHandler {

	protected static final int WATERMARK_MAX = 50;
	protected static int counter = -1;
	protected static Date firstTrigger;

	protected static Properties properties;

	@Override
	public HTTPResponse handle(URI uri, String method, Map<String, String> query, Map<String, String> headers, String body) {
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
		return new HTTPResponse(reply);
	}

	public int getStatusCode() {
		return 200;
	}

	protected void log(String str) {
		if(!"true".equalsIgnoreCase(System.getProperty("BENCHMARK"))) {
			System.out.println(str);
		}
	}

	protected Map<String, String> getMapFromString(String body, String separator) {
		Map<String, String> mapBody;
		mapBody = new HashMap<String, String>();
		if(!"".equals(body)){
			String[] keyValues = body.split(separator);
			for (int i = 0; i < keyValues.length; i++) {
				String[] keyValue = keyValues[i].split("=");
				mapBody.put(keyValue[0], keyValue[1]);
			}
		}
		return mapBody;
	}

	protected String replaceBody(String body, Map<String, String> replacements) {
		replacements.put("__counter__", getFormattedCounter());
		for (Map.Entry<String, String> entry : replacements.entrySet()) {
			body = body.replaceAll("@@" + entry.getKey() +"@@", entry.getValue());
		}

		return body;
	}

	protected String getFormattedCounter() {
		String counterFormat = properties.getProperty("counter.format");
		return String.format(counterFormat, counter);
	}

	protected boolean methodSendBody(String method) {
		return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
	}

	static protected void newTrigger() {
		if("true".equalsIgnoreCase(System.getProperty("BENCHMARK"))) {
			counter++;
			String screenDisplay = "";
			int displayCounter = counter%50;

			if(firstTrigger == null) {
				firstTrigger = new Date();
			}

			Date now = new Date();
			long diff = (now.getTime() - firstTrigger.getTime())/1000;

			//			//After 2 minutes idle, reset
			//			if(diff>120) {
			//				counter = 0;
			//				firstTrigger = new Date();
			//			}

			for (int i = 0; i < WATERMARK_MAX; i++) {
				screenDisplay += i<displayCounter?"=":i==displayCounter?">":" ";
			}

			long perf = diff==0?0:counter/diff;
			screenDisplay = "\rLoad: [" + screenDisplay + "] " + (counter+1) + " tx, " + perf + " tx/s, last tx at " + now + " ";
			try {
				System.out.write(screenDisplay.getBytes());
			} catch (IOException e) {
				System.out.print('#');
			}
		}
	}
}
