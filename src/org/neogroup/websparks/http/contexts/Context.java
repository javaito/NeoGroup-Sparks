
package org.neogroup.websparks.http.contexts;

import org.neogroup.websparks.http.HttpRequest;
import org.neogroup.websparks.http.HttpResponse;

public abstract class Context {

    private final String path;

    public Context(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public abstract void onContext (HttpRequest request, HttpResponse response);
}