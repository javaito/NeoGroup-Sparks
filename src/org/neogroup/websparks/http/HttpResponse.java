
package org.neogroup.websparks.http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.neogroup.websparks.http.contexts.HttpContextInstance;
import org.neogroup.websparks.properties.Properties;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HttpResponse {
    
    public static final String SERVER_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
    
    private static DateFormat dateFormatter;
    
    static {
        dateFormatter = new SimpleDateFormat(SERVER_DATE_FORMAT);
    }
    
    private final HttpExchange exchange;
    private int responseCode;
    private ByteArrayOutputStream body;
    private boolean headersSent;
    
    public HttpResponse() {
        this(HttpResponseCode.OK);
    }
    
    public HttpResponse(int responseCode) {
        this.exchange = HttpContextInstance.getInstance().getExchange();
        this.responseCode = responseCode;
        this.body = new ByteArrayOutputStream();
        this.headersSent = false;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
    
    public void setBody(String body) {
        setBody(body.getBytes());
    }
    
    public void setBody(byte[] body) {
        this.body.reset();
        try { this.body.write(body); } catch (Exception ex) {}
    }
    
    public Headers getHeaders() {
        return exchange.getResponseHeaders();
    }
    
    public void addHeader (String headerName, String headerValue) {
        exchange.getResponseHeaders().add(headerName, headerValue);
    }
    
    public void clearHeaders () {
        exchange.getResponseHeaders().clear();
    }

    public void send () {
        sendHeaders();
        writeContents();
        closeConnection();
    }
    
    public void write (String text) {
        write(text.getBytes());
    }
    
    public void write (byte[] bytes) {
        try { this.body.write(bytes); } catch (Exception ex) {}
    }
    
    public void flush () {
        sendHeaders(0);
        writeContents();
    }

    private void sendHeaders () {
        sendHeaders(body != null? body.size() : -1);
    }

    private void sendHeaders (long contentLength) {
        if (!headersSent) {
            addHeader(HttpHeader.DATE, dateFormatter.format(new Date()));
            addHeader(HttpHeader.SERVER, Properties.get(Properties.SERVER_NAME_PROPERTY));
            try { exchange.sendResponseHeaders(responseCode, contentLength); } catch (IOException ex) {}
            headersSent = true;
        }
    }

    private void writeContents () {
        try { body.writeTo(exchange.getResponseBody()); } catch (IOException ex) {}
        try { exchange.getResponseBody().flush(); } catch (Exception ex) {}
        body.reset();
    }

    private void closeConnection () {
        try { exchange.getResponseBody().close(); } catch (Exception ex) {}
        try { body.close(); } catch (Exception ex) {}
    }
}