package edu.ntut.selab.event;

import edu.ntut.selab.TimeHelper;
import edu.ntut.selab.entity.Device;

public class EventExecutor {
	private long waitingTime = 0;
	private Device device;
	
	public EventExecutor(Device device) {
		this.device = device;
		try {
			waitingTime = TimeHelper.getWaitingTime("eventSleepTimeSecond");
		} 
		catch(NumberFormatException e) {
			e.printStackTrace();
		}
	}
	public long getWaitingTime() {
		return waitingTime;
	}
	
	public void run(AndroidEvent event) {
		try {
			event.executeOn(device);
			TimeHelper.sleep(waitingTime);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
