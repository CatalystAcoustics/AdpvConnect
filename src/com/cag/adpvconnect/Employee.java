/**
 * 
 */
package com.cag.adpvconnect;

/**
 * @author pamelamarengo
 *
 */
public class Employee {

	private String id;
	private String associateId;
	private String positionId;
	private String visualId;
	private String firstName;
	private String lastName;
	

	/**
	 * @return the visualId
	 */
	public String getVisualId() {
		return visualId;
	}
	/**
	 * @param visualId the visualId to set
	 */
	public void setVisualId(String visualId) {
		this.visualId = visualId;
	}
	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}
	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}
	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getAssociateId() {
		return associateId;
	}
	public void setAssociateId(String associateId) {
		this.associateId = associateId;
	}
	public String getPositionId() {
		return positionId;
	}
	public void setPositionId(String positionId) {
		this.positionId = positionId;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
}
