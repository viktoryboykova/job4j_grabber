package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {

    private Properties properties;

    public AlertRabbit(Properties properties) {
        this.properties = properties;
    }

    public static void main(String[] args) throws IOException {
        try(InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            Properties properties = new Properties();
            properties.load(in);
            AlertRabbit alertRabbit = new AlertRabbit(properties);
            alertRabbit.executeScheduler();
        }
    }

    public void executeScheduler() {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(getIntervalInSeconds())
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException | IOException se) {
            se.printStackTrace();
        }
    }

    public int getIntervalInSeconds() throws IOException {
        return Integer.parseInt(properties.getProperty("rabbit.interval"));
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ...");
        }
    }
}
