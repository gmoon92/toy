package com.gmun.springaop.proxy4;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfileImple implements Profile{

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private Object target;
	private Method method;
	private Object[] args;
	
	private long staTime; //시작하는 시점 계산
	private long endTime; //프로그램이 끝나는 시점 계산
	
	@Override
	public void getProcessInfo(Object target, Method method, Object[] args) {
		this.target = target;
		this.method = method;
		if(args == null) {
			args = new Object[] {};
		}
		this.args 	= args;
	}
	
	@Override
	public void startTime() {
		logger.info("START TIME IS : {}", getTime());
		this.staTime = System.currentTimeMillis();
		targetInfo();
	}

	@Override
	public void endTime() {
		this.endTime = System.currentTimeMillis();
		long runTime = ( endTime - staTime )/1000;
		logger.info("END TIME IS : {} RUNTIME : {} 초", getTime(), runTime);
	}

	private String getTime() {
		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
		return sdf.format(date);
	}
	
	private void targetInfo() {
		String clazzName = this.target.getClass().getName();
		String methodName = this.method.getName();
		logger.info("\n  targetInfo -> \n Class Name : {} Method Name : {}", clazzName, methodName);
		logger.info("\n Params : \n");
		for(int i=0; i<args.length; i++) {
			logger.info((String) args[i]);
		}
	}
    
}