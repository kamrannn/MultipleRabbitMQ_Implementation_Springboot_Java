package com.freenow.multirabbit.example.java;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.amqp.rabbit.connection.SimpleResourceHolder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.amqp.ConnectionFactoryContextWrapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
class SomeController {

    private final RabbitTemplate rabbitTemplate;
    private final ConnectionFactoryContextWrapper contextWrapper;

    SomeController(final RabbitTemplate rabbitTemplate,
                   final ConnectionFactoryContextWrapper contextWrapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.contextWrapper = contextWrapper;
    }

    @PostConstruct
    void sendMessageToDefaultServer() throws JsonProcessingException {
        rabbitTemplate.convertAndSend("dpdp-exchange", "dpdp-queue_rk", "Message for the remote server from direct host");
    }

    /**
     * Sends a message using the default Spring implementation.
     */
    @PostConstruct
    private void sendMessageTheDefaultWayToRemote() {
        // Binding to the right context of Rabbit ConnectionFactory
        SimpleResourceHolder.bind(rabbitTemplate.getConnectionFactory(), "connectionNameA");

        final String exchange = "dpdp-exchange";
        final String routingKey = "dpdp-queue_rk";
        try {
            // Regular use of RabbitTemplate
            rabbitTemplate.convertAndSend(exchange, routingKey, "Message for Connect to remote server");
        } finally {
            // Unbinding the context of Rabbit ConnectionFactory
            SimpleResourceHolder.unbind(rabbitTemplate.getConnectionFactory());
        }
    }

    @PostConstruct
    private void sendMessageToLocal() {
        // Binding to the right context of Rabbit ConnectionFactory
        SimpleResourceHolder.bind(rabbitTemplate.getConnectionFactory(), "connectionNameB");
        final String exchange = "dpdp-exchange";
        final String routingKey = "dpdp_publish_event";
        try {
            // Regular use of RabbitTemplate
            rabbitTemplate.convertAndSend(exchange, routingKey, "Message for Connect to local server");
        } finally {
            // Unbinding the context of Rabbit ConnectionFactory{
            SimpleResourceHolder.unbind(rabbitTemplate.getConnectionFactory());
        }
    }


    @PostConstruct
    private void sendMessageUsingContextWrapper() {
        contextWrapper.run("connectionNameB", () -> {
            // Regular use of RabbitTemplate
            rabbitTemplate.convertAndSend("dpdp-exchange", "dpdp_publish_event", "Message for Connect to local server through context server");
        });
    }
}
