package com.boomi.proserv.httpd.server;

import java.net.URI;
import java.util.Map;

public interface HTTPHandler {
	public String handle(URI uri, Map<String, String> query, Map<String, String> headers, String body);
	public int getStatusCode();
	public Map<String, String> getHeaders();
}
