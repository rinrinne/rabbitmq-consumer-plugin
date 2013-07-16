package org.jenkinsci.plugins.rabbitmqconsumer.publishers;

import java.util.concurrent.Future;


import com.rabbitmq.client.AMQP;

/**
 * A interface class for controlling RabbitMQ publish channel from externals.
 * 
 * @author rinrinne a.k.a. rin_ne
 */
public interface PublishChannel {

    /**
     * Publish message.
     *
     * The usage of this method is the same as original amqp client library.
     * See "Publising message"section in API guide.
     * http://www.rabbitmq.com/api-guide.html
     *
     * This is non-blocking method. The message you provide is published from other thread.
     * Note that the message still not published even if return from this method.
     * 
     * @param exchangeName the name of exchange.
     * @param routingKey the routing key.
     * @param props the properties of AMQP message.
     * @param body the message body.
     * @return instance of Future<T> class. T is {@link PublishRMQChannel.Result}.
     */
    Future<PublishResult> publish(String exchangeName, String routingKey,
            AMQP.BasicProperties props, byte[] body);

    /**
     * Gets channel is opened.
     *
     * Note that you may not be able to publish message even if this method returns true.
     *
     * @return true if channel is opened.
     */
    boolean isOpen();
}
