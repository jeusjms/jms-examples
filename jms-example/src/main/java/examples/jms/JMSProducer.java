package examples.jms;

import javax.jms.*;
import java.util.Arrays;

class JMSProducer extends JMSMessageHandler {
    private Destination destination;


    JMSProducer(Connection connection) {
        super(connection);
        this.id = "[JMSProducer-" + idGenerator.getAndIncrement() + "]";
    }

    public void init(Destination destination) throws JMSException {
        this.destination = destination;
    }

    public void run() {
        try {

            long startTime = System.currentTimeMillis();
            long elapsed = System.currentTimeMillis() - startTime;

            int failedCount = 0;
            count = 0;
            while (elapsed < duration) {
                try {
                    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    MessageProducer producer = session.createProducer(destination);
                    Message message = setMessage();
                    producer.send(message);
                    count++;
                    if (DEBUG)
                        System.out.println(id + " send " + message + "[" + count + "]");

                    if (interval > 0) {
                        if ((System.currentTimeMillis() - startTime) >= duration)
                            break;
                        try {
                            Thread.sleep(interval);
                        } catch (InterruptedException ignore) {
                        }
                    }

                    session.close();
                    elapsed = System.currentTimeMillis() - startTime;
                } catch (JMSException e) {
                    failedCount++;
                    System.out.println(id +"Failed to send message(" + failedCount + "):" + e);
                    e.printStackTrace(System.out);

                    if (DEFAULT_FAILED_COUNT > 0 && failedCount > DEFAULT_FAILED_COUNT) {
                        System.out.println(id + "Stop sending by failure limit.");
                        break;
                    }
                    if (interval > 0) {
                        if ((System.currentTimeMillis() - startTime) >= duration)
                            break;
                        try {
                            long sleepTime = interval * 2;
                            if (sleepTime < 20)
                                sleepTime = 20;

                            Thread.sleep(sleepTime);
                        } catch (InterruptedException ignore) {
                        }
                    }
                }

            }
            System.out.println(id +"Sending " + count + "mesages during " + elapsed + "ms");
//        } catch (JMSException e) {
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException ignore) {
                } finally {
                    session = null;
                }
            }
        }
    }

    private Message setMessage() throws JMSException {
        try {
            if (type.equalsIgnoreCase("bytes")) {
                BytesMessage bytesMessage = session.createBytesMessage();
                byte[] b = new byte[Integer.parseInt(contents)];
                Arrays.fill(b, (byte) 9);
                bytesMessage.writeBytes(b);
                bytesMessage.writeLong(System.currentTimeMillis());

                return bytesMessage;
            } else {
                TextMessage textMessage = session.createTextMessage();
                textMessage.setText(contents + System.currentTimeMillis());

                return textMessage;
            }
        } catch (JMSException e) {
            System.out.println(id +"Failed to start: message create failed");
            e.printStackTrace(System.out);
            throw e;
        } catch (NumberFormatException e) {
            System.out.println(id +"Failed to start: invalid contents: " + contents);
            e.printStackTrace(System.out);
            throw new JMSException(e.getMessage());
        }
    }
}
