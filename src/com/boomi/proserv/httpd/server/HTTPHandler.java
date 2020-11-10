package com.boomi.proserv.httpd.server;

import java.net.URI;
import java.util.Map;

public interface HTTPHandler {
	public HTTPResponse handle(URI uri, String method, Map<String, String> query, Map<String, String> headers, String body);
	public int getStatusCode();
}
