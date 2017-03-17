
package org.neogroup.sparks.web.processors;

import org.neogroup.httpserver.HttpHeader;
import org.neogroup.httpserver.HttpRequest;
import org.neogroup.httpserver.HttpResponse;
import org.neogroup.httpserver.HttpResponseCode;
import org.neogroup.sparks.processors.Processor;
import org.neogroup.sparks.web.commands.WebCommand;
import org.neogroup.sparks.web.routing.RouteAction;
import org.neogroup.util.MimeTypes;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class WebProcessor extends Processor<WebCommand, HttpResponse> {

    private final Map<String, Method> actionMethods;

    public WebProcessor() {
        actionMethods = new HashMap<>();
        for (Method method : getClass().getDeclaredMethods()) {
            RouteAction webAction = method.getAnnotation(RouteAction.class);
            if (webAction != null) {
                actionMethods.put(webAction.name(), method);
            }
        }
    }

    @Override
    public final HttpResponse process(WebCommand command) {

        HttpResponse response = null;
        HttpRequest request = command.getRequest();
        String action = command.getWebAction();
        try {
            Method method = actionMethods.get(action);
            if (method != null) {
                response = onBeforeAction(action, request);
                if (response == null) {
                    response = (HttpResponse)method.invoke(this, request);
                    response = onAfterAction(action, request, response);
                }
            }
            else {
                response = onActionNotFound(action, request);
            }
        }
        catch (Throwable throwable) {
            response = onActionError(action, request, throwable);
        }
        return response;
    }

    protected HttpResponse onBeforeAction (String action, HttpRequest request) {
        return null;
    }

    protected HttpResponse onAfterAction (String action, HttpRequest request, HttpResponse response) {
        return response;
    }

    protected HttpResponse onActionError (String action, HttpRequest request, Throwable throwable) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream printer = new PrintStream(out);
        throwable.printStackTrace(printer);
        byte[] body = out.toByteArray();
        HttpResponse response = new HttpResponse();
        response.setResponseCode(HttpResponseCode.HTTP_INTERNAL_ERROR);
        response.addHeader(HttpHeader.CONTENT_TYPE, MimeTypes.TEXT_PLAIN);
        response.setBody(body);
        return response;
    }

    protected HttpResponse onActionNotFound (String action, HttpRequest request) {
        HttpResponse response = new HttpResponse();
        response.setResponseCode(HttpResponseCode.HTTP_NOT_FOUND);
        response.addHeader(HttpHeader.CONTENT_TYPE, MimeTypes.TEXT_PLAIN);
        response.setBody("Action \"" + action + "\" found in controller \"" + this + "\" !!");
        return response;
    }
}
