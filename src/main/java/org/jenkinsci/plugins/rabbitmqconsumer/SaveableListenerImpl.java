package org.jenkinsci.plugins.rabbitmqconsumer;

import hudson.Extension;
import hudson.XmlFile;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;

import java.util.logging.Logger;

/**
 * Implement class for {@link SaveableListener}.
 * 
 * @author rinrinne a.k.a. rin_ne
 */
@Extension
public class SaveableListenerImpl extends SaveableListener {

    private static final Logger LOGGER = Logger.getLogger(SaveableListenerImpl.class.getName());

    @Override
    public final void onChange(Saveable o, XmlFile file) {
        if (o instanceof GlobalRabbitmqConfiguration) {
            LOGGER.info("RabbitMQ configuration is updated, so update connection...");
            RMQManager.getInstance().update();
        }
        super.onChange(o, file);
    }

    /**
     * Gets instance of this extension.
     * 
     * @return the instance of this extension.
     */
    public static SaveableListenerImpl get() {
        return SaveableListener.all().get(SaveableListenerImpl.class);
    }
}
