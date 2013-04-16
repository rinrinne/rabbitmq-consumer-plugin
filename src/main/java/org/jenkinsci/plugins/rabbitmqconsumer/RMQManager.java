package org.jenkinsci.plugins.rabbitmqconsumer;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.jenkinsci.plugins.rabbitmqconsumer.listeners.RMQConnectionListener;

/**
 * Manager class for RabbitMQ connection.
 * 
 * @author rinrinne a.k.a. rin_ne
 */
public final class RMQManager implements RMQConnectionListener {

    /**
     * Intance holder class for {@link RMQManager}.
     * 
     * @author rinrinne a.k.a. rin_ne
     */
    private static class InstanceHolder {
        private static final RMQManager INSTANCE = new RMQManager();
    }

    private static final long TIMEOUT_CLOSE = 300000;
    private static final Logger LOGGER = Logger.getLogger(RMQManager.class.getName());

    private RMQConnection rmqConnection;
    private volatile boolean statusOpen = false;
    private CountDownLatch closeLatch = null;

    /**
     * Gets instance.
     * 
     * @return the instance.
     */
    public static RMQManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Updates RabbitMQ connection.
     */
    public void update() {
        LOGGER.info("Start to update connections...");
        String uri = GlobalRabbitmqConfiguration.get().getServiceUri();
        boolean enableConsumer = GlobalRabbitmqConfiguration.get().isEnableConsumer();

        try {
            if (!enableConsumer || uri == null) {
                if (rmqConnection != null) {
                    shutdownWithWait();
                    rmqConnection = null;
                }
            }
            if (rmqConnection != null && !uri.equals(rmqConnection.getServiceUri())) {
                if (rmqConnection != null) {
                    shutdownWithWait();
                    rmqConnection = null;
                }
            }

            if (enableConsumer) {
                if (rmqConnection == null) {
                    rmqConnection = new RMQConnection(uri);
                    rmqConnection.addRMQConnectionListener(this);
                    try {
                        rmqConnection.open();
                    } catch (IOException e) {
                        LOGGER.warning("Cannot open connection.");
                        return;
                    }
                }
                rmqConnection.updateChannels(GlobalRabbitmqConfiguration.get().getConsumeItems());
            }
        } catch (InterruptedException e) {
            LOGGER.warning("Interrupted when waiting to close connection.");
        }
    }

    /**
     * Shutdown connection.
     */
    public void shutdown() {
        if (rmqConnection != null) {
            rmqConnection.close();
        }
    }

    /**
     * Shutdown connection then wait to close connection.
     * 
     * @throws InterruptedException
     *             throw if wait process is interrupted.
     */
    public synchronized void shutdownWithWait() throws InterruptedException {
        if (rmqConnection != null && rmqConnection.isOpen()) {
            try {
                closeLatch = new CountDownLatch(1);
                shutdown();
                if (!closeLatch.await(TIMEOUT_CLOSE, TimeUnit.MILLISECONDS)) {
                    throw new InterruptedException("Wait timeout");
                }
            } finally {
                closeLatch = null;
            }
        }
    }

    /**
     * Gets whether connection is established or not.
     * 
     * @return true if connection is already established.
     */
    public boolean isOpen() {
        return statusOpen;
    }

    /**
     * Gets status of channel for specified queue.
     * 
     * @param queueName
     *            the queue name.
     * @return true if channel for specified queue is already established.
     */
    public boolean getChannelStatus(String queueName) {
        if (rmqConnection == null) {
            return false;
        } else {
            return rmqConnection.getChannelStatus(queueName);
        }
    }

    /**
     * @inheritDoc
     * @param rmqConnection
     *            the connection.
     */
    public void onOpen(RMQConnection rmqConnection) {
        LOGGER.info("Open RabbitMQ connection.");
        statusOpen = true;
    }

    /**
     * @inheritDoc
     * @param rmqConnection
     *            the connection.
     */
    public void onCloseCompleted(RMQConnection rmqConnection) {
        LOGGER.info("Closed RabbitMQ connection.");
        statusOpen = false;
        rmqConnection.removeRMQConnectionListener(this);
        rmqConnection = null;
        if (closeLatch != null) {
            closeLatch.countDown();
        }
    }

    /**
     * Creates instance.
     */
    private RMQManager() {
    }
}
