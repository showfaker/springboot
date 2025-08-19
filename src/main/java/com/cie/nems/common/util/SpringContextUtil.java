package com.cie.nems.common.util;

import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

/**
 * 上下文获取工具类
 * 
 * @author shh
 *
 */
public class SpringContextUtil {
	public static final int EXIT_OK = 0;
	public static final int EXIT_FAIL = -1;
	
	private static ApplicationContext applicationContext;

	public static void setApplicationContext(ApplicationContext context) {
		applicationContext = context;
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public static Object getBean(String beanId) {
		return applicationContext.getBean(beanId);
	}
	
	public static void exit(int code) {
		int exitCode = SpringApplication.exit(applicationContext, (ExitCodeGenerator) () -> code);
		System.exit(exitCode);
	}
}
