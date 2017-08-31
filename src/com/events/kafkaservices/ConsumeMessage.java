package com.events.kafkaservices;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.PartitionInfo;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.events.pojo.EventRequest;
import com.events.pojo.EventResponse;
import com.messagehub.bluemix.BluemixEnvironment;
import com.messagehub.bluemix.MessageHubCredentials;

public class ConsumeMessage {

	private static final String APP_NAME = "EventCreator";
	private static final String DEFAULT_TOPIC_NAME = "mdo-man-calc-topic2";
	private static final Logger logger = Logger.getLogger(CreateMessage.class);

	private static String resourceDir;

	public EventResponse consumeMessage(EventRequest req) {

		final String userDir = System.getProperty("user.dir");
		final Properties clientProperties = new Properties();

		String bootstrapServers = null;
		String adminRestURL = null;
		String apiKey = null;

		String topic = DEFAULT_TOPIC_NAME;
		String user;
		String password;

		EventResponse resp = new EventResponse();

		

		List<String> messages = new ArrayList<String>();

		try {

			logger.log(Level.INFO, "Running in Bluemix mode.");
			resourceDir = userDir + File.separator + APP_NAME + File.separator + "classes";

			System.out.println("Consuming message");
			MessageHubCredentials credentials = BluemixEnvironment.getMessageHubCredentials();

			bootstrapServers = stringArrayToCSV(credentials.getKafkaBrokersSasl());
			System.out.println("bootstrapServers:" + bootstrapServers);
			adminRestURL = credentials.getKafkaRestUrl();
			System.out.println("adminRestURL:" + adminRestURL);

			apiKey = credentials.getApiKey();
			user = credentials.getUser();
			password = credentials.getPassword();

			// inject bootstrapServers in configuration, for both consumer and producer
			clientProperties.put("bootstrap.servers", bootstrapServers);

			Properties consumerProperties = getHardcodedClientConfig(clientProperties, user, password);

			KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String, String>(consumerProperties);

			// Checking for topic existence before subscribing
			List<PartitionInfo> partitions = kafkaConsumer.partitionsFor(topic);
			if (partitions == null || partitions.isEmpty()) {
				logger.log(Level.ERROR, "Topic '" + topic + "' does not exists - application will terminate");
				kafkaConsumer.close();
				throw new IllegalStateException("Topic '" + topic + "' does not exists - application will terminate");
			} else {
				logger.log(Level.INFO, partitions.toString());
			}

			kafkaConsumer.subscribe(Arrays.asList(topic));

			try {

				try {
					// Poll on the Kafka consumer, waiting up to 3 secs if there's nothing to
					// consume.
					ConsumerRecords<String, String> records = kafkaConsumer.poll(3000);

					if (records.isEmpty()) {
						logger.log(Level.INFO, "No messages consumed");
					} else {
						// Iterate through all the messages received and print their content
						for (ConsumerRecord<String, String> record : records) {
							logger.log(Level.INFO, "Message consumed: " + record.toString());
							messages.add(record.value());
						}
					}

				} catch (final KafkaException e) {
					logger.log(Level.ERROR, "KafkaException: " + e, e);
					try {
						Thread.sleep(5000); // Longer sleep before retrying
					} catch (InterruptedException e1) {
						logger.log(Level.WARN, "Consumer closing - caught exception: " + e);
					}
				}

			} finally {
				kafkaConsumer.close();
				logger.log(Level.INFO, ConsumeMessage.class.toString() + " has shut down.");
			}

		} catch (Exception e) {
			logger.log(Level.ERROR, "Exception occurred, application will terminate", e);
			System.out.println("Error in createMessage : " + e);
		}

		resp.setMessages(messages);
		
		String respData=null;
		
		if(messages.size()>0)
		{
			respData="Returning " + messages.size() + "records";
		}
		else
		{
			respData="No messages found in Que";
		}
		resp.setStatus("Success");
		resp.setRespData(respData);

		return (resp);

	}

	/*
	 * Hardcoded values... change this to read from file
	 * 
	 */
	public Properties getHardcodedClientConfig(Properties commonProps, String user, String password) {
		Properties result = new Properties();
		result.putAll(commonProps);
		String saslJaasConfig = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + user
				+ "\" password=\"" + password + "\";";
		System.out.println("saslJaasConfig:" + saslJaasConfig);
		logger.log(Level.ERROR, "saslJaasConfig:" + saslJaasConfig);

		result.setProperty("sasl.jaas.config", saslJaasConfig);

		result.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		result.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		result.setProperty("client.id", "kafka-java-console-sample-consumer");
		result.setProperty("group.id", "kafka-java-console-sample-group");

		result.setProperty("security.protocol", "SASL_SSL");
		result.setProperty("sasl.mechanism", "PLAIN");
		result.setProperty("ssl.protocol", "TLSv1.2");
		result.setProperty("ssl.enabled.protocols", "TLSv1.2");
		result.setProperty("ssl.endpoint.identification.algorithm", "HTTPS");
		result.setProperty("auto.offset.reset", "latest");

		return result;

	}

	/*
	 * Return a CSV-String from a String array
	 */
	private static String stringArrayToCSV(String[] sArray) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sArray.length; i++) {
			sb.append(sArray[i]);
			if (i < sArray.length - 1)
				sb.append(",");
		}
		return sb.toString();
	}

}
