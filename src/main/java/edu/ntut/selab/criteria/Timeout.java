package edu.ntut.selab.criteria;

import edu.ntut.selab.XMLReader;
import edu.ntut.selab.util.Config;

public class Timeout {
	private long timeout = 0;
	public Timeout() {
		long configTimeout = Config.TIMEOUT_SECOND;
		if(configTimeout != 0) {
			timeout = configTimeout * 1000; // config�̭���timeout��minute�A�n�ഫ��ms
		}
	}
	
	public boolean check(long currentExecuteTime) {
		if(timeout == 0)
			return false;
		else {
			if(currentExecuteTime > timeout)
				return true;
			else
				return false;
		}
	}
	
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	public long getTimeout() {
		return this.timeout;
	}

}
