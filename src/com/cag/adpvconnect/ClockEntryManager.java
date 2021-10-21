package com.cag.adpvconnect;

import java.util.ArrayList;

public class ClockEntryManager {
	
	int numberOfEntries = -1;
	int numberOfEntriesSent = -1;
	ArrayList<ClockEntry> clockEntries = null;
	boolean completed = false;
	
	
	/**
	 * @return the numberOfEntries
	 */
	public int getNumberOfEntries() {
		return numberOfEntries;
	}
	/**
	 * @param numberOfEntries the numberOfEntries to set
	 */
	public void setNumberOfEntries(int numberOfEntries) {
		this.numberOfEntries = numberOfEntries;
	}
	/**
	 * @return the numberOfEntriesSent
	 */
	public int getNumberOfEntriesSent() {
		return numberOfEntriesSent;
	}
	/**
	 * @param numberOfEntriesSent the numberOfEntriesSent to set
	 */
	public void setNumberOfEntriesSent(int numberOfEntriesSent) {
		this.numberOfEntriesSent = numberOfEntriesSent;
	}
	/**
	 * @return the clockEntries
	 */
	public ArrayList<ClockEntry> getClockEntries() {
		return clockEntries;
	}
	/**
	 * @param clockEntries the clockEntries to set
	 */
	public void setClockEntries(ArrayList<ClockEntry> clockEntries) {
		this.clockEntries = clockEntries;
	}
	/**
	 * @return the completed
	 */
	public boolean isCompleted() {
		return completed;
	}
	/**
	 * @param completed the completed to set
	 */
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

}
