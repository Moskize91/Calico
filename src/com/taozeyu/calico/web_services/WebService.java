package com.taozeyu.calico.web_services;

import com.taozeyu.calico.GlobalConfig;
import com.taozeyu.calico.generator.Router;
import fi.iki.elonen.NanoHTTPD;

/**
 * Created by taozeyu on 15/10/9.
 */
public class WebService extends NanoHTTPD {

    private final Router router;

    public WebService(Router router) {
        super(GlobalConfig.instance().getInt("port", 8080));
        this.router = router;
    }

    @Override
    public Response serve(IHTTPSession session) {
        synchronized (this) {
            return handleRequest(session);
        }
    }

    private Response handleRequest(IHTTPSession session) {
        return new Response("hello world!");
    }
}
