package com.gmun.springaop.proxy4;

import java.lang.reflect.Method;

public interface Profile {
	public void startTime();
	public void endTime();
	
	public void getProcessInfo(Object target, Method method, Object[] args);
}
