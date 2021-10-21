# ClientCredConnectionSampleApp - Stand Alone Java Application that uses ADPConnection Library

The ClientCredConnectionSampleApp is a standalone java application that uses ADPConnection Library to simplify process of authentication, authorization and connecting to ADP Marketplace gateway.

## Installation

Unzip ClientCredConnectionSampleApp and copy to {Home}/workspace/ so that it can be run as standalone java application

Import the ClientCredConnectionSampleApp to your Eclipse IDE 

Do a one time setup for client certificates if not already done.

NOTE: 

Certificates bundled in libraries will only work for ADP Sandbox environment

Refer ONE-TIME_CERTS_SETUP.md for jks key generation and import to cacerts

Verify and(or) Update clientConfig.properties for ClientCredConnectionSampleApp:
 	
sslCertPath  - absolute path to the provided 'keystore.jks' or your own generated xxxxx.jks file

storePassword - if not using default password (adpadp10) this has to be updated based on what was used during creating keystore and truststore 

keyPassword - if not using default password (adpadp10) this has to be updated based on what was used during creating keystore and truststore
 		               
NOTE: for sslCertPath choose the appropriate path structure based on Windows/Unix/Linux

For running the stand alone java application: ClientCredentialsSampleApp
     
## Build the sample app - ClientCredentialsSampleApp

Execute below commands in command line from the ClientCredConnectionSampleApps directory 
     
$ mvn clean install -e

Generates a ClientCredConnectionSampleApp-1.0.0.jar in /target folder
     
ECLIPSE:     

Assuming your eclipse is already configured to use maven:

Right click on pom.xml -> 

Click Run As -> Run Configurations -> to create a new configuration 
     
## Maven Configuration Setup

Provide below details for new configuration tabs:
     
Main Tab

Name: Name of the Sample App: 'ClientCredentialsSampleApp'

Goals: clean install -e

Check relevant Check boxes: Update Snapshots, Debug Output 

JRE Tab: use default or use appropriate option

Refresh Tab: check Refresh

Common Tab: Check Debug and Run for 'Display in favorite menu'
    
Click Apply and Run 

## Usage

**Steps to initialize connection**

Initialize Client Credentials Configuration based on Grant type **client_credentials**

Create ClientCredentialsConnection with initialized configuration

Invoke connect() on Connection to get Access Token from clientCredentialsConnection

Invoke getAccessToken() on Connection to obtain Access Token 

Invoke getErrorResponse() on Connection to obtain error details in case of no Access Token

** Create Client Credentials Connection**

// create an instance of ClientCredentialsConfiguration
    ClientCredentialsConfiguration connectionConfiguration = new ClientCredentialsConfiguration();
    
    // map properties
    Properties properties = CLIENT_UTILS_INSTANCE.getConfigProperties();
    CLIENT_UTILS_INSTANCE.getConfigProperties().mapPropertiesToClientCredentialsConfiguration(properties, connectionConfiguration);
   
    // create connection
    clientCredentialsConnection = (ClientCredentialsConnection) 
		CONNECTION_FACTORY_INSTANCE.createConnection(connectionConfiguration);
						
	// set connection configuration object on connection
	clientCredentialsConnection.setConnectionConfiguration(connectionConfiguration);
	
	// invoke connect to acquire Access Token
	clientCredentialsConnection.connect();
	
	// at this time this connection must have token
	Token token = clientCredentialsConnection.getToken()
	
	// alternate flow - no token returned in connection
	String errorResponse  = clientCredentialsConnection.getErrorResponse()


## Test ClientCredentialsConnectionSampleApp:
 
## Eclipse 
 
Select ClientCredConnectionSampleApp

From Menu -> Project -> Clean
 
Select /src/main/java ADPDemoClientCredentials.java and Run as Java Application 
 
/ClientCredConnectionSampleApp/src/main/java/com/adp/marketplace/demo/client/auth/clientcred/ADPDemoClientCredentials.java 

## Command Line

$ mvn package appassembler:assemble

$ sh target/appassembler/bin/clientcredConnectionSampleApp

Generates clientcredConnectionSampleApp for Unix and 

ClientcredConnectionSampleApp.bat for windows

## API Documentation

Documentation on the individual API calls

Library Documentation

file://{HOME}/workspace/ADPConnection/doc/index.html

file://{HOME}/workspace/ADPUserInfo/doc/index.html

**Sample App Documentation**

file://{HOME}/workspace/ClientCredConnectionSampleApp/doc/index.html

Additional documentation can also be found on the [ADP Developer Portal](https://developers.adp.com).

## Dependencies

This Sample application depends on the following libraries.

1.  commons-logging-1.2.jar
2.  commons-lang3-3.4.jar
3.  httpclient-4.5.1.jar
4.  httpcore-4.4.3.jar
5.  commons-codec-1.9.jar
6.  httpcore-osgi-4.4.4.jar
7.  httpcore-nio-4.4.4.jar
8.  gson-2.3.1.jar
9.  json-simple-1.1.1.jar
10. junit-4.12.jar
11. hamcrest-core-1.3.jar
12. slf4j-api-1.7.14.jar
13. **ADPConnection-1.0.0.jar**


## License
[Apache 2](http://www.apache.org/licenses/LICENSE-2.0)