/**
 * 
 */
package com.cag.adpvconnect;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.adp.marketplace.connection.configuration.ClientCredentialsConfiguration;
import com.adp.marketplace.connection.configuration.ConnectionConfiguration;
import com.adp.marketplace.connection.core.ADPAPIConnectionFactory;
import com.adp.marketplace.connection.core.ClientCredentialsConnection;
import com.adp.marketplace.connection.exception.ConnectionException;
import com.adp.marketplace.connection.utils.SSLUtils;
import com.adp.marketplace.connection.vo.Token;
import com.adp.marketplace.demo.client.auth.authcode.utils.ClientUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author pamelamarengo
 *
 */
public class AdpParser {

	private static ClientUtils CLIENT_UTILS_INSTANCE = ClientUtils.getInstance();
	private static ADPAPIConnectionFactory CONNECTION_FACTORY_INSTANCE = ADPAPIConnectionFactory.getInstance();
	// CONFIG FOR CLIENT CREDENTIALS CODE
	private static final String clientID = "da86831e-c566-4fab-974b-a568b49f38aa";
	private static final String clientSecret = "321aed8e-5a31-4114-ad56-8ed46d137057";
	// file path has to follow patterns based of Windows or Unix
	private static final String sslCertPath = "\\certs\\keystorepk.jks";
	private static final String storePassword = "changeit";
	private static final String keyPassword = "changeit";
	private static final String tokenServerURL = "https://accounts.adp.com/auth/oauth/v2/token";
	private static final String apiRequestURL = "https://api.adp.com/hr/v2";
	private static final String clockRequestURL = "https://accounts.adp.com/time/v2/workers/MFQ002043/team-time-cards?$filter=teamTimeCards/timeCards/associateOID eq ";
//	private static final String grantType = "client_credentials";
	private static final String HOST_NAME = "https://api.adp.com";
	private static final int MAX_NUMBER_OF_ENTRIES = 25;
	private static final int MAX_RETRIES = 300;
	private Token token = null;

//S	private static final Logger logger = LoggerFactory.getLogger("AdpvParser");

	/**
	 * Constructor
	 */
	public AdpParser() {
	}

	/**
	 * Retrieves the employees from ADP
	 * 
	 * @return
	 * @throws AdpvConnectException
	 */
	public List<Employee> getEmployees() throws AdpvConnectException {
		List<Employee> employees = new ArrayList<Employee>();

		// CloseableHttpResponse httpResponse =
		// getUrl("https://api.adp.com/hr/v2/workers?$top=5");
		// getUrl("https://api.adp.com/hr/v2/workers?$top=5", token.getAccess_token());
		// /hr/v2/workers?$filter=workers/workerStatus/statusCode/codeValue%20eq%20'xxxxxx'
//			String response = getUrl("https://api.adp.com/hr/v2/workers?$filter=workers/workerStatus/statusCode/codeValue%20eq%20'Active'",  token.getAccess_token());
//			String response = getUrl("https://api.adp.com/hr/v2/workers?$filter=workers/workAssignments/payrollGroupCode%20eq%20'MFG'",  token.getAccess_token());
		String response = getUrl("https://api.adp.com/hr/v2/workers");
		employees = getEmployeesFromJson(response);
		// ?$filter=workers/workerID/idValue eq '1A9HUHNNN'
		// clientCredentialsConnection.connect();

		return employees;
	}

	public List<Employee> getEmployeesTemp() throws AdpvConnectException {
		List<Employee> employees = new ArrayList<Employee>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("employees.csv"));
			Employee employee = null;
			String line = null;
			String[] emp = null;

