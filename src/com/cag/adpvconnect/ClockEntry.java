package com.cag.adpvconnect;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class ClockEntry {

	private String employeeId;
	private LocalDateTime recordDateTime;
	private LocalDateTime creationDateTime;
    private LocalDateTime effectiveDateTime;
    private boolean clockOut;
    
	/**
	 * @return the employee
	 */
	public String getEmployeeId() {
		return employeeId;
	}
	/**
	 * @param employee the employee to set
	 */
	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}
	/**
	 * @return the recordDateTime
	 */
	public LocalDateTime getRecordDateTime() {
		return recordDateTime;
	}
	/**
	 * @param recordDateTime the recordDateTime to set
	 */
	public void setRecordDateTime(LocalDateTime recordDateTime) {
		this.recordDateTime = recordDateTime;
	}
	/**
	 * @return the creationDateTime
	 */
	public LocalDateTime getCreationDateTime() {
		return creationDateTime;
	}
	/**
	 * @param creationDateTime the creationDateTime to set
	 */
	public void setCreationDateTime(LocalDateTime creationDateTime) {
		this.creationDateTime = creationDateTime;
	}
	/**
	 * @return the effectiveDateTime
	 */
	public LocalDateTime getEffectiveDateTime() {
		return effectiveDateTime;
	}
	/**
	 * @param effectiveDateTime the effectiveDateTime to set
	 */
	public void setEffectiveDateTime(LocalDateTime effectiveDateTime) {
		this.effectiveDateTime = effectiveDateTime;
	}
	/**
	 * @return the clockOut
	 */
	public boolean isClockOut() {
		return clockOut;
	}
	/**
	 * @param clockOut the clockOut to set
	 */
	public void setClockOut(boolean clockOut) {
		this.clockOut = clockOut;
	}

}
