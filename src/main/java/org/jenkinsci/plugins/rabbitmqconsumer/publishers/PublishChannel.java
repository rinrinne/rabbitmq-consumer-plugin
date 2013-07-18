package org.jenkinsci.plugins.rabbitmqconsumer.publishers;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.jenkinsci.plugins.rabbitmqconsumer.listeners.RMQChannelListener;


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
     * Setup exchange.
     *
     * Fanout type exchange is declared then binded to queue in RabbitMQ.
     * Note that this is blocking method.
     *
     * @param exchangeName the exchange name. If null, unique name is used.
     * You can get it from {@link PublishResult#getExchangeName()}.
     * @param queueName the queue name. this is mandatory.
     * @return instance of {@link PublishResult}.
     * @throws CancellationException if the computation was cancelled.
     * @throws ExecutionException if the computation threw an exception.
     * @throws InterruptedException if the current thread was interrupted while waiting.
     */

    PublishResult setupExchange(String exchangeName, String queueName)
            throws CancellationException, ExecutionException, InterruptedException;

    /**
     * Setup exchange.
     *
     * Exchange is declared then binded to queue in RabbitMQ.
     * Note that this is blocking method.
     *
     * @param exchangeName the exchange name. If null, unique name is used.
     * You can get it from {@link PublishResult#getExchangeName()}.
     * @param queueName the queue name. this is mandatory.
     * @param type the exchange type.
     * @param routingKey the routing key. Key usage is decided by exchange type.
     * @return instance of {@link PublishResult}.
     * @throws CancellationException if the computation was cancelled.
     * @throws ExecutionException if the computation threw an exception.
     * @throws InterruptedException if the current thread was interrupted while waiting.
     */
    PublishResult setupExchange(String exchangeName, String queueName, ExchangeType type, String routingKey)
            throws CancellationException, ExecutionException, InterruptedException;

    /**
     * Gets channel is opened.
     *
     * Note that you may not be able to publish message even if this method returns true.
     *
     * @return true if channel is opened.
     */
    boolean isOpen();

    /**
     * Adds channel listener.
     *
     * @param listener the listener.
     */
    void addListener(RMQChannelListener listener);

    /**
     * Remove channel listener.
     *
     * @param listener the listener.
     */
    void removeListener(RMQChannelListener listener);
}
