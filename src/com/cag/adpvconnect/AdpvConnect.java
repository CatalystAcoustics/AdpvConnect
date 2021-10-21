package com.cag.adpvconnect;

import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdpvConnect {
	private AdpParser adpParser;
	private static VisualParser visualParser;
	private static final Logger logger = LoggerFactory.getLogger("AdpvConnect");

	public static void main(String[] args) throws AdpvConnectException {
		ArrayList<Employee> visualEmployees = null;
//		ArrayList<Employee> adpEmployees = null;
//		ArrayList<Employee> finalEmployees = null;
		ArrayList<ClockEntry> clockEntries = null;

		int timeoutInMinutes = 60;
		boolean done = false;
		
		if (args.length > 0) {
			try {
				int tmpTimeout = Integer.valueOf(args[1].toString());
				timeoutInMinutes = tmpTimeout;
			} catch (Exception e) {
				// Do nothing. Leave the timeout the same as it was.
			}
		}

		try {
			while (!done) {
				
				// get the list of Visual employees to check
				visualParser = new VisualParser();
				visualEmployees = visualParser.getEmployeeList();

				// get the list of ADP employees
				AdpParser adpParser = new AdpParser();
				
				// TODO: Enable the following code when clock entries api is available from ADP
				// get the employee list from ADP
//    			adpEmployees = (ArrayList<Employee>) adpParser.getEmployeesTemp();

				// Compare the employee list from ADP and the visual list to see who should be
				// updated
//    			finalEmployees = getUpdateableEmployees(visualEmployees, adpEmployees);

				// Retrieve the clock entries from the final employee list
//    			clockEntries = adpParser.getClockEntriesFromList(finalEmployees);
//    			clockEntries = adpParser.getClockEntriesFromList(adpEmployees);

				clockEntries = adpParser.getClockEntries(visualEmployees);

				if (null != clockEntries && clockEntries.size() > 0) {
					visualParser.updateClockEntriesDB(clockEntries);
				} else {
					logger.warn("No clock entries available to update the Visual database.");
				}
				
				logger.debug("----------------------- Sleeping for " + timeoutInMinutes
						+ " minutes ----------------------");
				Thread.sleep(timeoutInMinutes *60 * 1000);
			}
		} catch (Exception e) {
			throw new AdpvConnectException(e.getMessage());
		}

	}

	public void processTimeEntries() throws AdpvConnectException {

	}

	/**
	 * @return the adpParser
	 */
	public AdpParser getAdpParser() {
		return adpParser;
	}

	private static ArrayList<Employee> getUpdateableEmployees(ArrayList<Employee> visualEmployees,
			ArrayList<Employee> adpEmployees) {
		ArrayList<Employee> finalEmployees = new ArrayList<Employee>();
		ArrayList<Employee> compare1 = null;
		ArrayList<Employee> compare2 = null;
		boolean visGrtr = false;

		// Presume ADP is the greater size or if equal
		compare1 = visualEmployees;
		compare2 = adpEmployees;

		// compare visual employees to adp employee list to get assoc and position IDs
		// put any IDs from ADP that aren't in the visual list into a log
		if (visualEmployees.size() > adpEmployees.size()) {
			// Visual database could be out of sync w the active/not active
			// or someone new was hired that's not in the Visual database
			// System.out.println("Visual database has more employees than returned from
			// ADP")
			visGrtr = true;
			compare1 = adpEmployees;
			compare2 = visualEmployees;

		}

		boolean found = false;

		// Compare the largest list to the shortest list
		for (Employee employee1 : compare1) {

			found = false;

			for (Employee employee2 : compare2) {
				if (employee1.getLastName().equals(employee2.getLastName())) {
					if (employee1.getFirstName().equals(employee2.getFirstName())) {
						found = true;
						finalEmployees.add(employee1);
						break;
					}
				}

			}

			if (!found) {
				String db = "Visual";
				if (visGrtr) {
					db = "ADP";
				}

				StringBuffer sb = new StringBuffer();
				sb.append("Employee ");
				sb.append(employee1.getFirstName());
				sb.append(" ");
				sb.append(employee1.getLastName());
				sb.append(" wasn't found in ");
				sb.append(db);
			}

			if (visualEmployees.size() > adpEmployees.size()) {
				// Visual database could be out of sync w the active/not active
				// or someone new was hired that's not in the Visual database
				System.out.println(
						"Visual database has more employees than returned from ADP. Check active/not active in Visual or if someone new was hired and isn't in Visual.");

				logger.warn(
						"Visual database has more employees than returned from ADP. Check active/not active in Visual or if someone new was hired and isn't in Visual.");
			} else if (visualEmployees.size() < adpEmployees.size()) {
				// Visual database may be out of sync
				System.out.println("There are more ADP employees than is in Visual. Check for new hires.");
				logger.warn("There are more ADP employees than is in Visual. Check for new hires.");
			}

		}

		return finalEmployees;
	}

}
