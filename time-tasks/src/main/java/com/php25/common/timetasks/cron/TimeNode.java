package com.php25.common.timetasks.cron;


import java.time.temporal.ChronoUnit;

/**
 * @author penghuiping
 * @date 2020/6/4 17:59
 */
public class TimeNode {
    /**
     * 时间范围，比如unit是秒，times为0~59
     */
    private int[] times;
    private TimeNode next;
    private TimeNode previous;
    /**
     * 表示时间范围times 中某一个时间点
     */
    private int index;
    private ChronoUnit unit;

    public TimeNode(int[] times, TimeNode next, int index, ChronoUnit unit) {
        this.times = times;
        this.next = next;
        this.index = index;
        this.unit = unit;
    }

    public TimeNode getPrevious() {
        return previous;
    }

    public void setPrevious(TimeNode previous) {
        this.previous = previous;
    }

    public int[] getTimes() {
        return times;
    }

    public void setTimes(int[] times) {
        this.times = times;
    }

    public TimeNode getNext() {
        return next;
    }

    public void setNext(TimeNode next) {
        this.next = next;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public ChronoUnit getUnit() {
        return unit;
    }

    public void setUnit(ChronoUnit unit) {
        this.unit = unit;
    }
}