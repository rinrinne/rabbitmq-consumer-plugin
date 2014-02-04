package org.jenkinsci.plugins.rabbitmqconsumer.channels;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import org.jenkinsci.plugins.rabbitmqconsumer.events.RMQChannelEvent;
import org.jenkinsci.plugins.rabbitmqconsumer.listeners.RMQChannelListener;
import org.jenkinsci.plugins.rabbitmqconsumer.notifiers.RMQChannelNotifier;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * abstract class for handling RabbitMQ channel.
 *
 * @author rinrinne a.k.a. rin_ne
 */
public abstract class AbstractRMQChannel implements RMQChannel, RMQChannelNotifier, ShutdownListener {

    private static final Logger LOGGER = Logger.getLogger(AbstractRMQChannel.class.getName());

    protected Channel channel;
    protected final Set<RMQChannelListener> rmqChannelListeners = new CopyOnWriteArraySet<RMQChannelListener>();

    /**
     * Creates instance with specified parameters.
     *
     * @param appIds
     *            the hashset of application id.
     */
    public AbstractRMQChannel() {
    }

    /**
     * Open connection.
     *
     * @param connection
     *            the instance of Connection, not RMQConnection.
     * @throws IOException
     *             exception if channel cannot be created.
     */
    public void open(final Connection connection) throws IOException {
        channel = connection.createChannel();
        if (channel != null) {
            channel.addShutdownListener(this);
            notifyOnOpen();
        }
    }

    /**
     * @inheritDoc
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Close channel.
     *
     * @throws IOException throws if something error.
     */
    public void close() throws IOException {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException ex) {
                notifyOnCloseCompleted();
                channel = null;
                throw ex;
            }
        }
    }

    /**
     * @inheritDoc
     * @param rmqChannelListener
     *            the channel listener.
     */
    public void addRMQChannelListener(RMQChannelListener rmqChannelListener) {
        rmqChannelListeners.add(rmqChannelListener);
    }

    /**
     * @inheritDoc
     * @param rmqChannelListener
     *            the channel listener.
     */
    public void removeRMQChannelListener(RMQChannelListener rmqChannelListener) {
        rmqChannelListeners.remove(rmqChannelListener);
    }

    /**
     * @inheritDoc
     * @return true if channel is already opened.
     */
    public boolean isOpenRMQChannel() {
        if (channel != null) {
            return channel.isOpen();
        } else {
            return false;
        }
    }

    /**
     * @inheritDoc
     * @param event
     *            the event for channel.
     */
    public void notifyRMQChannelListeners(RMQChannelEvent event) {
        for (RMQChannelListener l : rmqChannelListeners) {
            if (event == RMQChannelEvent.CLOSE_COMPLETED) {
                l.onCloseCompleted(this);
            } else if (event == RMQChannelEvent.OPEN) {
                l.onOpen(this);
            }
        }
    }

    /**
     * Notify OnCloseCompleted event.
     */
    public void notifyOnCloseCompleted() {
        notifyRMQChannelListeners(RMQChannelEvent.CLOSE_COMPLETED);
    }

    /**
     * Notify OnOpen event.
     */
    public void notifyOnOpen() {
        notifyRMQChannelListeners(RMQChannelEvent.OPEN);
    }

    /**
     * @inheritDoc
     * @param shutdownSignalException
     *            the exception.
     */
    public void shutdownCompleted(ShutdownSignalException shutdownSignalException) {
        if (shutdownSignalException != null && !shutdownSignalException.isInitiatedByApplication()) {
            LOGGER.warning(MessageFormat.format("RabbitMQ channel {0} was suddenly closed.", channel.getChannelNumber()));
        }
        notifyOnCloseCompleted();
        channel = null;
    }
}
