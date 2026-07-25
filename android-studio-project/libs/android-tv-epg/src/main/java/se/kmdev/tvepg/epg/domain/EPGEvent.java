package se.kmdev.tvepg.epg.domain;

/**
 * Created by Kristoffer.
 */
public class EPGEvent {

    private long start;
    private long end;
    private String title;
    private String description;

    public EPGEvent(long start, long end, String title, String description) {
        this.start = start;
        this.end = end;
        this.title = title;
        this.description = description;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
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

        if (!(obj instanceof EPGEvent))
            return false;

        EPGEvent that = (EPGEvent) obj;

        if (!this.title.equals(that.getTitle()))
            return false;

        if (this.start != that.start)
            return false;

        return true;
    }
}
