package se_tpb_speechgen2.audio;

public class ClipTime {

	private Double timeInMs = null;

	public ClipTime() {
		timeInMs = null;
	}
	
	public ClipTime(double timeInMs) {
		this.timeInMs = timeInMs;
	}
	
	public ClipTime(ClipTime clipToCopy) {
		this.timeInMs = clipToCopy.getTimeInMs();
	}

	public double getTimeInMs() {
		if(notSet()) {
			return 0;
		} else {
			return timeInMs;
		}
	}

	public void setTimeInMs(double timeInMs) {
		this.timeInMs = timeInMs;
	}

	public boolean notSet() {
		if(this.timeInMs == null) {
			return true;
		} else {
			return false;
		}
	}

	public ClipTime add(ClipTime timeToAdd) {
		this.setTimeInMs(this.getTimeInMs() + timeToAdd.getTimeInMs());
		return this;
	}

	public ClipTime subtract(ClipTime timeToSubtract) {
		this.setTimeInMs(this.getTimeInMs() - timeToSubtract.getTimeInMs());
		return this;
	}
}
