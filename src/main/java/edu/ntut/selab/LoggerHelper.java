package edu.ntut.selab;

import java.util.logging.Logger;

public class LoggerHelper {
	public static void warning(String msg) {
		Logger.getLogger("log").warning(msg);
	}
}
