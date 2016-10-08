package com.bupt.indoorPosition.uti;

import java.util.HashMap;

public class Global {
	public static enum LoginStatus {
		NOT_LOGINED, LOGINED
	}

	public static LoginStatus loginStatus = LoginStatus.NOT_LOGINED;
	// cookie
	public static HashMap<String, String> cookieContainer = new HashMap<String, String>();
}
