package org.jenkinsci.plugins.rabbitmqconsumer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import org.jenkinsci.plugins.rabbitmqconsumer.events.RMQConnectionEvent;
import org.jenkinsci.plugins.rabbitmqconsumer.listeners.RMQChannelListener;
import org.jenkinsci.plugins.rabbitmqconsumer.listeners.RMQConnectionListener;
import org.jenkinsci.plugins.rabbitmqconsumer.notifiers.RMQConnectionNotifier;
import org.jenkinsci.plugins.rabbitmqconsumer.watchdog.ReconnectTimer;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * Handle class for RabbitMQ connection.
 * 
 * @author rinrinne a.k.a. rin_ne
 */
public class RMQConnection implements ShutdownListener, RMQChannelListener, RMQConnectionNotifier {

    private static final int TIMEOUT_CONNECTION_MILLIS = 30000;
    private static final int HEARTBEAT_CONNECTION_SECS = 60;

    private static final Logger LOGGER = Logger.getLogger(RMQConnection.class.getName());

    private final String serviceUri;
    private final ConnectionFactory factory;
    private Connection connection = null;
    private final List<RMQChannel> rmqChannels = new ArrayList<RMQChannel>();
    private final List<RMQConnectionListener> rmqConnectionListeners = new ArrayList<RMQConnectionListener>();
    private volatile boolean closeRequested = true;

    private Object lockForListener = new Object();
    private Object lockForChannels = new Object();

    /**
     * Creates instance with specified parameter.
     * 
     * @param serviceUri
     *            the URI for RabbitMQ service.
     */
    public RMQConnection(String serviceUri) {
        this.serviceUri = serviceUri;
        this.factory = new ConnectionFactory();
        this.factory.setConnectionTimeout(TIMEOUT_CONNECTION_MILLIS);
        this.factory.setRequestedHeartbeat(HEARTBEAT_CONNECTION_SECS);
    }

    /**
     * Gets URI for RabbitMQ service.
     * 
     * @return the URI.
     */
    public String getServiceUri() {
        return serviceUri;
    }

    /**
     * Gets the list of RMQChannels.
     * 
     * @return the list of RMQChannels.
     */
    public List<RMQChannel> getRMQChannels() {
        return rmqChannels;
    }

    /**
     * Gets status of channel bins specified queue.
     * 
     * @param queueName
     *            the queue name.
     * @return true if channel for specified queue is already established.
     */
    public boolean getChannelStatus(String queueName) {
        synchronized (lockForChannels) {
            for (RMQChannel ch : rmqChannels) {
                if (ch.getQueueName().equals(queueName)) {
                    return ch.isConsumeStarted();
                }
            }
        }
        return false;
    }

    /**
     * Open connection.
     * 
     * @throws IOException
     *             thow if connection cannot be opend.
     */
    public void open() throws IOException {
        if (closeRequested) {
            try {
                factory.setUri(serviceUri);
                connection = factory.newConnection();
                connection.addShutdownListener(this);
                closeRequested = false;
                ReconnectTimer.get().start();
                notifyOnOpen();
            } catch (URISyntaxException e) {
                throw new IOException(e);
            } catch (GeneralSecurityException e) {
                throw new IOException(e);
            }
        } else {
            throw new IOException("Connection is already opened.");
        }
    }

    /**
     * Close connection.
     */
    public void close() {
        try {
            closeRequested = true;
            ReconnectTimer.get().stop();
            if (connection != null) {
                connection.close();
            }
        } catch (IOException e) {
            LOGGER.warning(e.toString());
        } finally {
            connection = null;
        }
    }

    /**
     * Gets if connection is established.
     * 
     * @return true if connection is already established.
     */
    public boolean isOpen() {
        return !closeRequested;
    }

    /**
     * Updates each channels.
     * 
     * @param consumeItems
     *            the list of consume items.
     */
    public void updateChannels(List<RabbitmqConsumeItem> consumeItems) {
        HashSet<String> uniqueQueueNames = new HashSet<String>();

        if (consumeItems == null) {
            closeAllChannels();
        } else {
            // generate unique queue name set
            for (RabbitmqConsumeItem i : consumeItems) {
                uniqueQueueNames.add(i.getQueueName());
            }
            uniqueQueueNames.remove(null);

            // close unused channels
            closeUnusedChannels(uniqueQueueNames);

            // create channels
            createNewChannels(uniqueQueueNames, consumeItems);
        }
    }

