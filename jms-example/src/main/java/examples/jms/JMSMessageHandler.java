package examples.jms;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import java.io.FileNotFoundException;
import java.util.concurrent.atomic.AtomicInteger;

public class JMSMessageHandler extends Thread {
    protected static final AtomicInteger idGenerator = new AtomicInteger(0);
    protected static final int DEFAULT_FAILED_COUNT;
    protected static final boolean DEBUG;
    protected static final int LOG_UNIT = 500;

    protected String id;
    protected Connection connection;
    protected Session session;

    protected long interval = 10;
    protected long duration = 1000;

    protected String type;
    protected String contents;

    protected int count = 0;

    static {
        int count = 3;
        boolean debug = false;
        try {
            count = Integer.parseInt(System.getProperty("jms.test.failed-count", "-1"));
            debug = Boolean.parseBoolean(System.getProperty("jms.test.debug", "false"));
        } catch (Throwable ignore) {
        }

        DEBUG = debug;
        DEFAULT_FAILED_COUNT = count;
    }

    public JMSMessageHandler(Connection connection) {
        this.connection = connection;
    }


    public void init(Destination destination) throws JMSException, FileNotFoundException {
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public void startProcess(long duration, String type, String contents) {
        this.duration = duration;
        this.type = type;
        this.contents = contents;

        start();
    }

    public void setInterval(long interval) {
        if (interval <= 0) {
            if (DEBUG)
                System.out.println("Invalid interval=" + interval);
            return;
        }

        this.interval = interval;
    }

    public boolean isEnd() {
        return session == null;
    }

    public int getCount() {
        int c = count;
        count = 0;
        return c;
    }
}
