package com.teambald.cse442_project_team_bald.Objects;

//TODO: @Chaoping: Modify this template based on your needs.
public class RecordingItem {
    String date;
    String duration;
    boolean play;

    public RecordingItem(String date, String duration, boolean play) {
        this.date = date;
        this.duration = duration;
        this.play = play;
    }

    public String getDate() {
        return date;
    }

    public String getDuration() {
        return duration;
    }

    public boolean isPlay() {
        return play;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setPlay(boolean play) {
        this.play = play;
    }
}
