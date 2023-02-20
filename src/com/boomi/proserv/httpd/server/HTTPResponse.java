package com.boomi.proserv.httpd.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPResponse {
	
	private String body;
	private Map<String, List<String>> headers;
	
	public final String getBody() {
		return body;
	}
	public final void setBody(String body) {
		this.body = body;
	}
	public final Map<String, List<String>> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}
	
	public void setHeadersFlat(Map<String, String> headers) {
		if(this.headers==null) {
			this.headers = new HashMap<String, List<String>>();
		}
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			List<String> list = new ArrayList<String>();
			list.add(entry.getValue());
			this.headers.put(entry.getKey(), list);
		}
	}
	
	public HTTPResponse() {
		this.body = "";
		this.headers = null;
	}
	public HTTPResponse(String body) {
		this.body = body;
		this.headers = null;
	}
}
