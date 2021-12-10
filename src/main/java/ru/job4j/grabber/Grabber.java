package ru.job4j.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.grabber.utils.SqlRuDateTimeParser;
import ru.job4j.html.SqlRuParse;

import java.io.*;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {

    private static final Logger LOG = LoggerFactory.getLogger(Grabber.class.getName());

    private final Properties properties = new Properties();

    public Store store() {
        return new PsqlStore(properties);
    }

    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    public void cfg() throws IOException {
        try (InputStream in = Grabber.class.getClassLoader().getResourceAsStream("app.properties")) {
            properties.load(in);
        }
    }

    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(Integer.parseInt(properties.getProperty("time")))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public static class GrabJob implements Job {

        @Override
        public void execute(JobExecutionContext context) {
            try {
                JobDataMap map = context.getJobDetail().getJobDataMap();
                Store store = (Store) map.get("store");
                Parse parse = (Parse) map.get("parse");
                List<Post> list = store.getAll();
                for (int i = 1; i <= 5; i++) {
                    List<Post> posts = parse.list(String.format("https://www.sql.ru/forum/job-offers/%s", i));
                    for (Post post : posts) {
                        if (!list.contains(post)) {
                            store.save(post);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("", e);
            }
        }

        public static void main(String[] args) throws Exception {
            Grabber grab = new Grabber();
            grab.cfg();
            Scheduler scheduler = grab.scheduler();
            Store store = grab.store();
            SqlRuDateTimeParser sqlRuDateTimeParser = new SqlRuDateTimeParser();
            grab.init(new SqlRuParse(sqlRuDateTimeParser), store, scheduler);
        }
    }
}
