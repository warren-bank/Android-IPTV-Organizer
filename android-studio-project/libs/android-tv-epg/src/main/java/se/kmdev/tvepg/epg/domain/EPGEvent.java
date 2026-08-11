package se.kmdev.tvepg.epg.domain;

import java.util.Calendar;

/**
 * Created by Kristoffer.
 */
public class EPGEvent {

    public static long UNDEFINED_TIMESTAMP = -1L;

    private static final long MILLISECONDS_PER_YEAR  = 31_536_000_000L;
    private static final long INDETERMINATE_END_TIME = System.currentTimeMillis() + MILLISECONDS_PER_YEAR;

    private long start;
    private long end;
    private String title;
    private String description;

    public EPGEvent(long start, long end, String title, String description) {
        setStart(start);
        setEnd(end);
        setTitle(title);
        setDescription(description);
    }

    public void setStart(long start) {
        if (start < 0L) start = UNDEFINED_TIMESTAMP;

        this.start = start;
    }

    public void setEnd(long end) {
        if (end < 0L) end = UNDEFINED_TIMESTAMP;

        this.end = end;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean hasStart() {
        return (start != UNDEFINED_TIMESTAMP);
    }

    public boolean hasEnd() {
        return (end != UNDEFINED_TIMESTAMP);
    }

    public long getStart() {
        return hasStart()
            ? start
            : 0L;
    }

    public long getEnd() {
        return hasEnd()
            ? end
            : INDETERMINATE_END_TIME;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCurrent() {
        long now = System.currentTimeMillis();
        return now >= start && now <= end;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if ((obj == null) || !(obj instanceof EPGEvent))
            return false;

        EPGEvent that = (EPGEvent) obj;

        if (this.start != that.start)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        // 1. calculate a timestamp in the recent past to use as epoch (ex: first day of previous month)
        // 2. rebase start timestamp relative to recent epoch
        // 3. reduce precision of relative timestamp from milliseconds to seconds (note: an int can only hold 24.8 days worth of milliseconds, but it can hold 68 years worth of seconds)
        // 4. cast relative timestamp from long to int

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long epoch = cal.getTimeInMillis();

        long relativeMs = this.start - epoch;
        int relativeSec = (int) (relativeMs / 1000L);

        return relativeSec;
    }
}
