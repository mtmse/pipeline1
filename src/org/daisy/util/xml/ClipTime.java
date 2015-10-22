package org.daisy.util.xml;

public class ClipTime {

	private final Double timeInMs;

	public ClipTime() {
		timeInMs = null;
	}
	
	public ClipTime(double timeInMs) {
		this.timeInMs = new Double(timeInMs);
	}
	
//	public ClipTime(ClipTime clipToCopy) {
//		this.timeInMs = new Double(clipToCopy.getTimeInMs());
//	}

	public double getTimeInMs() {
		if(notSet()) {
			return 0;
		} else {
			return timeInMs;
		}
	}

//	public void setTimeInMs(double timeInMs) {
//		this.timeInMs = new Double(timeInMs);
//	}

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
