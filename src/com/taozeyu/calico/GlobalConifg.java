package com.taozeyu.calico;

import java.nio.charset.Charset;

public class GlobalConifg {

	private final Charset charset = Charset.forName("UTF-8");
	
	public static final GlobalConifg instance = new GlobalConifg();

	public Charset getCharset() {
		return charset;
	}
}
