package org.jenkinsci.plugins.rabbitmqconsumer;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for EventLoader class.
 *
 * @author rinrinne a.k.a. rin_ne
 *
 */
public class EventLoaderTest {

    EventLoader loader = new EventLoader();

    @Test
    public void test() {
        try {
            loader.load();
            String msg = loader.getNext();

            assertNotNull(msg);
            assertFalse(msg.isEmpty());
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

}
