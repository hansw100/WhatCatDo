package com.team_project2.hans.whatcatdo.database;

import java.io.Serializable;

public class Emotion implements Serializable {
    private long timestamp;
    private String title;
    private float percent;

    public Emotion(long timestamp, String title, float percent){
        this.timestamp = timestamp;
        this.title = title;
        this.percent = percent;
    }

    public float getPercent() {
        return percent;
    }

    public String getTitle() {
        return title;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
