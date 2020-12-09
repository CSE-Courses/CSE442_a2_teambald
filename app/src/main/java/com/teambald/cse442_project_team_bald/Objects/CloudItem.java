package com.teambald.cse442_project_team_bald.Objects;

import java.io.File;

//TODO: @Chaoping: Modify this template based on your needs.
public class CloudItem extends RecordingItem{
    boolean existLocal;

    public CloudItem(String date, String duration, boolean play,boolean exist) {
        this.date = date;
        this.duration = duration;
        this.play = play;
        this.existLocal = exist;
    }

    public boolean getExistLocal()
    {return existLocal;}
    public void setExistLocal(boolean el)
    {
        this.existLocal = el;
    }
}
