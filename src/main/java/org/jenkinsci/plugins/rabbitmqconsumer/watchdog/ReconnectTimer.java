package org.jenkinsci.plugins.rabbitmqconsumer.watchdog;

import org.jenkinsci.plugins.rabbitmqconsumer.GlobalRabbitmqConfiguration;
import org.jenkinsci.plugins.rabbitmqconsumer.RMQManager;

import hudson.Extension;
import hudson.model.AperiodicWork;

/**
 * Reconnect timer class.
 *
 * @author rinrinne a.k.a. rin_ne
 */
@Extension
public class ReconnectTimer extends AperiodicWork {

    private static final long DEFAULT_RECCURENCE_TIME = 300000;
    private static final long INITIAL_DELAY_TIME = 600000;

    private volatile boolean stopRequested;
    private volatile boolean updateChannelRequested;
    private long reccurencePeriod;

    /**
     * Creates instance.
     */
    public ReconnectTimer() {
        this(DEFAULT_RECCURENCE_TIME, false, false);
    }

    /**
     * Creates instance with specified parameters.
     *
     * @param reccurencePeriod
     *            the reccurence period in millis.
     * @param stopRequested
     *            true if stop timer is requested.
     */
    public ReconnectTimer(long reccurencePeriod, boolean stopRequested, boolean updateChannelRequested) {
        this.reccurencePeriod = reccurencePeriod;
        this.stopRequested = stopRequested;
        this.updateChannelRequested = updateChannelRequested;
    }

    @Override
    public long getRecurrencePeriod() {
        return reccurencePeriod;
    }

    /**
     * Sets recurrence period.
     *
     * @param reccurencePeriod
     *            the recurrnce period in millis.
     */
    public void setRecurrencePeriod(long reccurencePeriod) {
        this.reccurencePeriod = reccurencePeriod;
    }

    /**
     * Request updating channel.
     */
    public void updateChannel() {
        updateChannelRequested = true;
    }

    @Override
    public long getInitialDelay() {
        return INITIAL_DELAY_TIME;
    }

    @Override
    public AperiodicWork getNewInstance() {
        return new ReconnectTimer(reccurencePeriod, stopRequested, updateChannelRequested);
    }

    @Override
    protected void doAperiodicRun() {
        if (!stopRequested) {
            RMQManager manager = RMQManager.getInstance();
            GlobalRabbitmqConfiguration config = GlobalRabbitmqConfiguration.get();

            if (config.isEnableConsumer()) {
                if (!manager.isOpen()) {
                    logger.info("watchdog: Reconnect requesting..");
                    RMQManager.getInstance().update();
                    updateChannelRequested = false;
                } else if (updateChannelRequested) {
                    logger.info("watchdog: channel update requesting..");
                    RMQManager.getInstance().update();
                    updateChannelRequested = false;
                }
            }
        }
    }

    /**
     * Stops periodic run.
     */
    public void stop() {
        stopRequested = true;
    }

    /**
     * Starts periodic run.
     */
    public void start() {
        stopRequested = false;
    }

    /**
     * Gets this extension from extension list.
     *
     * @return the instance of this plugin.
     */
    public static ReconnectTimer get() {
        return AperiodicWork.all().get(ReconnectTimer.class);
    }
}