			while ((line = reader.readLine()) != null) {
				emp = line.split(",");
				employee = new Employee();
				employee.setId(emp[0]);
				employee.setAssociateId(emp[1]);
				employee.setFirstName(emp[2]);
				employee.setLastName(emp[3]);
				employees.add(employee);
			}
		} catch (Exception e1) {
			throw new AdpvConnectException(e1.getMessage(), this.getClass().getName());
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw new AdpvConnectException(e.getMessage(), this.getClass().getName());
			}
			;
		}

		return employees;
	}

	private Token getToken() throws AdpvConnectException {
		if (null == token) {
		ConnectionConfiguration connectionConfiguration = getConnectionConfiguration();

		// create connection
		ClientCredentialsConnection clientCredentialsConnection = null;
		try {
			clientCredentialsConnection = (ClientCredentialsConnection) CONNECTION_FACTORY_INSTANCE
					.createConnection(connectionConfiguration);

			// set connection configuration object on connection
			clientCredentialsConnection.setConnectionConfiguration(connectionConfiguration);

			// invoke connect to acquire Access Token
			clientCredentialsConnection.connect();

			// at this time this connection must have token
			token = clientCredentialsConnection.getToken();

			// alternate flow - no token returned in connection
			String errorResponse = clientCredentialsConnection.getErrorResponse();
			if (null != errorResponse && !errorResponse.isEmpty() && !errorResponse.isBlank()) {
				throw new AdpvConnectException(errorResponse, this.getClass().getName());
			}
		} catch (ConnectionException e) {
			try {
				if (null != clientCredentialsConnection) {
					clientCredentialsConnection.disconnect();
				}
			} catch (ConnectionException e1) {
				throw new AdpvConnectException(e1.getMessage(), this.getClass().getName());
			}
			throw new AdpvConnectException(e.getMessage(), this.getClass().getName());
		} finally {
			try {
				if (null != clientCredentialsConnection) {
					clientCredentialsConnection.disconnect();
				}
			} catch (ConnectionException e2) {
				throw new AdpvConnectException(e2.getMessage(), this.getClass().getName() +"getConnection");
			}
		}
		}
		return token;
	}

	/**
	 * Retrieves the time info from ADP, filters the list based on the employees in
	 * Visual and returns it.
	 * 
	 * @param vEmployees
	 * @return
	 * @throws com.cag.adpvconnect.AdpvConnectException
	 */
	public ArrayList<ClockEntry> getClockEntries(ArrayList<Employee> vEmployees) throws AdpvConnectException {
		ArrayList<ClockEntry> clockEntries = new ArrayList<ClockEntry>();
		ClockEntryManager clockEntryMgr = null;
		int numEntries = 0;
		String url1 = "https://api.adp.com/time/v2/workers/G5FRE3KNVC57ZFAZ/team-time-cards";
		String url2 = "?$skip=";
		String url3 = "&$top=";
		// leave this but it's not implemented yet at ADP
		// String url4 = "&$expand=dayEntries";
		String url = null;
		String response = null;
		boolean done = false;
		int retries = 0;

		while (!done && retries < MAX_RETRIES) {

			try {
				if (retries == 0) {
					url = url1;
				} else {
					url = url1 + url2 + numEntries + url3 + MAX_NUMBER_OF_ENTRIES;
				}
				response = getUrl(url);
				
				clockEntryMgr = getClockEntriesFromJson(response, vEmployees);
				
				if (clockEntryMgr.isCompleted()) {
					done = true;
				}

				if (clockEntryMgr.getNumberOfEntries() > 0) {

					clockEntries.addAll(clockEntryMgr.getClockEntries());
				}
				numEntries = numEntries + clockEntryMgr.getNumberOfEntriesSent();
				retries++;
			} catch (Exception e) {
				done = true;
			}
		}
		return clockEntries;
	}

	/**
	 * Original version - SAVE - will be going to clock-entries API
	 * 
	 * @return
	 * @throws com.cag.adpvconnect.AdpvConnectException
	 */
	public ArrayList<ClockEntry> getClockEntriesOriginal() throws com.cag.adpvconnect.AdpvConnectException {
		ArrayList<ClockEntry> clockEntries = new ArrayList<ClockEntry>();

		// getUrl("https://api.adp.com/hr/v2/workers?$top=5", token.getAccess_token());
		// /hr/v2/workers?$filter=workers/workerStatus/statusCode/codeValue%20eq%20'xxxxxx'
//			String response = getUrl("https://api.adp.com/hr/v2/workers?$filter=workers/workerStatus/statusCode/codeValue%20eq%20'Active'",  token.getAccess_token());
//			String response = getUrl("https://api.adp.com/hr/v2/workers?$filter=workers/workAssignments/payrollGroupCode%20eq%20'MFG'",  token.getAccess_token());
		String response = getUrl("https://api.adp.com/hr/v2/clockEntries");
		clockEntries = getClockEntriesFromJsonOriginal(response);
		// ?$filter=workers/workerID/idValue eq '1A9HUHNNN'
		// clientCredentialsConnection.connect();
		// CloseableHttpResponse httpResponse =
		// getUrl("https://api.adp.com/hr/v2/workers?$top=5");

		return clockEntries;
	}

	/**
	 * Retrieves the clock entries from a list of employees - SAVE - use when
	 * clock-entries API when available
	 * 
	 * @param employees
	 * @return
	 * @throws com.cag.adpvconnect.AdpvConnectException
	 */
	public ArrayList<ClockEntry> getClockEntriesFromList(ArrayList<Employee> employees)
			throws com.cag.adpvconnect.AdpvConnectException {
		ArrayList<ClockEntry> clockEntries = new ArrayList<ClockEntry>();
		Employee curEmployee = null;
		ClockEntry clockEntry = null;
		JsonObject jsonObject = null;
		JsonArray results = null;
		String response = null;
		String entryDateTime = null;
		LocalDateTime dateTime = null;
		LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.of(0, 0));
		LocalDate today = LocalDate.now();
		String url = null;

		JsonParser jParser = new JsonParser();

		for (Employee employee : employees) {
			curEmployee = employee;
			clockEntry = new ClockEntry();
			clockEntry.setEmployeeId(curEmployee.getVisualId());

			// /time/v2/workers/{aoid}/team-time-cards?$filter=teamTimeCards/timeCards/associateOID
			// eq '{aoid}' and
			// timeCards/timePeriod/startDate eq '2018-11-26' (today)

			url = clockRequestURL + curEmployee.getAssociateId() + "' and timeCards/timePeriod/startDate eq "
					+ today.toString();
//			response = getUrl("https://api.adp.com/hr/v2/" + curEmployee.getAssociateId() + "/clock-entries",
//					getConnection().getAccess_token());

			// https://api.adp.com/hr/v2/workers/clock-entries
			response = getUrl(url);

			jsonObject = jParser.parse(response).getAsJsonObject();

			results = jsonObject.getAsJsonArray("clockEntries");

			// status =
			// jsonObject.getAsJsonObject("workers").getAsJsonObject("workerStatus").getAsJsonObject("statusCode").get("codeValue").getAsString();
			for (int i = 0; i < results.size(); i++) {

				entryDateTime = results.get(i).getAsJsonObject().getAsJsonObject("clockEntries").get("entryDateTime")
						.getAsString();

				dateTime = LocalDateTime.parse(entryDateTime);

				if (dateTime.isAfter(todayStart)) {
					clockEntry.setCreationDateTime(LocalDateTime.parse(entryDateTime));

					entryDateTime = jsonObject.getAsJsonObject("clockEntries").get("deviceDateTime").getAsString();
					dateTime = LocalDateTime.parse(entryDateTime);
					clockEntry.setRecordDateTime(dateTime);

					clockEntries.add(clockEntry);
				}
			}
		}
		return clockEntries;
	}

	/**
	 * Parses the clock entries from Json. Save - going to clock entries api
	 * 
	 * @param jsonString
	 * @return
	 * @throws AdpvConnectException
	 */
	public String getURIFromJson(String jsonString) throws AdpvConnectException {
		String uri = null;
		;
		JsonParser jParser = new JsonParser();

		try {
			JsonObject jsonObject = jParser.parse(jsonString).getAsJsonObject();
			uri = jsonObject.getAsJsonObject("confirmMessage").getAsJsonArray("resourceMessages").get(0)
					.getAsJsonObject().getAsJsonObject("resourceLink").get("href").getAsString();

			if (null != uri) {
				uri = HOST_NAME + uri;
			}
		} catch (Exception e) {
			throw new AdpvConnectException(e.getMessage(), this.getClass().getName());
		}

		return uri;
	}

	/**
	 * Parses the clock entries from Json. SAVE - going to clock entries api
	 * 
	 * @param jsonString
	 * @return
	 * @throws AdpvConnectException
	 */
	public ArrayList<ClockEntry> getClockEntriesFromJsonOriginal(String jsonString) throws AdpvConnectException {
		ArrayList<ClockEntry> clockEntries = new ArrayList<ClockEntry>();
		ClockEntry clockEntry = null;
//		JsonArray clockResults = null;
		JsonObject clockJsonObject = null;
		JsonElement jElement = null;
		JsonParser jParser = new JsonParser();
		String name = null;
		int index = -1;

		JsonObject jsonObject = jParser.parse(jsonString).getAsJsonObject();
		JsonArray results = jsonObject.getAsJsonArray("workers");

		try {

			for (int i = 0; i < results.size(); i++) {
				clockEntry = new ClockEntry();

				jElement = results.get(0);
				clockJsonObject = jElement.getAsJsonObject();

				// TODO: clock entry id = visual employee id

				// status =
				// clockJsonObject.getAsJsonObject("workerStatus").getAsJsonObject("statusCode").get("codeValue").getAsString();

				// employee.setAssociateId(empJsonObject.get("associateOID").getAsString());

				// empResults = empJsonObject.getAsJsonArray("workAssignments");
				// employee.setPositionId(empResults.get(0).getAsJsonObject().get("positionID").getAsString());
				// empResults = empJsonObject.getAsJsonArray("person");
				// name =
				// empResults.get(0).getAsJsonObject().get("legalName").getAsJsonObject().get("formattedName").getAsString();

				// formatted name style: "Abrams, Joshua Russell"
				// index = name.indexOf(",");
				// last = name.substring(0,index);
				// first = name.substring(index + 2);

				// employee.setFirstName(first);
				// employee.setLastName(last);
				// employees.add(employee);
			}
		} catch (Exception e) {
			throw new AdpvConnectException(e.getMessage(), this.getClass().getName());
		}

		return clockEntries;
	}

	/**
	 * Parses the clock entries from Json. SAVE - going to clock entries api
	 * 
	 * @param jsonString
	 * @return
	 * @throws AdpvConnectException
	 */
	public ClockEntryManager getClockEntriesFromJson(String jsonString, ArrayList<Employee> vEmployees)
			throws AdpvConnectException {

		ClockEntryManager clockEntryMgr = new ClockEntryManager();
		ArrayList<ClockEntry> clockEntries = new ArrayList<ClockEntry>();
		ClockEntry clockEntry = null;
		Employee employee = null;
		JsonObject timecardObj = null;
		JsonArray timeCards = null;
		JsonArray dailyTotals = null;
		JsonParser jParser = new JsonParser();
		LocalDateTime dailyTotalDateTime = null;
		String lastName = null;
		String firstName = null;
		String recordDate = null;
		boolean found = false;
		
		String entryDate = null;
		
		JsonObject jsonObject = jParser.parse(jsonString).getAsJsonObject();
		boolean completed = Boolean.valueOf(jsonObject.getAsJsonObject("meta").get("completeIndicator").toString());
		clockEntryMgr.setCompleted(completed);
		JsonArray results = jsonObject.getAsJsonArray("teamTimeCards");
//		LocalDateTime yesterday = LocalDateTime.now().minus(1, ChronoUnit.DAYS);

		if (null != results) {
			clockEntryMgr.setNumberOfEntries(0);
			clockEntryMgr.setNumberOfEntriesSent(results.size());
			clockEntryMgr.setClockEntries(clockEntries);
			
			// logger.debug("------------------ AdpParser - ADP Entries -------------------");
			try {

				for (int i = 0; i < results.size(); i++) {
					clockEntry = new ClockEntry();

					timecardObj = results.get(i).getAsJsonObject();
					
					timeCards = timecardObj.getAsJsonArray("timeCards");

					lastName = timeCards.get(timeCards.size()-1).getAsJsonObject()
							.getAsJsonObject("personLegalName").get("familyName1").getAsString().toUpperCase();
					firstName = timeCards.get(timeCards.size()-1).getAsJsonObject()
							.getAsJsonObject("personLegalName").get("givenName").getAsString().toUpperCase();
					
					// TODO: Remove this section after debug
					dailyTotals = timeCards.get(timeCards.size() -1).getAsJsonObject().getAsJsonArray("dailyTotals");
					JsonElement entryDateElement = null;
					
					if (!(null == dailyTotals)) {
						entryDateElement = dailyTotals.get(dailyTotals.size() - 1).getAsJsonObject()
							.get("entryDate");
					}
					
					if (null == entryDateElement) {
						entryDate = "";
					} else {
						entryDate = entryDateElement.getAsString();
					}
					// logger.debug(firstName + " " + lastName +": " + entryDate);
					// TODO: End remove
					
					found = false;

					for (int j = 0; !found && j < vEmployees.size(); j++) {
						employee = vEmployees.get(j);

						if (employee.getLastName().toUpperCase().equals(lastName)) {
							if (employee.getFirstName().toUpperCase().equals(firstName)) {

								found = true;

								try {

									dailyTotals = timeCards.get(timeCards.size() -1).getAsJsonObject().getAsJsonArray("dailyTotals");
									recordDate = dailyTotals.get(dailyTotals.size() - 1).getAsJsonObject()
											.get("entryDate").getAsString() + "T00:00:00";

									dailyTotalDateTime = LocalDateTime.parse(recordDate);

			//						if (ChronoUnit.HOURS.between(yesterday,dailyTotalDateTime) > 0) {
										clockEntry = new ClockEntry();
										clockEntry.setEmployeeId(employee.getId());
										clockEntry.setRecordDateTime(dailyTotalDateTime);
										clockEntries.add(clockEntry);
			//						}

								} catch (Exception e) {
									// Do nothing. If we can't get the clock in time there likely isn't one. Keep
									// going.
								}
							}
						}

					}
					clockEntryMgr.setNumberOfEntries(clockEntries.size());
					clockEntryMgr.setClockEntries(clockEntries);
				}

			} catch (Exception e) {
				throw new AdpvConnectException(e.getMessage(), this.getClass().getName());
			}
		} else {

		}

		return clockEntryMgr;
	}

	/**
	 * Retrieves the connection config
	 * 
	 * @return
	 * @throws AdpvConnectException
	 */
	private ConnectionConfiguration getConnectionConfiguration() throws AdpvConnectException {

		// create an instance of ClientCredentialsConfiguration
		ClientCredentialsConfiguration connectionConfiguration = new ClientCredentialsConfiguration();

		// ACQUIRING CONNECTION FOR CLIENT CREDENTIALS

		// get client credentials configuration properties
		try {

			Properties properties = CLIENT_UTILS_INSTANCE.getConfigProperties();
			properties.setProperty("clientID", clientID);
			properties.setProperty("clientSecret", clientSecret);
			properties.setProperty("sslCertPath", sslCertPath);
			properties.setProperty("storePassword", storePassword);
			properties.setProperty("keyPassword", keyPassword);
			properties.setProperty("tokenServerURL", tokenServerURL);
			properties.setProperty("apiRequestURL", apiRequestURL);

			CLIENT_UTILS_INSTANCE.mapPropertiesToClientCredentialsConfiguration(properties, connectionConfiguration);

		} catch (Exception e) {
			throw new AdpvConnectException(e.getMessage(), this.getClass().getName());
		}

		return connectionConfiguration;

	}

	/**
	 * Retrieves from an http GET call. Connection must be live and token retrieved.
	 * 
	 * @param url
	 * @return
	 * @throws ConnectionException
	 * @throws AdpvConnectException
	 */
	public String getUrl(String url) throws AdpvConnectException {

		CloseableHttpClient httpClient = null;
		CloseableHttpResponse httpResponse = null;
		String responseString = null;
		HttpEntity entity = null;

		try {
			if (null == token) {
				getToken();
			}
			httpClient = SSLUtils.getInstance().getHttpsClient(getConnectionConfiguration());
			// create POST request to acquire access token
			HttpGet get = new HttpGet(url);
			get.setHeader("Authorization", "Bearer " + token.getAccess_token());

			// TODO: Enable the following code when clock-entries API is available from ADP
			/*
			 * @SuppressWarnings("deprecation") HttpParams params = new BasicHttpParams();
			 * params.setParameter("$filter",
			 * "workers/workAssignments/payrollGroupCode%20eq%20'MFG'");
			 * get.setParams(params);
			 */

			/*
			 * URI uri = new URIBuilder(get.getURI()).addParameter("$filter",
			 * "workers/workAssignments/payrollGroupCode eq 'MFG'").build();
			 * get.setURI(uri);
			 */
			// URI uri = new URIBuilder(get.getURI()).build();
			// get.setURI(uri);
			httpResponse = httpClient.execute(get);

			entity = httpResponse.getEntity();
			responseString = EntityUtils.toString(entity, "UTF-8");

		} catch (Exception e) {
			throw new AdpvConnectException(e.getMessage(), this.getClass().getName());
		} finally {
			try {
				if (httpResponse != null) {
					httpResponse.close();
				}
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
				throw new AdpvConnectException(e.getMessage(), this.getClass().getName());
			}
		}

		return responseString;
	}

	/**
	 * Retrieves the json from the response
	 * 
	 * @param jsonString
	 * @return
	 * @throws AdpvConnectException
	 */
	public List<Employee> getEmployeesFromJson(String jsonString) throws AdpvConnectException {
		List<Employee> employees = new ArrayList<Employee>();
		Employee employee = null;
		JsonArray empResults = null;
		JsonObject empJsonObject = null;
		JsonElement jElement = null;
		JsonParser jParser = new JsonParser();
//		String status;
		String name = null;
		String first = null;
		String last = null;
		int index = -1;

		JsonObject jsonObject = jParser.parse(jsonString).getAsJsonObject();
		JsonArray results = jsonObject.getAsJsonArray("workers");

		try {

			for (int i = 0; i < results.size(); i++) {
				employee = new Employee();

				jElement = results.get(0);
				empJsonObject = jElement.getAsJsonObject();

				// status = empJsonObject.getAsJsonObject("workerStatus").getAsJsonObject("statusCode").get("codeValue")
				//		.getAsString();

				// TODO: if (null != status && !(status.equals("Terminated"))) {

				employee.setAssociateId(empJsonObject.get("associateOID").getAsString());

				empResults = empJsonObject.getAsJsonArray("workAssignments");
				employee.setPositionId(empResults.get(0).getAsJsonObject().get("positionID").getAsString());
				empResults = empJsonObject.getAsJsonArray("person");
				name = empResults.get(0).getAsJsonObject().get("legalName").getAsJsonObject().get("formattedName")
						.getAsString();

				// formatted name style: "Abrams, Joshua Russell"
				index = name.indexOf(",");
				last = name.substring(0, index);
				first = name.substring(index + 2);

				employee.setFirstName(first);
				employee.setLastName(last);
				employees.add(employee);
			}
		} catch (Exception e) {
			throw new AdpvConnectException(e.getMessage(), this.getClass().getName());
		}

		return employees;
	}

}
