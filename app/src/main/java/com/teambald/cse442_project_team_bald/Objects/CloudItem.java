package com.teambald.cse442_project_team_bald.Objects;

import java.io.File;

//TODO: @Chaoping: Modify this template based on your needs.
public class CloudItem extends RecordingItem{
    String date;
    String duration;
    File audio_file;
    int StartTime;
    boolean play;
    boolean locker;
    boolean existLocal;

    public CloudItem(String date, String duration, boolean play,boolean exist) {
        this.date = date;
        this.duration = duration;
        this.play = play;
        this.locker=true; // set to true
        this.existLocal = exist;
    }

    public boolean getExistLocal()
    {return existLocal;}
    public void setExistLocal(boolean el)
    {
        this.existLocal = el;
    }
}
