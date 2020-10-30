package com.teambald.cse442_project_team_bald.Objects;

import java.io.File;

//TODO: @Chaoping: Modify this template based on your needs.
public class LocalItem extends RecordingItem{
    String date;
    String duration;
    File audio_file;
    int StartTime;
    boolean play;
    boolean locker;
    boolean existCloud;

    public LocalItem()
    {}
    public LocalItem(String date, String duration, boolean play) {
        this.date = date;
        this.duration = duration;
        this.play = play;
        this.locker=true; // set to true
    }
    public LocalItem(String date, String duration,String path, boolean play,File audio_file) {
        this.date = date;
        this.duration = duration;
        this.play = play;
        this.audio_file = audio_file;
        this.locker=true; // set to true
        StartTime = 0;
    }

    public boolean getExistCloud()
    {return existCloud;}
    public void setExistCloud(boolean cl)
    {
        this.existCloud = cl;
    }
}
