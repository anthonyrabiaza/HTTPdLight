package com.boomi.proserv.httpd.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class HTTPReplyAndForwardHandler implements HTTPHandler {

	private static final int WATERMARK_MAX = 50;
	private static Properties properties;
	private static int counter = -1;
	private static Date firstTrigger;


	public HTTPReplyAndForwardHandler() {
		if(properties == null) {
			try {
				properties = HTTPServer.getProperties("HTTPReplyAndForwardHandler.properties");
			} catch (Exception e) {
				System.err.println("Error opening the properties file " + e.getMessage());
			}
		}
	}

	@Override
	public String handle(URI uri, Map<String, String> query, Map<String, String> headers, String body) {

		newTrigger();

		//RECEIVING
		log("########## " + new Date() + " Receiving request" + " ##########");
		log("Receiving request: " + uri);
		log("Headers:");
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			log("\t" + entry.getKey() + ":"+ entry.getValue());
		}
		log("Body:\n"+ body);
		log("########## " + new Date() + " End of Receiving request" + " ##########");
		//END RECEIVING

		final String url;
		String replyBody;
		Map<String, String> mapBody = null;

		//BUILDING REPLY
		String contentType = headers.get("Content-type");
		if("application/x-www-form-urlencoded".equals(contentType)) {
			mapBody = getMapFromString(body, "&");
		}

		replyBody = properties.getProperty("reply.body");
		if(mapBody!=null) {
			replyBody = replaceBody(replyBody, mapBody);
		} 

		log("########## " + new Date() + " Replying " + " ##########");
		log("Reply will be " + replyBody);
		log("########## " + new Date() + " End of Replying " + " ##########");
		//END BUILDING REPLY

		//FORWARD
		String forwardBody = properties.getProperty("forward.body");
		if(forwardBody!=null) {
			forwardBody = replaceBody(forwardBody, mapBody);
		}
		final String finalforwardBody = forwardBody;

		url = properties.getProperty("forward.url");
		if(url!=null && !url.equals("")) {
			final int sleep = Integer.valueOf(properties.getProperty("forward.sleep"));

			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(sleep);
						log("########## " + new Date() + " Forwarding " + " ##########");
						log("Forward will be " + finalforwardBody);
						URL urlTarget = new URL(url);
						HttpURLConnection httpConnection = (HttpURLConnection) urlTarget.openConnection();
						httpConnection.setUseCaches(false);
						httpConnection.setDoOutput(true);
						httpConnection.setRequestMethod("POST");
						httpConnection.setFixedLengthStreamingMode(finalforwardBody.length());
						String headersStr = properties.getProperty("forward.headers");
						Map<String, String> forwardHeaders = getMapFromString(headersStr, ",");
						for (Map.Entry<String, String> entry : forwardHeaders.entrySet()) {
							httpConnection.setRequestProperty(entry.getKey(),  entry.getValue());
						}
						OutputStreamWriter writer = new OutputStreamWriter(httpConnection.getOutputStream());
						writer.write(finalforwardBody);
						writer.flush();
						httpConnection.disconnect();
						log("########## " + new Date() + " End of Forwarding " + " ##########");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});  
			thread.start();
		}
		// END FORWARD

		return replyBody;
	}

	private Map<String, String> getMapFromString(String body, String separator) {
		Map<String, String> mapBody;
		mapBody = new HashMap<String, String>();
		String[] keyValues = body.split(separator);
		for (int i = 0; i < keyValues.length; i++) {
			String[] keyValue = keyValues[i].split("=");
			mapBody.put(keyValue[0], keyValue[1]);
		}
		return mapBody;
	}

	@Override
	public int getStatusCode() {
		return Integer.parseInt(properties.getProperty("reply.status"));
	}

	@Override
	public Map<String, String> getHeaders() {
		String headersStr = properties.getProperty("reply.headers");
		return getMapFromString(headersStr, ",");
	}

	private String replaceBody(String body, Map<String, String> replacements) {
		for (Map.Entry<String, String> entry : replacements.entrySet()) {
			body = body.replaceAll("@@" + entry.getKey() +"@@", entry.getValue());
		}

		return body;
	}

	private void log(String str) {
		if(!"true".equalsIgnoreCase(System.getProperty("BENCHMARK"))) {
			System.out.println(str);
		}
	}

	static private void newTrigger() {
		if("true".equalsIgnoreCase(System.getProperty("BENCHMARK"))) {
			counter++;
			String screenDisplay = "";
			int displayCounter = counter%50;

			if(firstTrigger == null) {
				firstTrigger = new Date();
			}

			Date now = new Date();
			long diff = (now.getTime() - firstTrigger.getTime())/1000;

			//After 2 minutes idle, reset
			if(diff>120) {
				counter = 0;
				firstTrigger = new Date();
			}

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
