package com.cag.adpvconnect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Visual database parser
 * @author pamelamarengo
 *
 */
public class VisualParser {

	private Connection conn = null;

	public boolean updateDB(ArrayList<Employee> employees) {

		boolean connectStatus = true;

		return connectStatus;
	}

	/**
	 * Retrieves the connection to the Visual database
	 * @return
	 * @throws AdpvConnectException
	 */
	private Connection getConnection() throws AdpvConnectException {

		conn = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", "Sysadm");
		connectionProps.put("password", "SSIGoLive");
		String connectionUrl = "jdbc:sqlserver://10.150.110.83:1433;databaseName=SSI";
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch (ClassNotFoundException e1) {
			throw new AdpvConnectException(e1.getMessage(), this.getClass().getName());
		}
		try {
			conn = DriverManager.getConnection(connectionUrl, connectionProps);
		} catch (Exception e) {
			throw new AdpvConnectException(e.getMessage(), this.getClass().getName());
		}
		System.out.println("Connected to database");
		return conn;
	}

	/**
	 * Retrieves the employee list from the Visual database.
	 * @return
	 * @throws AdpvConnectException
	 */
	public ArrayList<Employee> getEmployeeList() throws AdpvConnectException {
		ArrayList<Employee> empList = new ArrayList<Employee>();
		String retrieveEmpStr = "SELECT ID, FIRST_NAME, LAST_NAME FROM EMPLOYEE WHERE ACTIVE = 'Y' AND LAST_NAME IS NOT NULL and FIRST_NAME IS NOT NULL AND LAST_NAME NOT LIKE '501%' AND FIRST_NAME NOT LIKE '%TEAM' ORDER BY LAST_NAME, FIRST_NAME";
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		Employee employee = null;

		if (null == conn) {
			conn = getConnection();
		}

		try {
			pstmt = conn.prepareStatement(retrieveEmpStr);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				employee = new Employee();
				employee.setId(rs.getString(1));
				employee.setFirstName(rs.getString(2));
				employee.setLastName(rs.getString(3));
				empList.add(employee);
			}
			rs.close(); // Close the ResultSet 5
			pstmt.close(); // Close the PreparedStatement 6
			closeConnection();
		} catch (Exception e) {
			throw new AdpvConnectException(e.getMessage(), this.getClass().getName());
		} finally {

			try {
				if (null != rs) {
					rs.close();

					if (null != pstmt) {
						pstmt.close();
					}
				}
			} catch (Exception e) {
				throw new AdpvConnectException(e.getMessage(), this.getClass().getName());
			}
		}
		return empList;
	}

	/**
	 * Updates the clock entries in the visual database from a list
	 * @param finalEmployees
	 * @return
	 * @throws AdpvConnectException
	 */
	public boolean updateClockEntries(ArrayList<Employee> finalEmployees) throws AdpvConnectException {
		boolean succeeded = true;

		try {
//			clockEntries.forEach(clockEntry -> updateClockEntriesDB(clockEntry));
		} catch (Exception e) {
			throw new AdpvConnectException(e.getMessage(), this.getClass().getName());
		} finally {
			closeConnection();
		}

		return succeeded;
	}

	/** 
	 * Updates the Visual database with the clock entries
	 * @param clockEntries
	 * @return
	 * @throws AdpvConnectException
	 */
	public boolean updateClockEntriesDB(ArrayList<ClockEntry> clockEntries) throws AdpvConnectException {
		String cleanOldClockEntryStr = "TRUNCATE TABLE CLOCK_ENTRY";
//		String clockInStr = "INSERT INTO CLOCK_ENTRY (EMPLOYEE_ID,LAST_CLOCK_IN) VALUES (?,?)";
//		String clockOutStr = "INSERT INTO CLOCK_ENTRY (EMPLOYEE_ID,LAST_CLOCKOUT) VALUES (?,?)";
		String clockStr = "INSERT INTO CLOCK_ENTRY (EMPLOYEE_ID,CREATION_DATE,DEVICE_DATE) VALUES (?,?,?)";

		LocalDateTime ldt = null;

		PreparedStatement pstmt = null;
		ClockEntry clockEntry = null;
		ZoneId zoneId = ZoneId.systemDefault();

		try {
			if (null == conn) {
				conn = getConnection();
			}
			pstmt = conn.prepareStatement(cleanOldClockEntryStr);
//			ldt = LocalDateTime.now().with(LocalTime.of(0, 0));
//			pstmt.setDate(1, java.sql.Date.valueOf(ldt.toString()));
			pstmt.executeUpdate();

			// sql date format is YYYY-MM-DD HH:MM:SS.MMM

			for (int j = 0; j < clockEntries.size(); j++) {
				clockEntry = clockEntries.get(j);
				pstmt = conn.prepareStatement(clockStr);
				pstmt.setString(1, clockEntry.getEmployeeId());
				if (null == clockEntry.getCreationDateTime()) {
					clockEntry.setCreationDateTime(LocalDateTime.now());
				}
				pstmt.setTimestamp(2, Timestamp.valueOf(clockEntry.getCreationDateTime()));
				pstmt.setTimestamp(3, Timestamp.valueOf(clockEntry.getRecordDateTime()));
				
//				pstmt.setTimestamp(2, new Timestamp(clockEntry.getCreationDateTime().atZone(zoneId).toEpochSecond()));
				//pstmt.setTimestamp(3, new Timestamp(clockEntry.getRecordDateTime().atZone(zoneId).toEpochSecond()));

				pstmt.executeUpdate(); // Update the database
			}
		} catch (Exception e) {
			throw new AdpvConnectException(e.getMessage(), this.getClass().getName());
		} finally {
			try {
				pstmt.close();
			} catch (SQLException e1) {
				throw new AdpvConnectException(e1.getMessage(), this.getClass().getName());
			}

		}
		return true;

	}

	/**
	 * Safely closes the connection to the Visual database
	 * @return
	 * @throws AdpvConnectException
	 */
	public boolean closeConnection() throws AdpvConnectException {
		boolean closed = true;

		if (null != conn) {
			try {
				conn.close();
			} catch (Exception e) {
				try {
					conn.close();
				} catch (Exception e1) {
					throw new AdpvConnectException(e.getMessage(), this.getClass().getName());
				}
			} finally {
				conn = null;
			}
		}
		return closed;
	}
}