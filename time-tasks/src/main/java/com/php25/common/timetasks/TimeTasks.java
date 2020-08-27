package com.php25.common.timetasks;

import com.php25.common.timetasks.cron.Cron;
import com.php25.common.timetasks.timewheel.TimeTask;
import com.php25.common.timetasks.timewheel.TimeWheel;

import java.time.LocalDateTime;

/**
 * @author penghuiping
 * @date 2020/6/9 14:27
 */
public class TimeTasks {

    public static void submit(TimeWheel timeWheel, String cron, Runnable task) {
        execute0(timeWheel, cron, task);
    }

    private static void execute0(TimeWheel timeWheel, String cron, Runnable task) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = Cron.nextExecuteTime(cron, now.plusSeconds(2));
        if (null != next) {
            TimeTask timeTask = new TimeTask(next, task, timeWheel.generateJobId(), cron);
            timeWheel.add(timeTask);
        }
    }
}