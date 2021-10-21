/**
 * 
 */
package com.cag.adpvconnect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pamelamarengo
 *
 */
@SuppressWarnings("serial")
public class AdpvConnectException extends Exception {
	
private static final Logger logger = LoggerFactory.getLogger("AdpvConnect");
	
	
	AdpvConnectException(String msg) {
		super(msg);
		logger.error(msg);
	}
	
	AdpvConnectException(String msg, String source) {
		super(source + ": " + msg);
		Logger logger2 = LoggerFactory.getLogger(source + ": " + msg );
		logger2.error(msg);
	}

}
