package com.events.kafkaservices;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.events.pojo.EventRequest;
import com.messagehub.bluemix.BluemixEnvironment;
import com.messagehub.bluemix.MessageHubCredentials;


public class CreateMessage {
	
    private static final String APP_NAME = "EventCreator";
    private static final String DEFAULT_TOPIC_NAME = "mdo-man-calc-topic2";
    private static final Logger logger = Logger.getLogger(CreateMessage.class);

    private static Thread producerThread = null;
    private static String resourceDir;
    

	
	public void createMessage(EventRequest req)
	{
		
        final String userDir = System.getProperty("user.dir");
        final Properties clientProperties = new Properties();

        String bootstrapServers = null;
        String adminRestURL = null;
        String apiKey = null;

        String topicName = DEFAULT_TOPIC_NAME;
        String user;
        String password;		
        
        try {
		
        logger.log(Level.INFO, "Running in Bluemix mode.");
        resourceDir = userDir + File.separator + APP_NAME + File.separator + "classes" ;

        System.out.println("Shajeer hiiii");
        MessageHubCredentials credentials = BluemixEnvironment.getMessageHubCredentials();

        bootstrapServers = stringArrayToCSV(credentials.getKafkaBrokersSasl());
        System.out.println("bootstrapServers:"+ bootstrapServers);
        adminRestURL = credentials.getKafkaRestUrl();
        System.out.println("adminRestURL:"+ adminRestURL);
        
        apiKey = credentials.getApiKey();
        user = credentials.getUser();
        password = credentials.getPassword();
		
		
        //inject bootstrapServers in configuration, for both consumer and producer
        clientProperties.put("bootstrap.servers", bootstrapServers);		
		
        
        Properties producerProperties = getHardcodedClientConfig(clientProperties, user, password);
        
        //checking if we can fetch properties from file instead of hardcoding in code
        Properties producerPropertiestemp = getClientConfiguration(clientProperties, "producer.properties", user, password);
        
        ProducerRunnable producerRunnable = new ProducerRunnable(producerProperties, topicName,req);
        producerThread = new Thread(producerRunnable, "Producer Thread");
        producerThread.start();
        
        } catch (Exception e) {
            logger.log(Level.ERROR, "Exception occurred, application will terminate", e);
            System.out.println("Error in createMessage : " + e);
        }
		
	}
	
    /*
     * Retrieve client configuration information, using a properties file, for
     * connecting to Message Hub Kafka.
     */
    public  Properties getClientConfiguration(Properties commonProps, String fileName, String user, String password) {
        Properties result = new Properties();
        InputStream propsStream;

        try {
        	System.out.println("file:"+resourceDir + File.separator + fileName);
            propsStream = new FileInputStream(resourceDir + File.separator + fileName);
            result.load(propsStream);
            propsStream.close();
        } catch (IOException e) {
        	System.out.println("error in IO");
            logger.log(Level.ERROR, "Could not load properties from file");
            return result;
        }

        result.putAll(commonProps);
        //Adding in credentials for MessageHub auth
        String saslJaasConfig = result.getProperty("sasl.jaas.config");
        saslJaasConfig = saslJaasConfig.replace("USERNAME", user).replace("PASSWORD", password);
        result.setProperty("sasl.jaas.config", saslJaasConfig);
        return result;
    }	
    
    /*Hardcoded values... change this to read from file
     * 
     */
    public Properties getHardcodedClientConfig(Properties commonProps,String user, String password)
    {
    	Properties result = new Properties();
    	result.putAll(commonProps);
    	String saslJaasConfig="org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + user + "\" password=\"" + password + "\";";
    	System.out.println("saslJaasConfig:"+saslJaasConfig);
        logger.log(Level.ERROR, "saslJaasConfig:"+saslJaasConfig);
        
        result.setProperty("sasl.jaas.config", saslJaasConfig);
        
        result.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        result.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        result.setProperty("client.id", "kafka-java-console-sample-producer");
        result.setProperty("security.protocol", "SASL_SSL");
        result.setProperty("sasl.mechanism", "PLAIN");
        result.setProperty("ssl.protocol", "TLSv1.2");
        result.setProperty("ssl.enabled.protocols", "TLSv1.2");
        result.setProperty("ssl.endpoint.identification.algorithm", "HTTPS");
        result.setProperty("acks", "-1");

        return result;
        
    }
    
    /*
     * Return a CSV-String from a String array
     */
    private static String stringArrayToCSV(String[] sArray) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sArray.length; i++) {
            sb.append(sArray[i]);
            if (i < sArray.length -1) sb.append(",");
        }
        return sb.toString();
    }    

}
