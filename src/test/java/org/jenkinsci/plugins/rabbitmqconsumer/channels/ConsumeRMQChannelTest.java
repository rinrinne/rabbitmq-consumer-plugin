package org.jenkinsci.plugins.rabbitmqconsumer.channels;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.apache.commons.codec.CharEncoding;
import org.jenkinsci.plugins.rabbitmqconsumer.Mocks;
import org.jenkinsci.plugins.rabbitmqconsumer.extensions.MessageQueueListener;
import org.jenkinsci.plugins.rabbitmqconsumer.listeners.RMQChannelListener;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;

/**
 * Test for ConsumeRMQChannel class.
 *
 * @author rinrinne a.k.a. rin_ne
 */
public class ConsumeRMQChannelTest {

    @Mocked
    Connection connection;

    @Mocked
    MessageQueueListener mqListener = null;     /* dummy */

    RMQChannelListener chListener = new Mocks.RMQChannelListenerMock();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        new Mocks.ConsumeRMQChannelMock();

        MessageQueueListener listener;
        listener = new Mocks.MessageQueueListenerMock("listener-1", "app-1");
        Mocks.mqListenerSet.add(listener);

        listener = new Mocks.MessageQueueListenerMock("listener-2", "app-2");
        Mocks.mqListenerSet.add(listener);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        new NonStrictExpectations() {{
            connection.createChannel(); result = new Mocks.ChannelMock().getMockInstance();

            MessageQueueListener.fireOnBind((HashSet<String>) any, anyString);
            result = new Mocks.OnBindDelegation();

            MessageQueueListener.fireOnUnbind((HashSet<String>) any, anyString);
            result = new Mocks.OnUnbindDelegation();

            MessageQueueListener.fireOnReceive(anyString,
                    anyString,
                    anyString,
                    (Map<String, Object>) any,
                    (byte[]) any);
            result = new Mocks.OnReceiveDelegation();
        }};
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConsume() {
        HashSet<String> appIds = new HashSet<String>();
        appIds.addAll(Arrays.asList("app-1" ,"app-2", "app-3"));
        Envelope envelope = new Envelope(0L, false, "exchange-1", "test.app");

        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        builder.appId("app-1");
        builder.contentEncoding(CharEncoding.UTF_8);
        builder.contentType("application/json");

        AMQP.BasicProperties props = builder.build();

        ConsumeRMQChannel channel = new ConsumeRMQChannel("theQueue", appIds);
        channel.addRMQChannelListener(chListener);
        try {
            channel.open(connection);
            channel.consume();

            Consumer consumer = Mocks.consumerPool.pop();
            consumer.handleDelivery("consumerTag", envelope, props, "Test message".getBytes());

            assertEquals("Unmatched response size", 1, Mocks.responseArray.size());
            assertThat("Unmatch consumed queue.", Mocks.responseArray.get(0), is("listener-1"));
            channel.close();
        } catch (Exception ex) {
            fail(ex.toString());
        }
    }

}
