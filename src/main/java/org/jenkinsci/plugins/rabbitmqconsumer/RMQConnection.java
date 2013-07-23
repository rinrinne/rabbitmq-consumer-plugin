package org.jenkinsci.plugins.rabbitmqconsumer;

import hudson.util.Secret;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.rabbitmqconsumer.channels.AbstractRMQChannel;
import org.jenkinsci.plugins.rabbitmqconsumer.channels.ConsumeRMQChannel;
import org.jenkinsci.plugins.rabbitmqconsumer.channels.PublishRMQChannel;
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
    private final String userName;
    private final Secret userPassword;
    private final ConnectionFactory factory;
    private Connection connection = null;
    private final Set<AbstractRMQChannel> rmqChannels = new CopyOnWriteArraySet<AbstractRMQChannel>();
    private final Set<RMQConnectionListener> rmqConnectionListeners = new CopyOnWriteArraySet<RMQConnectionListener>();
    private volatile boolean closeRequested = true;

    /**
     * Creates instance with specified parameter.
     * 
     * @param serviceUri
     *            the URI for RabbitMQ service.
     */
    public RMQConnection(String serviceUri, String userName, Secret userPassword) {
        this.serviceUri = serviceUri;
        this.userName = userName;
        this.userPassword = userPassword;
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
     * Gets URI for RabbitMQ service.
     * 
     * @return the URI.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Gets URI for RabbitMQ service.
     * 
     * @return the URI.
     */
    public Secret getUserPassword() {
        return userPassword;
    }

    /**
     * Gets the list of RMQChannels.
     * 
     * @return the list of RMQChannels.
     */
    public Set<AbstractRMQChannel> getRMQChannels() {
        return rmqChannels;
    }

    /**
     * Gets the list of ConsumeRMQChannels.
     * 
     * @return the list of ComsumeRMQChannels.
     */
    public Set<ConsumeRMQChannel> getConsumeRMQChannels() {
        Set<ConsumeRMQChannel> channels = new HashSet<ConsumeRMQChannel>();
        for (AbstractRMQChannel ch : rmqChannels) {
            if (ch instanceof ConsumeRMQChannel) {
                channels.add((ConsumeRMQChannel) ch);
            }
        }
        return channels;
    }

    /**
     * Gets the list of PublishRMQChannels.
     * 
     * @return the list of PublishRMQChannels.
     */
    public Set<PublishRMQChannel> getPublishRMQChannels() {
        Set<PublishRMQChannel> channels = new HashSet<PublishRMQChannel>();
        for (AbstractRMQChannel ch : rmqChannels) {
            if (ch instanceof PublishRMQChannel) {
                channels.add((PublishRMQChannel) ch);
            }
        }
        return channels;
    }

    /**
     * Gets status of channel binds specified queue.
     * 
     * @param queueName
     *            the queue name.
     * @return true if channel for specified queue is already established.
     */
    public boolean getConsumeChannelStatus(String queueName) {
        for (ConsumeRMQChannel ch : getConsumeRMQChannels()) {
            if (ch.getQueueName().equals(queueName)) {
                return ch.isConsumeStarted();
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
                if (StringUtils.isNotEmpty(userName)) {
                    factory.setUsername(userName);
                }
                if (StringUtils.isNotEmpty(Secret.toString(userPassword))) {
                    factory.setPassword(Secret.toString(userPassword));
                }
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

        updatePublishChannel();

        if (consumeItems == null) {
            closeAllConsumeChannels();
        } else {
            // generate unique queue name set
            for (RabbitmqConsumeItem i : consumeItems) {
                uniqueQueueNames.add(i.getQueueName());
            }
            uniqueQueueNames.remove(null);

            // close unused channels
            closeUnusedConsumeChannels(uniqueQueueNames);

            // create channels
            createNewConsumeChannels(uniqueQueueNames, consumeItems);
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
    private void createNewConsumeChannels(HashSet<String> uniqueQueueNames, List<RabbitmqConsumeItem> consumeItems) {
        if (closeRequested) {
            LOGGER.warning("Cannot create channel while shutdown.");
            return;
        }
        
        if (uniqueQueueNames == null || consumeItems == null || uniqueQueueNames.isEmpty() || consumeItems.isEmpty()) {
            LOGGER.info("No create new channel due to empty.");
        } else {
            HashSet<String> existingQueueNames = new HashSet<String>();

            // get existing channel name set
            for (ConsumeRMQChannel h : getConsumeRMQChannels()) {
                existingQueueNames.add(h.getQueueName());
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
                            ConsumeRMQChannel ch = new ConsumeRMQChannel(queueName, appIds);
                            ch.addRMQChannelListener(this);
                            ch.open(connection);
                            rmqChannels.add(ch);
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
    private void closeUnusedConsumeChannels(HashSet<String> usedQueueNames) {
        Set<ConsumeRMQChannel> channels = getConsumeRMQChannels();
        if (!channels.isEmpty()) {
            for (ConsumeRMQChannel ch : channels) {
                if (!usedQueueNames.contains(ch.getQueueName())) {
                    ch.close();
                }
            }
        }
    }

    /**
     * Close all channels.
     */
    private void closeAllChannels() {
        if (!rmqChannels.isEmpty()) {
            for (AbstractRMQChannel h : rmqChannels) {
                h.close();
            }
        }
    }

    /**
     * Close all consume channels.
     */
    private void closeAllConsumeChannels() {
        Set<ConsumeRMQChannel> channels = getConsumeRMQChannels();
        if (!channels.isEmpty()) {
            for (ConsumeRMQChannel h : channels) {
                h.close();
            }
        }
    }

    /**
     * Update publish channel.
     */
    public void updatePublishChannel() {
        if (getPublishRMQChannels().size() == 0) {
            try {
                PublishRMQChannel pubch = new PublishRMQChannel();
                pubch.addRMQChannelListener(this);
                pubch.open(connection);
                rmqChannels.add(pubch);
            } catch (IOException e) {
                LOGGER.warning(e.toString());
            }
        }
    }

    /**
     * @inheritDoc
     * @param rmqChannel
     *            the channel.
     */
    public void onOpen(AbstractRMQChannel rmqChannel) {
        if (rmqChannel instanceof ConsumeRMQChannel) {
            ConsumeRMQChannel consumeChannel = (ConsumeRMQChannel) rmqChannel;
            LOGGER.info("Open RabbitMQ channel for " + consumeChannel.getQueueName() + ".");
            consumeChannel.consume();
        } else if (rmqChannel instanceof PublishRMQChannel) {
            LOGGER.info("Open RabbitMQ channel for publish.");
        }
    }

    /**
     * @inheritDoc
     * @param rmqChannel
     *            the channel.
     */
    public void onCloseCompleted(AbstractRMQChannel rmqChannel) {
        if (rmqChannel instanceof ConsumeRMQChannel) {
            LOGGER.info("Closed RabbitMQ channel for " + ((ConsumeRMQChannel)rmqChannel).getQueueName() + ".");
        } else if (rmqChannel instanceof PublishRMQChannel) {
            LOGGER.info("Closed RabbitMQ channel for publish.");
        }
        rmqChannel.removeRMQChannelListener(this);
        rmqChannels.remove(rmqChannel);
    }

    /**
     * @inheritDoc
     * @param rmqConnectionListener
     *            the connection listener.
     */
    public void addRMQConnectionListener(RMQConnectionListener rmqConnectionListener) {
        rmqConnectionListeners.add(rmqConnectionListener);
    }

    /**
     * @inheritDoc
     * @param rmqConnectionListener
     *            the connection listener.
     */
    public void removeRMQConnectionListener(RMQConnectionListener rmqConnectionListener) {
        rmqConnectionListeners.remove(rmqConnectionListener);
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
        for (RMQConnectionListener l : rmqConnectionListeners) {
            if (event == RMQConnectionEvent.CLOSE_COMPLETED) {
                l.onCloseCompleted(this);
            } else if (event == RMQConnectionEvent.OPEN) {
                l.onOpen(this);
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
