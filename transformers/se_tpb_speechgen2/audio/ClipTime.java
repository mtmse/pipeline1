package se_tpb_speechgen2.audio;

public class ClipTime {

	private Double timeInMs;

	public ClipTime() {
		timeInMs = null;
	}
	
	public ClipTime(double timeInMs) {
		this.timeInMs = timeInMs;
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
}
