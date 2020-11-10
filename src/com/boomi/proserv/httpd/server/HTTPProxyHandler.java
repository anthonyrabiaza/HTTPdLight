package com.boomi.proserv.httpd.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Map;

public class HTTPProxyHandler extends HTTPBasicHandler implements HTTPHandler {

	public HTTPProxyHandler() {
		if(properties == null) {
			try {
				properties = HTTPServer.getProperties("HTTPProxyHandler.properties");
			} catch (Exception e) {
				System.err.println("Error opening the properties file " + e.getMessage());
			}
		}
	}

	@Override
	public HTTPResponse handle(URI uri, String method, Map<String, String> query, Map<String, String> headers, String body) {

		newTrigger();
		
		boolean skip = skip(uri);

		//RECEIVING
		log(skip, "########## " + new Date() + " Receiving request" + " ##########");
		log(skip, "Receiving request: " + uri);
		log(skip, "Headers:");
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			log(skip, "\t" + entry.getKey() + ":"+ entry.getValue());
		}
		if("".equals(body)){
			log(skip, "Empty body");
		} else {
			log(skip, "Body:\n"+ body);
		}
		log(skip, "########## " + new Date() + " End of Receiving request" + " ##########");
		//END RECEIVING

		final String url;
		String replyBody = "{ \"message\" : \"no reply\"} ";
		Map<String, String> mapBody = null;

		//PROXYING
		String forwardBody = properties.getProperty("proxy.body");
		if(forwardBody!=null) {
			forwardBody = replaceBody(forwardBody, mapBody);
		}
		final String finalforwardBody = forwardBody;

		url = properties.getProperty("proxy.url") + uri;

		try {
			log(skip, "########## " + new Date() + " Proxying " + " ##########");
			log(skip, "Proxying content will be " + finalforwardBody);
			URL urlTarget = new URL(url);
			HttpURLConnection httpConnection = (HttpURLConnection) urlTarget.openConnection();
			httpConnection.setRequestMethod(method);
			if(finalforwardBody!=null) {
				httpConnection.setUseCaches(false);
				httpConnection.setDoOutput(true);
				httpConnection.setFixedLengthStreamingMode(finalforwardBody.length());
			}
			//Adding received headers
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				log(skip, "\t" + entry.getKey() + ":"+ entry.getValue());
				httpConnection.setRequestProperty(entry.getKey(),  entry.getValue());
			}
			//Adding additional headers
			String headersStr = properties.getProperty("proxy.headers");
			Map<String, String> forwardHeaders = getMapFromString(headersStr, ",");
			for (Map.Entry<String, String> entry : forwardHeaders.entrySet()) {
				log(skip, "\t" + entry.getKey() + ":"+ entry.getValue());
				httpConnection.setRequestProperty(entry.getKey(),  entry.getValue());
			}
			
			log(skip, "Sending information to server (" + url + ")...");
			if(finalforwardBody!=null) {
				OutputStreamWriter writer = new OutputStreamWriter(httpConnection.getOutputStream());
				writer.write(finalforwardBody);
				writer.flush();
				log(skip, "Sent");
			} else {
				log(skip, "No information to send, waiting for reply");
			}
			
			int responseCode = httpConnection.getResponseCode();
			log(skip, "Response code:" + responseCode);
			BufferedReader br;
			if (100 <= responseCode && responseCode <= 399) {
				br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
			} else {
				br = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
			}
			StringBuilder sb = new StringBuilder();
			String output;
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
			replyBody = sb.toString();
			
			HTTPResponse httpResponse = new HTTPResponse(replyBody);
			httpResponse.setHeaders(httpConnection.getHeaderFields());
			
			httpConnection.disconnect();
			log(skip, "########## " + new Date() + " End of Proxying " + " ##########");
			
			return httpResponse;
		} catch (Exception e) {
			e.printStackTrace();
		}
		//END PROXYING

		return new HTTPResponse();
	}
	
	protected boolean skip(URI uri) {
		 String ignoreURIs = properties.getProperty("proxy.ignore.uri");
		 if(ignoreURIs.contains(uri.toString())) {
			 return true;
		 } else {
			 return false;
		 }
	}
	
	protected void log(boolean skip, String str) {
		if(!skip) {
			super.log(str);
		}
	}
}
