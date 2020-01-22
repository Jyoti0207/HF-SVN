package com.hf.wc.integration.util;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.lcs.wc.util.FileLocation;
import com.lcs.wc.util.LCSProperties;

/**
 * This file is used to generate Logs.
 * 
 * @author "true" ITCINFOTECH
 * @version "true" 1.0
 */
public final class HFLogFileGenerator {
	/**
	 * String log4jFileNameProduct.
	 */
	private static String log4jFileNameProduct = LCSProperties.get("HF_Integration_Log4j_File");
	/** configuring and instantiating logger instance. */
	/**
	 * Logger logger.
	 */
	static final Logger logger = Logger.getLogger(HFLogFileGenerator.class);
	/**
	 * HFLogFileGenerator HFLogger.
	 */
	private static HFLogFileGenerator hfLogger;

	/**
	 * Constructor
	 */
	private HFLogFileGenerator() {
	}

	/**
	 * configureLog Method to Configure logs.
	 * 
	 * @param interfaceType
	 *            for interfaceType
	 */
	public static void configureLog(String interfaceType) {
		if ((!(hfLogger != null))) {
			hfLogger = new HFLogFileGenerator();
			String path = "";
			path = FileLocation.codebase + FileLocation.fileSeperator
					+ log4jFileNameProduct;
			PropertyConfigurator.configure(path);
			logger.info("******Logger Configured !!!!!!!!***");
			logger.info("@@@@@@@check for update@@@@");
		}
	}
}
