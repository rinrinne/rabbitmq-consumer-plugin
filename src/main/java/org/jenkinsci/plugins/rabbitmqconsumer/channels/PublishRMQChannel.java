package org.jenkinsci.plugins.rabbitmqconsumer.channels;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jenkinsci.plugins.rabbitmqconsumer.listeners.RMQChannelListener;
import org.jenkinsci.plugins.rabbitmqconsumer.publishers.ExchangeType;
import org.jenkinsci.plugins.rabbitmqconsumer.publishers.PublishChannel;
import org.jenkinsci.plugins.rabbitmqconsumer.publishers.PublishResult;

import com.rabbitmq.client.AMQP;

/**
 * Handle class for RabbitMQ publish channel.
 *
 * @author rinrinne a.k.a. rin_ne
 */
public class PublishRMQChannel extends AbstractRMQChannel implements PublishChannel {

    private static final Logger LOGGER = Logger.getLogger(PublishRMQChannel.class.getName());
    private final ExecutorService publishExecutor = Executors.newSingleThreadExecutor();

    /**
     * Creates instance.
     */
    public PublishRMQChannel() {
    }

    /**
     * @inheritDoc
     */
    public Future<PublishResult> publish(String exchangeName, String routingKey,
            AMQP.BasicProperties props, byte[] body) {
        return publishExecutor.submit(new PublishTask(exchangeName, routingKey, props, body));
    }

    /**
     * @inheritDoc
     */
    public PublishResult setupExchange(String exchangeName, String queueName) {
        Future<PublishResult> future = publishExecutor.submit(
                new PrepareTask(exchangeName, queueName, ExchangeType.FANOUT, ""));
        PublishResult result = null;
        try {
            result = future.get();
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
        return result;
    }

    /**
     * @inheritDoc
     */
    public PublishResult setupExchange(String exchangeName, String queueName,
            ExchangeType exchangeType, String routingKey) {
        Future<PublishResult> future = publishExecutor.submit(
                new PrepareTask(exchangeName, queueName, exchangeType, routingKey));
        PublishResult result = null;
        try {
            result = future.get();
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
        return result;
    }

    /**
     * @inheritDoc
     */
    public boolean isOpen() {
        return isOpenRMQChannel();
    }

    /**
     * @inheritDoc
     */
    public void addListener(RMQChannelListener listener) {
        addRMQChannelListener(listener);
    }

    /**
     * @inheritDoc
     */
    public void removeListener(RMQChannelListener listener) {
        removeRMQChannelListener(listener);
    }

    /**
     * A class to publish message.
     *
     * @author rinrinne a.k.a. rin_ne
     *
     */
    public class PublishTask implements Callable<PublishResult> {

        private String exchangeName;
        private String routingKey;
        private AMQP.BasicProperties props;
        private byte[] body;

        /**
         * Create instance.
         *
         * @param exchangeName the exchange name.
         * @param routingKey the routing key.
         * @param props the properties for AMQP headers.
         * @param body the message body.
         */
        public PublishTask(String exchangeName, String routingKey,
                AMQP.BasicProperties props, byte[] body) {
            this.exchangeName = exchangeName;
            this.routingKey = routingKey;
            this.props = props;
            this.body = body;
        }

        /**
         * @inheritDoc
         */
        public PublishResult call() throws Exception {
            if (channel != null && channel.isOpen()) {
                if (body != null) {
                    try {
                        channel.basicPublish(exchangeName, routingKey, props, body);
                        return new PublishResult(true, "Published", exchangeName);
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Failed to publish message.", e);
                        return new PublishResult(false, "Failed to publish message.", exchangeName);
                    }
                }
            }
            return new PublishResult(false, "Channel is not opened.", exchangeName);
        }
    }

    /**
     * A class to prepare publising
     *
     * @author rinrinne a.k.a. rin_ne
     */
    public class PrepareTask implements Callable<PublishResult> {

        private String exchangeName;
        private String queueName;
        private ExchangeType exchangeType;
        private String routingKey;

        /**
         * Create instance.
         *
         * @param exchangeName the exchange name.
         * @param queueName the queue name.
         */
        public PrepareTask(String exchangeName, String queueName, ExchangeType exchangeType, String routingKey) {
            this.exchangeName = exchangeName;
            this.queueName = queueName;
            this.exchangeType = exchangeType;
            this.routingKey = routingKey;
        }

        /**
         * @inheritDoc
         */
        public PublishResult call() throws Exception {
            if (channel != null && channel.isOpen()) {
                if (queueName == null) {
                    return createPublishResult(false, "Queue name should not be null.");
                }

                if (exchangeName == null) {
                    exchangeName = UUID.randomUUID().toString();
                    try {
                        channel.exchangeDeclare(exchangeName, exchangeType.name().toLowerCase());
                    } catch (IOException e) {
                        return createPublishResult(false, e.getMessage());
                    }
                }

                try {
                    channel.queueBind(queueName, exchangeName, routingKey);
                } catch (IOException e) {
                    return createPublishResult(false, e.getMessage());
                }

                return createPublishResult(true, "SUCCESS");
            }
            return createPublishResult(false, "Channel is not opened.");
       }

        PublishResult createPublishResult(boolean isSuccess, String message) {
            return new PublishResult(isSuccess, message,
                    exchangeName, queueName, exchangeType, routingKey);
        }
    }
}
