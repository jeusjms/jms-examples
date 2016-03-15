package examples.test;

import examples.monitoring.JEUSMBeanMonitor;
import examples.monitoring.MonitorExecutor;

import java.io.IOException;
import java.util.Date;

/**
 * @author donghwan
 */
public class Monitoring {
    public static void main(String[] args) {
        try {
            TestExecutor.printEnv();
            TestExecutor.startJeus();
            Thread.sleep(TestEnvironment.WARMING_UP_TIME);
            MonitorExecutor monitor = new MonitorExecutor();
            monitor.start();

            Thread.sleep(TestEnvironment.DURATION);

            monitor.quit();
            TestExecutor.stopJeus();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
