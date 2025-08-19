package com.cie.nems.common.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@DependsOn(value = "calcTopoConfig")
@Configuration
@EnableKafka
public class KafkaConfig {
	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Bean
	public KafkaAdmin kafkaAdmin() {
		Map<String, Object> props = new HashMap<>();
		props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		KafkaAdmin admin = new KafkaAdmin(props);
		return admin;
	}

	@Bean
	public AdminClient adminClient() {
		return AdminClient.create(kafkaAdmin().getConfigurationProperties());
	}

	// ConcurrentKafkaListenerContainerFactory为创建Kafka监听器的工程类
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		factory.setAutoStartup(false);
		return factory;
	}

	// 根据consumerProps填写的参数创建消费者工厂
	@Bean
	public ConsumerFactory<String, String> consumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerProps());
	}

	// 根据senderProps填写的参数创建生产者工厂
	@Bean
	public ProducerFactory<String, String> producerFactory() {
		return new DefaultKafkaProducerFactory<>(senderProps());
	}

	// kafkaTemplate实现了Kafka发送接收等功能
	@Bean
	public KafkaTemplate<String, String> kafkaTemplate() {
		return new KafkaTemplate<String, String>(producerFactory());
	}

	@Value("${spring.kafka.consumer.group-id:#{'default-group'}}")
	private String groupId;
	@Value("${spring.kafka.consumer.auto-offset-reset:#{'earliest'}}")
	private String autoOffSetReset;
	@Value("${spring.kafka.consumer.enable-auto-commit:#{false}}")
	private boolean enableAutoCommit;
	@Value("${spring.kafka.consumer.auto-commit-interval:#{1000}}")
	private int autoCommitInterval;
	@Value("${spring.kafka.consumer.session-timeout-ms:#{30000}}")
	private int sessionTimeoutMs;
//	@Value("${spring.kafka.consumer.max-poll-interval-ms:#{10000}}")
//	private int maxPollIntervalMs;
//	@Value("${spring.kafka.consumer.max-poll-records:#{10}}")
//	private int maxPollRecords;

	@Value("${cie.consumer.normal.max-poll-interval-ms:#{10000}}")
	private int normalMaxPollIntervalMs;
	@Value("${cie.consumer.normal.max-poll-records:#{10}}")
	private int normalMaxPollRecords;
	@Value("${cie.consumer.batch.max-poll-interval-ms:#{10000}}")
	private int batchMaxPollIntervalMs;
	@Value("${cie.consumer.batch.max-poll-records:#{100}}")
	private int batchMaxPollRecords;

	// 消费者配置参数
	public Map<String, Object> consumerProps() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffSetReset);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, autoCommitInterval);
		props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, normalMaxPollIntervalMs);
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, normalMaxPollRecords);
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeoutMs);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		return props;
	}

	// 消费者配置参数
	public Map<String, Object> batchConsumerProps() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffSetReset);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, autoCommitInterval);
		props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, batchMaxPollIntervalMs);
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, batchMaxPollRecords);
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeoutMs);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		return props;
	}

	@Value("${spring.kafka.producer.retries:#{1}}")
	private int retries;
	@Value("${spring.kafka.producer.batch-size:#{16384}}")
	private int batchSize;
	@Value("${spring.kafka.producer.linger-ms:#{1}}")
	private int lingerMs;
	@Value("${spring.kafka.producer.buffer-memory:#{10240000}}")
	private int bufferMemory;

	// 生产者配置
	public Map<String, Object> senderProps() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.RETRIES_CONFIG, retries);
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
		props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		return props;
	}

}
