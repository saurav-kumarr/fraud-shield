package com.fraudshield.transaction.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RefreshScope
public class KafkaConfig {

    @Value("${kafka.topic.transaction-events}")
    private String transactionEvents;

    @Value("${kafka.topic.fraud-alerts}")
    private String fraudAlerts;

    @Value("${kafka.topic.notification-events}")
    private String notificationEvents;

    @Value("${kafka.topic.audit-log}")
    private String auditLog;

    @Bean
    public NewTopic transactionEventsTopic() {
        return TopicBuilder
                .name(transactionEvents)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic fraudAlertsTopic() {
        return TopicBuilder
                .name(fraudAlerts)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder
                .name(notificationEvents)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic auditLogTopic() {
        return TopicBuilder
                .name(auditLog)
                .partitions(1)
                .replicas(1)
                .build();
    }

}
