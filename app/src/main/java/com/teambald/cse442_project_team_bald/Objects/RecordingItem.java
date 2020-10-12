package com.teambald.cse442_project_team_bald.Objects;

import java.io.File;

//TODO: @Chaoping: Modify this template based on your needs.
public class RecordingItem {
    String date;
    String duration;
    File audio_file;
    boolean play;

    public RecordingItem(String date, String duration, boolean play) {
        this.date = date;
        this.duration = duration;
        this.play = play;
    }
    public RecordingItem(String date, String duration,String path, boolean play,File audio_file) {
        this.date = date;
        this.duration = duration;
        this.play = play;
        this.audio_file = audio_file;
    }

    public String getDate() {
        return date;
    }

    public File getAudio_file(){return this.audio_file;}

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
