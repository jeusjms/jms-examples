package examples.jms;

import javax.jms.*;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class JMSConsumer extends JMSMessageHandler implements MessageListener {
    private MessageConsumer consumer;
    private PrintWriter writer;

    private boolean async = false;

    private static final boolean PERFORMANCE = Boolean.parseBoolean(System.getProperty("examples.test.consume-performance", "false"));
    static {
        if (PERFORMANCE) {
            System.out.println("profile enabled");
            System.setProperty("jeus.jms.server.profiling", "true");
        }
    }

    JMSConsumer(Connection connection, boolean async) {
        super(connection);
        this.id = "[JMSConsumer-" + idGenerator.getAndIncrement() + "]";
        this.async = async;

    }

    public void init(Destination destination) throws JMSException, FileNotFoundException {
        super.init(destination);
        consumer = session.createConsumer(destination);
        connection.start();
    }


    public void setInterval(long interval) {
        this.interval = interval;
    }

    public void run() {
        try {
            if (PERFORMANCE)
                writer = new PrintWriter("result/elapsed/JMSConsumer" + System.currentTimeMillis() + ".csv");
            long startTime = System.currentTimeMillis();
            long elapsed = System.currentTimeMillis() - startTime;

            int failedCount = 0;
            while (elapsed < duration) {
                if (async) {
                    try {
                        if (consumer.getMessageListener() == null)
                            consumer.setMessageListener(this);
                        else
                            Thread.sleep(interval + 1000);


                    } catch (JMSException ignore) {
                        // never occured
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Message message = consumer.receive(1000);
                        onMessage(message);
                    } catch (JMSException e) {
                        failedCount++;
                        System.out.println(id +"Failed to send message(" + failedCount + ")");
                        if (failedCount > DEFAULT_FAILED_COUNT) {
                            System.out.println(id + "Stop sending by failure limit.");
                            break;
                        }
                    }
                }

                if ((System.currentTimeMillis() - startTime) >= duration)
                    break;

                if (interval > 0) {
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException ignore) {
                    }
                }

                elapsed = System.currentTimeMillis() - startTime;
            }
            consumer.setMessageListener(null);
            System.out.println(id + "Cosuming " + count + "mesages during " + elapsed + "ms. average elapsed:" + averageElapsed + "ns");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException ignore) {
                } finally {
                    session = null;
                }
            }

            if (writer != null) {
                writer.close();
            }

        }

    }

    double averageElapsed = 0.0;

    public void onMessage(Message message) {
        if (message == null)
            return;
        long start = System.nanoTime();
        count++;

        if (type.equalsIgnoreCase("bytes")) {
            if (message instanceof BytesMessage) {
                BytesMessage bytesMessage = (BytesMessage) message;
                try {
                    byte[] bytes = new byte[Integer.parseInt(contents)];
                    bytesMessage.readBytes(bytes);
                    long t = bytesMessage.readLong();
                    if (writer != null)
                        writer.println(System.currentTimeMillis() - t);


                    if (DEBUG)
                        System.out.println(id + " receive[" + count + "]" + message);

                } catch (JMSException e) {
                    System.out.println(id + "Failed to handle received message:" + message);
                    e.printStackTrace(System.out);
                } catch (NumberFormatException e) {
                    System.out.println(id + "Invalid contents: " + contents);
                    e.printStackTrace(System.out);
                }
            } else {
                System.out.println(id+ "Invalid type of message:" + message);
            }
        }
        long elapsed = System.nanoTime() - start;
        averageElapsed = (elapsed + (averageElapsed * (count - 1))) / count;

    }
}
