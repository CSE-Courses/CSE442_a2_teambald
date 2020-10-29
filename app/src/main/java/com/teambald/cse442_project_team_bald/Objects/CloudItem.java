package com.teambald.cse442_project_team_bald.Objects;

import java.io.File;

//TODO: @Chaoping: Modify this template based on your needs.
public class CloudItem {
    String date;
    String duration;
    File audio_file;
    int StartTime;
    boolean play;
    boolean locker;

    public CloudItem(String date, String duration, boolean play) {
        this.date = date;
        this.duration = duration;
        this.play = play;
        this.locker=true; // set to true
    }
    public boolean isLocked(){
        return this.locker;
    }
    public void Lock(){
        locker=true;
    }
    public void unLock(){
        locker=false;
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

    public void setStartTime(int length){StartTime= length;}

    public int getStartTimeTime(){return StartTime;}

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
