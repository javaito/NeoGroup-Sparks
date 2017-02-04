
package org.neogroup.sparks.processors;

import org.neogroup.net.httpserver.HttpRequest;
import org.neogroup.net.httpserver.HttpResponse;
import org.neogroup.sparks.commands.WebCommand;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@ProcessorComponent (commands = {WebCommand.class})
public abstract class WebProcessor extends Processor<WebCommand, HttpResponse> {

    private final Map<String, Method> actionMethods;

    public WebProcessor() {
        actionMethods = new HashMap<>();
        for (Method method : getClass().getDeclaredMethods()) {
            WebAction webAction = method.getAnnotation(WebAction.class);
            if (webAction != null) {
                actionMethods.put(webAction.name(), method);
            }
        }
    }

    @Override
    public HttpResponse processCommand(WebCommand command) {

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
        return null;
    }

    protected HttpResponse onActionNotFound (String action, HttpRequest request) {
        return null;
    }
}