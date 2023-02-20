package com.boomi.proserv.httpd.server;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Map;


public class HTTPReplyAndForwardHandler extends HTTPBasicHandler implements HTTPHandler {

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
	public HTTPResponse handle(URI uri, String method, Map<String, String> query, Map<String, String> headers, String body) {

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
		if(forwardBody!=null && mapBody!=null) {
			forwardBody = replaceBody(forwardBody, mapBody);
		}
		final String finalforwardBody = forwardBody;

		url = properties.getProperty("forward.url");
		if(url!=null && !url.equals("")) {
			final int forwardSleep = Integer.valueOf(properties.getProperty("forward.sleep"));

			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(forwardSleep);
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
							log("\t" + entry.getKey() + ":"+ entry.getValue());
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

		int replySleep = 0;
		try {
			replySleep = Integer.valueOf(properties.getProperty("reply.sleep"));
			Thread.sleep(replySleep);
		} catch (Exception e) {}
		HTTPResponse httpResponse = new HTTPResponse(replyBody);
		httpResponse.setHeadersFlat(getHeaders());
		return httpResponse;
	}


	@Override
	public int getStatusCode() {
		return Integer.parseInt(properties.getProperty("reply.status"));
	}

	public Map<String, String> getHeaders() {
		String headersStr = properties.getProperty("reply.headers");
		return getMapFromString(headersStr, ",");
	}

}
