package org.jenkinsci.plugins.rabbitmqconsumer;

import static org.junit.Assert.*;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.jenkinsci.plugins.rabbitmqconsumer.Mocks.ServerOperatorMock;
import org.jenkinsci.plugins.rabbitmqconsumer.channels.ConsumeRMQChannel;
import org.jenkinsci.plugins.rabbitmqconsumer.channels.ControlRMQChannel;
import org.jenkinsci.plugins.rabbitmqconsumer.extensions.MessageQueueListener;
import org.jenkinsci.plugins.rabbitmqconsumer.extensions.ServerOperator;
import org.jenkinsci.plugins.rabbitmqconsumer.listeners.RMQConnectionListener;
import org.jenkinsci.plugins.rabbitmqconsumer.watchdog.ReconnectTimer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Test for RMQConnection class.
 *
 * @author rinrinne a.k.a. rin_ne
 *
 */
public class RMQConnectionTest {

    @Mocked
    ConnectionFactory factory = new ConnectionFactory();

    @Mocked
    ReconnectTimer timer = new ReconnectTimer();

    @Mocked
    MessageQueueListener mqListener = null;     /* dummy */

    @Mocked
    ServerOperator serverOperator = null;       /* dummy */

    @Mocked
    Connection connection;

    RMQConnectionListener connListener = new Mocks.RMQConnectionListenerMock();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        new Mocks.RMQConnectionMock();
        new Mocks.ConsumeRMQChannelMock();
        new Mocks.ControlRMQChannelMock();

        Mocks.operatorSet.add(new Mocks.ServerOperatorMock());
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {

        new NonStrictExpectations() {{
            connection.createChannel(); result = new Mocks.ChannelMock().getMockInstance();
            MessageQueueListener.all();
            factory.setConnectionTimeout(anyInt);
            factory.setRequestedHeartbeat(anyInt);
            factory.setUri(anyString);
            factory.newConnection(); result = connection;
            ReconnectTimer.get(); result = timer;
            timer.start();
            timer.stop();

            ServerOperator.fireOnOpen((ControlRMQChannel) any);
            result = new Mocks.OnOpenDelegation();

            ServerOperator.fireOnCloseCompleted((ControlRMQChannel) any);
            result = new Mocks.OnCloseCompletedDelegation();

            ServerOperator.fireOnOpenConsumer((ControlRMQChannel) any, anyString, (HashSet<String>) any);
            result = new Mocks.OnOpenConsumerDelegation();

            ServerOperator.fireOnClosedConsumer((ControlRMQChannel) any, anyString, (HashSet<String>) any);
            result = new Mocks.OnClosedConsumerDelegation();
        }};
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testOpenChannels() {
        RMQConnection conn = new RMQConnection("", "", null);
        conn.addRMQConnectionListener(connListener);
        List<RabbitmqConsumeItem> items = new ArrayList<RabbitmqConsumeItem>();
        items.add(new RabbitmqConsumeItem("app-1-a", "queue-1"));
        items.add(new RabbitmqConsumeItem("app-1-b", "queue-1"));
        items.add(new RabbitmqConsumeItem("app-2", "queue-2"));
        items.add(new RabbitmqConsumeItem("app-3", "queue-3"));

        HashSet<String> queueNameSet = new HashSet<String>();
        queueNameSet.addAll(Arrays.asList("queue-1","queue-2","queue-3"));

        try {
            conn.open();
            conn.updateChannels(items);
            Set<ConsumeRMQChannel> channels = conn.getConsumeRMQChannels();
            assertEquals(3, channels.size());
            for (ConsumeRMQChannel ch : channels) {
                assertTrue(queueNameSet.contains(ch.getQueueName()));
                if ("queue-1".equals(ch.getQueueName())) {
                    assertEquals(2, ch.getAppIds().size());
                } else {
                    assertEquals(1, ch.getAppIds().size());
                }
            }
            conn.close();
        } catch (Exception ex) {
            fail(ex.toString());
        }
    }

    @Test
    public void testAddChannels() {
        RMQConnection conn = new RMQConnection("", "", null);
        conn.addRMQConnectionListener(connListener);
        List<RabbitmqConsumeItem> items = new ArrayList<RabbitmqConsumeItem>();
        items.add(new RabbitmqConsumeItem("app-1-a", "queue-1"));
        items.add(new RabbitmqConsumeItem("app-1-b", "queue-1"));
        items.add(new RabbitmqConsumeItem("app-2", "queue-2"));
        items.add(new RabbitmqConsumeItem("app-3", "queue-3"));

        try {
            Set<ConsumeRMQChannel> channels;
            conn.open();
            conn.updateChannels(items);
            channels = conn.getConsumeRMQChannels();
            assertEquals(3, channels.size());

            items.add(new RabbitmqConsumeItem("app-4", "queue-4"));
            conn.updateChannels(items);
            channels = conn.getConsumeRMQChannels();
            assertEquals(4, channels.size());

            conn.close();
        } catch (Exception ex) {
            fail(ex.toString());
        }
    }

    @Test
    public void testDeleteChannels() {
        RMQConnection conn = new RMQConnection("", "", null);
        conn.addRMQConnectionListener(connListener);
        RabbitmqConsumeItem item = new RabbitmqConsumeItem("app-4", "queue-4");
        List<RabbitmqConsumeItem> items = new ArrayList<RabbitmqConsumeItem>();
        items.add(new RabbitmqConsumeItem("app-1-a", "queue-1"));
        items.add(new RabbitmqConsumeItem("app-1-b", "queue-1"));
        items.add(new RabbitmqConsumeItem("app-2", "queue-2"));
        items.add(new RabbitmqConsumeItem("app-3", "queue-3"));
        items.add(item);

        try {
            Set<ConsumeRMQChannel> channels;
            conn.open();
            conn.updateChannels(items);
            channels = conn.getConsumeRMQChannels();
            assertEquals(4, channels.size());

            items.remove(item);
            conn.updateChannels(items);
            channels = conn.getConsumeRMQChannels();
            assertEquals(3, channels.size());

            conn.close();
        } catch (Exception ex) {
            fail(ex.toString());
        }
    }

    @Test
    public void testDeleteAndAddChannels() {
        RMQConnection conn = new RMQConnection("", "", null);
        conn.addRMQConnectionListener(connListener);
        RabbitmqConsumeItem item3 = new RabbitmqConsumeItem("app-3", "queue-3");
        RabbitmqConsumeItem item4 = new RabbitmqConsumeItem("app-4", "queue-4");
        List<RabbitmqConsumeItem> items = new ArrayList<RabbitmqConsumeItem>();
        items.add(new RabbitmqConsumeItem("app-1-a", "queue-1"));
        items.add(new RabbitmqConsumeItem("app-1-b", "queue-1"));
        items.add(new RabbitmqConsumeItem("app-2", "queue-2"));
        items.add(item3);

        try {
            Set<ConsumeRMQChannel> channels;
            conn.open();
            conn.updateChannels(items);
            channels = conn.getConsumeRMQChannels();
            assertEquals(3, channels.size());

            items.remove(item3);
            items.add(item4);
            conn.updateChannels(items);
            channels = conn.getConsumeRMQChannels();
            assertEquals(3, channels.size());
            HashSet<String> queueNames = new HashSet<String>();
            for (ConsumeRMQChannel ch : channels) {
                queueNames.add(ch.getQueueName());
            }
            assertFalse(queueNames.contains("queue-3"));
            assertTrue(queueNames.contains("queue-4"));
            conn.close();
        } catch (Exception ex) {
            fail(ex.toString());
        }
    }

}
