package com.taozeyu.calico.resource;

import java.io.File;

public abstract class AbstractTemplate extends ResourceFileWithHead {

	protected AbstractTemplate(File resourceFile) {
		super(resourceFile);
	}
}
