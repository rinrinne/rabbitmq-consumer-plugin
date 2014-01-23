package org.jenkinsci.plugins.rabbitmqconsumer.watchdog;

import mockit.Mocked;
import mockit.NonStrictExpectations;

import static org.junit.Assert.*;

import org.jenkinsci.plugins.rabbitmqconsumer.GlobalRabbitmqConfiguration;
import org.jenkinsci.plugins.rabbitmqconsumer.RMQManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for ReconnectTimer class.
 *
 * @author rinrinne a.k.a. rin_ne
 *
 */
public class ReconnectTimerTest {

    @Mocked
    RMQManager manager;

    @Mocked
    GlobalRabbitmqConfiguration config;

    ReconnectTimer timer = new ReconnectTimer();

    @Before
    public void setUp() throws Exception {
        new NonStrictExpectations() {{
            RMQManager.getInstance(); result = manager;
            GlobalRabbitmqConfiguration.get(); result = config;
        }};
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSetRecurrencePeriod() {
        timer.setRecurrencePeriod(1000);
        assertEquals(1000, timer.getRecurrencePeriod());
    }

    @Test
    public void testIfAllGrean() {
        new NonStrictExpectations() {{
            manager.isOpen(); result = true;
            config.isEnableConsumer(); result = true;
            manager.update(); times = 0;
        }};

        timer.start();
        timer.doAperiodicRun();
        timer.stop();
    }

    @Test
    public void testIfManagerIsClosed() {
        new NonStrictExpectations() {{
            manager.isOpen(); result = false;
            config.isEnableConsumer(); result = true;
            manager.update(); times = 1;
        }};

        timer.start();
        timer.doAperiodicRun();
        timer.stop();
    }

    @Test
    public void testIfConsumerIsDisabled() {
        new NonStrictExpectations() {{
            manager.isOpen(); result = true;
            config.isEnableConsumer(); result = false;
            manager.update(); times = 0;
        }};

        timer.start();
        timer.doAperiodicRun();
        timer.stop();
    }

    @Test
    public void testIfAllNegative() {
        new NonStrictExpectations() {{
            manager.isOpen(); result = false;
            config.isEnableConsumer(); result = false;
            manager.update(); times = 0;
        }};

        timer.start();
        timer.doAperiodicRun();
        timer.stop();
    }

    @Test
    public void testDoAperiodicRunInShutdown() {
        new NonStrictExpectations() {{
            manager.isOpen(); result = false;
            config.isEnableConsumer(); result = true;
            manager.update(); times = 0;
        }};

        timer.stop();
        timer.doAperiodicRun();
    }
}
