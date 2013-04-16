package org.jenkinsci.plugins.rabbitmqconsumer;

import java.util.logging.Logger;

import hudson.Extension;
import hudson.model.listeners.ItemListener;

/**
 * Implements of {@link ItemListener}.
 * 
 * @author rinrinne a.k.a. rin_ne
 */
@Extension
public class ItemListenerImpl extends ItemListener {

    private static final Logger LOGGER = Logger.getLogger(ItemListenerImpl.class.getName());

    private final RMQManager rmqManager;

    /**
     * Creates instance from this class.
     */
    public ItemListenerImpl() {
        super();
        this.rmqManager = RMQManager.getInstance();
    }

    @Override
    public final void onLoaded() {
        LOGGER.info("Start bootup process.");
        rmqManager.update();
        super.onLoaded();
    }

    @Override
    public final void onBeforeShutdown() {
        rmqManager.shutdown();
        super.onBeforeShutdown();
    }

    /**
     * Gets this extension's instance.
     * 
     * @return the instance of this extension.
     */
    public static ItemListenerImpl get() {
        return ItemListener.all().get(ItemListenerImpl.class);
    }
}
