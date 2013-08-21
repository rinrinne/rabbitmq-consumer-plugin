package org.jenkinsci.plugins.rabbitmqconsumer.channels;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import org.jenkinsci.plugins.rabbitmqconsumer.events.RMQChannelEvent;
import org.jenkinsci.plugins.rabbitmqconsumer.listeners.RMQChannelListener;
import org.jenkinsci.plugins.rabbitmqconsumer.listeners.RMQConnectionListener;
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
public abstract class AbstractRMQChannel implements RMQChannelNotifier, ShutdownListener {

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
        channel.addShutdownListener(this);
        notifyOnOpen();
    }

    /**
     * Gets instance of Channel, not RMQChannel.
     *
     * @return the instance of Channel.
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Close channel.
     */
    public void close() {
        try {
            channel.close();
        } catch (IOException e) {
            LOGGER.warning("Could not close channel. but go forward.");
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
        channel = null;
        notifyOnCloseCompleted();
    }
}
