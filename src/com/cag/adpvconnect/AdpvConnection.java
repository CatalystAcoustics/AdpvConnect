package com.cag.adpvconnect;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import com.adp.marketplace.connection.configuration.ClientCredentialsConfiguration;
import com.adp.marketplace.connection.configuration.ConnectionConfiguration;
import com.adp.marketplace.connection.constants.Constants;
import com.adp.marketplace.connection.core.ClientCredentialsConnection;
import com.adp.marketplace.connection.exception.ConnectionException;
import com.adp.marketplace.connection.utils.ConnectionUtils;
import com.adp.marketplace.connection.utils.ConnectionValidatorUtils;
import com.adp.marketplace.connection.utils.SSLUtils;
import com.adp.marketplace.connection.vo.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection class as an extension of ADP ClientCredentialsConfiguration class
 * to manage HTTP calls.
 * 
 * @author pamelamarengo
 *
 */
public class AdpvConnection extends ClientCredentialsConnection {
	private static final Logger logger = LoggerFactory.getLogger("AdpvConnection");

	public AdpvConnection(ConnectionConfiguration connectionConfiguration) {
		super(connectionConfiguration);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Retrieves from an http GET call. Connection must be live and token retrieved.
	 * 
	 * @param url
	 * @return 
	 * @throws ConnectionException 
	 * @throws AdpvConnectException 
	 */
	public CloseableHttpResponse getUrl(String url) throws ConnectionException, AdpvConnectException {
		int responseStatusCode;

		StringBuffer sb = null;
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse httpResponse = null;

		try {
			ConnectionValidatorUtils validatorInstance = ConnectionValidatorUtils.getInstance();
			boolean isValid = validatorInstance.validate(this.getConnectionConfiguration());

			if (isValid) {

				// create a https client
				httpClient = SSLUtils.getInstance().getHttpsClient(getConnectionConfiguration());

				// create POST request to acquire access token
				HttpGet get = new HttpGet(url);
//				post.addHeader("User-Agent", Constants.CONNECTION_USER_AGENT);

				ConnectionUtils connectionInstance = ConnectionUtils.getInstance();

				// List<NameValuePair> nameValuePairs = connectionInstance.getNameValuePairs(this);

				// map Client credentials and grant types to post request
			//	 get.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				URIBuilder builder = new URIBuilder()
					    .setScheme("https")
					    .setHost("api.adp.com")
					    .setPath("/hr/v2/workers")
					    .addParameter("top", "5");
					builder.toString();
					
					
				// get POST response
				httpResponse = httpClient.execute(get);

				if (httpResponse != null) {
					responseStatusCode = httpResponse.getStatusLine().getStatusCode();
					if (httpResponse.getStatusLine().getStatusCode() == Constants.HTTP_SUCCESS) {

						// set token
						/*
						 * token = ConnectionUtils.processTokenResponse(httpResponse);
						 * 
						 * this.setToken(token); setConnectionAlive(true);
						 * 
						 * this.tokenTimeTracker = (System.currentTimeMillis() / 1000.0) +
						 * token.getExpires_in();
						 * 
						 * HttpEntity entity = httpResponse.getEntity(); Header[] headers =
						 * httpResponse.getAllHeaders();
						 */
					} else {
						// isConnectionAlive = false;

						if (responseStatusCode == Constants.HTTP_CLIENT_ERROR
								|| responseStatusCode == Constants.HTTP_CLIENT_INVALID
								|| responseStatusCode == Constants.HTTP_SERVER_ERROR) {

							setErrorResponse(ConnectionUtils.processResponse(httpResponse));

							String httpStatusLine = httpResponse.getStatusLine().toString();
							if (StringUtils.isNotBlank(httpStatusLine)) {

								sb = new StringBuffer(httpStatusLine).append(" ").append(getErrorResponse());
								setErrorResponse(sb.toString());
							}
						}
					}
				}
			}

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
		return httpResponse;
	}
}
