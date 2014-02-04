package org.jenkinsci.plugins.rabbitmqconsumer;

import java.util.HashSet;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.rabbitmqconsumer.extensions.MessageQueueListener;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.rabbitmq.client.Channel;

/**
 * Item class that indicates queue setting in global configuration.
 *
 * @author rinrinne a.k.a. rin_ne
 *
 */
public class RabbitmqConsumeItem extends AbstractDescribableImpl<RabbitmqConsumeItem> {

    /**
     * App ID for debug.
     */
    public static final String DEBUG_APPID = "*";

    private String appId = null;
    private String queueName = null;

    /**
     * Creates instance with specific parameters.
     *
     * @param appId
     *            the application id.
     * @param queueName
     *            the queue name.
     */
    @DataBoundConstructor
    public RabbitmqConsumeItem(String appId, String queueName) {
        this.appId = StringUtils.stripToNull(appId);
        this.queueName = StringUtils.stripToNull(queueName);
    }

    /**
     * Creates instance with no parametesrs.
     */
    public RabbitmqConsumeItem() {
    }

    /**
     * Gets application id.
     *
     * @return the application id.
     */
    public final String getAppId() {
        return appId;
    }

    /**
     * Sets application id.
     *
     * @param appId the application id.
     */
    public final void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * Gets queue name.
     *
     * @return the queue name.
     */
    public final String getQueueName() {
        return queueName;
    }

    /**
     * Sets queue name.
     *
     * @param queueName the queue name.
     */
    public final void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    // CHECKSTYLE:OFF
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((appId == null) ? 0 : appId.hashCode());
        result = prime * result + ((queueName == null) ? 0 : queueName.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RabbitmqConsumeItem other = (RabbitmqConsumeItem) obj;
        if (appId == null) {
            if (other.appId != null)
                return false;
        } else if (!appId.equals(other.appId))
            return false;
        if (queueName == null) {
            if (other.queueName != null)
                return false;
        } else if (!queueName.equals(other.queueName))
            return false;
        return true;
    }

    // CHECKSTYLE:ON

    /**
     * Implements descriptor for parent class.
     *
     * @author rinrinne a.k.a. rin_ne
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<RabbitmqConsumeItem> {
        @Override
        public String getDisplayName() {
            return "";
        }

        /**
         * Fills dropdown list in global configuration using app ids.
         *
         * @return ListBoxModel instance that is filled by app ids.
         */
        public ListBoxModel doFillAppIdItems() {
            ListBoxModel items = new ListBoxModel();
            HashSet<String> appIds = new HashSet<String>();

            for (MessageQueueListener l : MessageQueueListener.all()) {
                appIds.add(l.getAppId());
            }
            appIds.remove(null);
            appIds.remove(DEBUG_APPID);

            items.add(GlobalRabbitmqConfiguration.CONTENT_NONE, GlobalRabbitmqConfiguration.CONTENT_NONE);
            if (GlobalRabbitmqConfiguration.get().isEnableDebug()) {
                items.add(DEBUG_APPID, DEBUG_APPID);
            }

            for (String appId : appIds) {
                items.add(appId, appId);
            }
            return items;
        }

        /**
         * Check given queue name.
         *
         * @param value the field value named queueName.
         * @return ok if no problem.
         */
        public FormValidation doCheckQueueName(@QueryParameter String value) {
            if (StringUtils.stripToNull(value) != null) {
                if (RMQManager.getInstance().isOpen()) {
                    Channel ch = RMQManager.getInstance().getChannel();
                    if (ch != null) {
                        try {
                            ch.queueDeclarePassive(StringUtils.strip(value));
                            return FormValidation.ok();
                        } catch (Exception ex) {
                            return FormValidation.error("Not found.");
                        }
                    }
                }
            }
            return FormValidation.ok();
        }
    }
}
