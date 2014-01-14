package org.jenkinsci.plugins.rabbitmqconsumer.channels;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jenkinsci.plugins.rabbitmqconsumer.GlobalRabbitmqConfiguration;
import org.jenkinsci.plugins.rabbitmqconsumer.RabbitmqConsumeItem;
import org.jenkinsci.plugins.rabbitmqconsumer.events.RMQChannelEvent;
import org.jenkinsci.plugins.rabbitmqconsumer.listeners.MessageQueueListener;
import org.jenkinsci.plugins.rabbitmqconsumer.listeners.RMQChannelListener;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;

/**
 * Handle class for RabbitMQ consume channel.
 *
 * @author rinrinne a.k.a. rin_ne
 */
public class ConsumeRMQChannel extends AbstractRMQChannel {

    private static final Logger LOGGER = Logger.getLogger(ConsumeRMQChannel.class.getName());

    protected final HashSet<String> appIds;
    private final String queueName;
    private volatile boolean consumeStarted = false;

    private final boolean debug = GlobalRabbitmqConfiguration.get().isEnableDebug();
    @SuppressWarnings("serial")
    private final HashSet<String> debugId = new HashSet<String>() {
        {
            add(RabbitmqConsumeItem.DEBUG_APPID);
        }
    };

    /**
     * Creates instance with specified parameters.
     *
     * @param queueName
     *            the queue name.
     * @param appIds
     *            the hashset of application id.
     */
    public ConsumeRMQChannel(String queueName, HashSet<String> appIds) {
        this.appIds = appIds;
        this.queueName = queueName;
    }

    /**
     * Get hashset of app ids.
     *
     * @return the hashset of app ids.
     */
    public HashSet<String> getAppIds() {
        return appIds;
    }

    /**
     * Gets queue name.
     *
     * @return the queue name.
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Starts consume.
     */
    public void consume() {
        try {
            channel.basicConsume(queueName, false, new MessageConsumer(channel));
            consumeStarted = true;
            MessageQueueListener.fireOnBind(appIds, queueName);
        } catch (IOException e) {
            LOGGER.info(e.toString());
        }
    }

    /**
     * Gets whether consumer is already started or not.
     *
     * @return true if consumer is already started.
     */
    public boolean isConsumeStarted() {
        return consumeStarted;
    }

    /**
     * Handle class that consume message.
     *
     * @author rinrinne a.k.a. rin_ne
     *
     */
    public class MessageConsumer extends DefaultConsumer {

        /**
         * Creates instance with specified parameter.
         *
         * @param channel
         *            the instance of Channel, not RMQChannel.
         */
        public MessageConsumer(Channel channel) {
            super(channel);
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
                throws IOException {

            try {

                long deliveryTag = envelope.getDeliveryTag();
                String contentType = properties.getContentType();
                Map<String, Object> headers = properties.getHeaders();

                if (debug) {
                    if (appIds.contains(RabbitmqConsumeItem.DEBUG_APPID)) {
                        MessageQueueListener.fireOnReceive(debugId, queueName, contentType, headers, body);
                    }
                }

                if (properties.getAppId() != null &&
                        !properties.getAppId().equals(RabbitmqConsumeItem.DEBUG_APPID)) {
                    if (appIds.contains(properties.getAppId())) {
                        MessageQueueListener.fireOnReceive(appIds, queueName, contentType, headers, body);
                    }
                }

                channel.basicAck(deliveryTag, false);

            } catch (IOException e) {
                throw e;
            } catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, "caught exception in delivery handler", e);
            }
        }
    }

    /**
     * @inheritDoc
     * @param event
     *            the event for channel.
     */
    public void notifyRMQChannelListeners(RMQChannelEvent event) {
        Set<RMQChannelListener> listeners = new HashSet<RMQChannelListener>();
        for (RMQChannelListener l : rmqChannelListeners) {
            if (event == RMQChannelEvent.CLOSE_COMPLETED) {
                l.onCloseCompleted(this);
                listeners.add(l);
            } else if (event == RMQChannelEvent.OPEN) {
                l.onOpen(this);
            }
        }
        if (listeners.size() > 0) {
            rmqChannelListeners.remove(listeners);
        }
    }

    /**
     * @inheritDoc
     * @param shutdownSignalException
     *            the exception.
     */
    public void shutdownCompleted(ShutdownSignalException shutdownSignalException) {
        channel = null;
        consumeStarted = false;
        MessageQueueListener.fireOnUnbind(appIds, queueName);
        super.shutdownCompleted(shutdownSignalException);
    }
}
