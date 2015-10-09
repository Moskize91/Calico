package com.taozeyu.calico.web_services;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by taozeyu on 15/10/9.
 */
public class WebService extends NanoHTTPD {

    public WebService(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        return new Response("hello world!");
    }


    public static void main(String[] args) throws Exception {
        WebService service = new WebService(8080);
        service.start();
    }

}
