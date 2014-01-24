package org.jenkinsci.plugins.rabbitmqconsumer;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * A Test for Plugin on Jenkins.
 *
 * @author rinrinne a.k.a. rin_ne
 */
public class PluginTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testPluginLoad() {
        GlobalRabbitmqConfiguration config = GlobalRabbitmqConfiguration.get();
        assertNotNull("Not found instance: GlobalRabbitmqConfiguration", config);
        assertFalse(config.isEnableConsumer());
    }

}
