package com.taozeyu.calico;

import com.taozeyu.calico.web_services.WebService;
import fi.iki.elonen.ServerRunner;

/**
 * Created by taozeyu on 15/10/9.
 */
public class Service {

    public static void main(String[] args) {
        ServerRunner.executeInstance(new WebService(8080));
    }
}
