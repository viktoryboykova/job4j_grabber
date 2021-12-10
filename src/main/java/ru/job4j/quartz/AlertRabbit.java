package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {

    private Properties properties;

    public AlertRabbit(Properties properties) {
        this.properties = properties;
    }

    public static void main(String[] args) {
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            Properties properties = new Properties();
            properties.load(in);
            AlertRabbit alertRabbit = new AlertRabbit(properties);
            alertRabbit.executeScheduler();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void executeScheduler() throws ClassNotFoundException {
        Class.forName(properties.getProperty("hibernate.connection.driver_class"));
        try (Connection connection =
                    DriverManager.getConnection(properties.getProperty("hibernate.connection.url"), properties.getProperty("hibernate.connection.username"), properties.getProperty("hibernate.connection.password"))) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("connection", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(getIntervalInSeconds())
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public int getIntervalInSeconds() throws IOException {
        return Integer.parseInt(properties.getProperty("rabbit.interval"));
    }

    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ...");
            Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try (PreparedStatement statement =
                         connection.prepareStatement("insert into rabbit(created_date) values (?)")) {
                statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                statement.execute();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
