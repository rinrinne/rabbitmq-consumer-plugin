package org.jenkinsci.plugins.rabbitmqconsumer.channels;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

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
    public boolean isOpen() {
        return isOpenRMQChannel();
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
                try {
                    channel.basicPublish(exchangeName, routingKey, props, body);
                    return new PublishResult(true, "Published");
                } catch (IOException e) {
                    LOGGER.warning(e.getMessage());
                    return new PublishResult(false, e.getMessage());
                }
            }
            return new PublishResult(false, "Channel is not opened.");
        }
    }
}