    /**
     * Creates new channels with specified consume items.
     * 
     * @param uniqueQueueNames
     *            the hashset of unique queue names.
     * @param consumeItems
     *            the list of consume items.
     */
    private void createNewChannels(HashSet<String> uniqueQueueNames, List<RabbitmqConsumeItem> consumeItems) {
        if (closeRequested) {
            LOGGER.warning("Cannot create channel while shutdown.");
            return;
        }

        if (uniqueQueueNames == null || consumeItems == null || uniqueQueueNames.isEmpty() || consumeItems.isEmpty()) {
            LOGGER.info("No create new channel due to empty.");
        } else {
            HashSet<String> existingQueueNames = new HashSet<String>();

            // get existing channel name set
            synchronized (lockForChannels) {
                for (RMQChannel h : rmqChannels) {
                    existingQueueNames.add(h.getQueueName());
                }
            }

            // create non-existing channels
            for (String queueName : uniqueQueueNames) {
                if (!existingQueueNames.contains(queueName)) {
                    HashSet<String> appIds = new HashSet<String>();
                    for (RabbitmqConsumeItem i : consumeItems) {
                        if (queueName.equals(i.getQueueName())) {
                            appIds.add(i.getAppId());
                        }
                    }
                    appIds.remove(GlobalRabbitmqConfiguration.CONTENT_NONE);
                    if (!appIds.isEmpty()) {
                        try {
                            RMQChannel ch = new RMQChannel(queueName, appIds);
                            ch.addRMQChannelListener(this);
                            ch.open(connection);
                            synchronized (lockForChannels) {
                                rmqChannels.add(ch);
                            }
                        } catch (IOException e) {
                            LOGGER.warning(e.toString());
                        }
                    }
                }
            }
        }
    }

    /**
     * Close unused channels.
     * 
     * @param usedQueueNames
     *            the hashset of used queue names.
     */
    private void closeUnusedChannels(HashSet<String> usedQueueNames) {
        if (!rmqChannels.isEmpty()) {
            synchronized (lockForChannels) {
                for (RMQChannel ch : rmqChannels) {
                    if (!usedQueueNames.contains(ch.getQueueName())) {
                        ch.close();
                    }
                }
            }
        }
    }

    /**
     * Close all channels.
     */
    private void closeAllChannels() {
        if (!rmqChannels.isEmpty()) {
            synchronized (lockForChannels) {
                for (RMQChannel h : rmqChannels) {
                    h.close();
                }
            }
        }
    }

    /**
     * @inheritDoc
     * @param rmqChannel
     *            the channel.
     */
    public void onOpen(RMQChannel rmqChannel) {
        LOGGER.info("Open RabbitMQ channel for " + rmqChannel.getQueueName() + ".");
        rmqChannel.consume();
    }

    /**
     * @inheritDoc
     * @param rmqChannel
     *            the channel.
     */
    public void onCloseCompleted(RMQChannel rmqChannel) {
        LOGGER.info("Closed RabbitMQ channel for " + rmqChannel.getQueueName() + ".");
        rmqChannel.removeRMQChannelListener(this);
        synchronized (lockForChannels) {
            rmqChannels.remove(rmqChannel);
        }
    }

    /**
     * @inheritDoc
     * @param rmqConnectionListener
     *            the connection listener.
     */
    public void addRMQConnectionListener(RMQConnectionListener rmqConnectionListener) {
        synchronized (lockForListener) {
            if (!rmqConnectionListeners.contains(rmqConnectionListener)) {
                rmqConnectionListeners.add(rmqConnectionListener);
            }
        }
    }

    /**
     * @inheritDoc
     * @param rmqConnectionListener
     *            the connection listener.
     */
    public void removeRMQConnectionListener(RMQConnectionListener rmqConnectionListener) {
        synchronized (lockForListener) {
            rmqConnectionListeners.remove(rmqConnectionListener);
        }
    }

    /**
     * @inheritDoc
     * @return true if connection is already established.
     */
    public boolean isOpenRMQConnection() {
        return connection.isOpen();
    }

    /**
     * @inheritDoc
     * @param event
     *            the event for connection.
     */
    public void notifyRMQConnectionListeners(RMQConnectionEvent event) {
        synchronized (lockForListener) {
            for (RMQConnectionListener l : rmqConnectionListeners) {
                if (event == RMQConnectionEvent.CLOSE_COMPLETED) {
                    l.onCloseCompleted(this);
                } else if (event == RMQConnectionEvent.OPEN) {
                    l.onOpen(this);
                }
            }
        }
    }

    /**
     * Notify OnCloseCompleted event.
     */
    public void notifyOnCloseCompleted() {
        notifyRMQConnectionListeners(RMQConnectionEvent.CLOSE_COMPLETED);
    }

    /**
     * Notify OnOpen event.
     */
    public void notifyOnOpen() {
        notifyRMQConnectionListeners(RMQConnectionEvent.OPEN);
    }

    /**
     * @inheritDoc
     * @param shutdownSignalException
     *            the exception.
     */
    public void shutdownCompleted(ShutdownSignalException shutdownSignalException) {
        connection = null;
        notifyOnCloseCompleted();
    }
}
