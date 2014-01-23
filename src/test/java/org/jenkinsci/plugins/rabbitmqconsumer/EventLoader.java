package org.jenkinsci.plugins.rabbitmqconsumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A class that load gerrit event from resource file.
 *
 * @author rinrinne a.k.a. rin_ne
 *
 */
public class EventLoader {

    private static final String EVENT_FILE = "events.txt";
    private BufferedReader reader = null;

    /**
     * Load event stream from resource file.
     *
     * @throws IOException throw if resource file is not found.
     */
    public void load() throws IOException {
        InputStream in = this.getClass().getResourceAsStream(EVENT_FILE);
        if (in == null) {
            throw new IOException("Resource not found: " + EVENT_FILE);
        }
        reader = new BufferedReader(new InputStreamReader(in));
    }

    /**
     * Read next event.
     *
     * @return the event string.
     * @throws IOException throw if error.
     */
    public String getNext() throws IOException {
        return reader.readLine();
    }

    /**
     * Dispose resource.
     *
     * @throws IOException throw if error.
     */
    public void dispose() throws IOException {
        reader.close();

    }
}
