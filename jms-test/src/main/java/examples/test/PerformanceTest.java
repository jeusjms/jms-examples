package examples.test;

import examples.jms.JMSClient;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.io.FileNotFoundException;

/**
 * @author donghwan
 */
public class PerformanceTest extends Thread {
    private TestCompletionListener testCompletionListener;
    
    // options setting
    String dest = "ExamplesQueue";
    long duration = 5*1000;
    String type = "bytes";
    String contents = String.valueOf(2048);
    int sessionCount = 2;
    long interval = 0;
    boolean send = true;
    boolean async = false;
    boolean forceQuit = false;

    public static void main(String[] args) {
        // options setting
        String dest = "ExamplesQueue";
        long duration = 5*1000;
        String type = "bytes";
        String contents = String.valueOf(2048);
        int sessionCount = 2;
        long interval = 0;
        boolean send = false;
        boolean async = false;
        boolean forceQuit = false;

        for (String arg : args) {
            String[] a = arg.split("=");
            if (a.length == 2) {
                if (a[0].equalsIgnoreCase("dest")) {
                    dest = a[1];
                } else if (a[0].equalsIgnoreCase("duration")) {
                    duration = Long.parseLong(a[1]);
                } else if (a[0].equalsIgnoreCase("type")) {
                    type = a[1];
                } else if (a[0].equalsIgnoreCase("contents")) {
                    contents = a[1];
                } else if (a[0].equalsIgnoreCase("sessions")) {
                    sessionCount = Integer.parseInt(a[1]);
                } else if (a[0].equalsIgnoreCase("async")) {
                    async = Boolean.parseBoolean(a[1]);
                } else if (a[0].equalsIgnoreCase("send")) {
                    send =  Boolean.parseBoolean(a[1]);
                } else if (a[0].equalsIgnoreCase("interval")) {
                    interval = Long.parseLong(a[1]);
                } else if (a[0].equalsIgnoreCase("force")) {
                    forceQuit = Boolean.parseBoolean(a[1]);
                }
            }
        }

        System.out.println("START-JMS-TEST:d=" + dest + ";s="+sessionCount + ";send=" + send + ";a=" + async);
        PerformanceTest test = new PerformanceTest();
        test.setParameter(dest, duration, type, contents, sessionCount, interval, send, async);
        test.run();
    }

    public void doTest(String dest, long duration, String type, String contents, int sessionCount, long interval, boolean send, boolean async) throws NamingException, JMSException {
        setParameter(dest, duration, type, contents, sessionCount, interval, send, async);
        
        start();
    }

    private void setParameter(String dest, long duration, String type, String contents, int sessionCount, long interval, boolean send, boolean async) {
        this.dest = dest;
        this.duration = duration;
        this.type = type;
        this.contents = contents;
        this.sessionCount = sessionCount;
        this.interval = interval;
        this.send = send;
        this.async = async;
    }

    public void run() {
        try {
            // clients start
            String[] dests = dest.split(",");


            JMSClient jmsClient = new JMSClient(TestEnvironment.INITIAL_CONTEXT, TestEnvironment.URL_PKG_PREFIXES,
                    TestEnvironment.HOST, TestEnvironment.PORT, TestEnvironment.CONNECTION_FACTORY,
                    TestEnvironment.USER_NAME, TestEnvironment.PASSWORD);

            System.out.println("START TEST-" + (send ? "SEND" : "CONSUME") + "-FOR [" + dest + "] WITH " + sessionCount);


            int destIdx = 0;

            for (int i = 0; i < sessionCount; i++) {
                String dest = dests[destIdx++];
                if (send) {
                    jmsClient.produce(dest, duration, type, contents, interval);
                } else {
                    jmsClient.consume(dest, duration, type, contents, async, interval);
                }

                if (destIdx >= dests.length)
                    destIdx = 0;
            }

            jmsClient.close(forceQuit);

            if (testCompletionListener != null)
                testCompletionListener.onComplete();
        } catch (JMSException e) {
            e.printStackTrace(System.out);
        } catch (NamingException e) {
            e.printStackTrace(System.out);
        } catch (FileNotFoundException e) {
            e.printStackTrace(System.out);
        }
    }

    public void setTestCompletionListener(TestCompletionListener testCompletionListener) {
        this.testCompletionListener = testCompletionListener;
    }
}
