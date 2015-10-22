package org.daisy.util.xml;

public class ClipTime {

	private final Double timeInMs;

	public ClipTime() {
		timeInMs = null;
	}
	
	public ClipTime(double timeInMs) {
		this.timeInMs = new Double(timeInMs);
	}

	public double getTimeInMs() {
		if(notSet()) {
			return 0;
		} else {
			return timeInMs;
		}
	}
	
	public double getTimeInMsRounded() {
		return Math.round(this.getTimeInMs());
	}

	public boolean notSet() {
		if(this.timeInMs == null) {
			return true;
		} else {
			return false;
		}
	}

	public ClipTime add(ClipTime timeToAdd) {
		//this.setTimeInMs(this.getTimeInMs() + timeToAdd.getTimeInMs());
		return new ClipTime(this.getTimeInMs() + timeToAdd.getTimeInMs());
	}

	public ClipTime subtract(ClipTime timeToSubtract) {
		//this.setTimeInMs(this.getTimeInMs() - timeToSubtract.getTimeInMs());
		return new ClipTime(this.getTimeInMs() - timeToSubtract.getTimeInMs());
	}
}
