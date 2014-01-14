package org.jenkinsci.plugins.rabbitmqconsumer.extensions;

import java.util.concurrent.Future;

import jenkins.model.Jenkins;
import hudson.Extension;
import hudson.ExtensionList;

import org.jenkinsci.plugins.rabbitmqconsumer.RMQManager;
import org.jenkinsci.plugins.rabbitmqconsumer.channels.PublishRMQChannel;
import org.jenkinsci.plugins.rabbitmqconsumer.publishers.PublishResult;

import com.rabbitmq.client.AMQP;

/**
 * Extension class to publish message to RabbitMQ.
 *
 * @author rinrinne a.k.a. rin_ne
 */
@Extension
public class MessagePublisher {

    /**
     * Publish message.
     *
     * @param exchangeName the exhange name.
     * @param routingKey the routing key.
     * @param props the list of property.
     * @param body the content.
     * @return future object for PublishResult.
     */
    Future<PublishResult> publish(String exchangeName, String routingKey,
            AMQP.BasicProperties props, byte[] body) {
        PublishRMQChannel ch = RMQManager.getInstance().getPublishChannel();
        if (ch != null && ch.isOpen()) {
            return ch.publish(exchangeName, routingKey, props, body);
        }
        return null;
    }

    /**
     * Get extension instance.
     *
     * @return the instance of this class.
     */
    public static MessagePublisher get() {
        ExtensionList<MessagePublisher> extensions = Jenkins.getInstance().getExtensionList(MessagePublisher.class);
        if (extensions != null && extensions.size() > 0) {
            return extensions.get(0);
        }
        return null;
    }
}
