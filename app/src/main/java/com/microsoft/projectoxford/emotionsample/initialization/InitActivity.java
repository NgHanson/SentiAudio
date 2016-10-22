package com.microsoft.projectoxford.emotionsample.initialization;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.microsoft.projectoxford.emotionsample.R;

import com.microsoft.projectoxford.emotionsample.musicPlayerExample.MusicActivity;

/**
 * Created by christophE on 2016-10-22.
 */

public class InitActivity extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstances){
        super.onCreate(savedInstances);
        setContentView(R.layout.activity_init);
        SongListModel mModel = new SongListModel(this);
        mModel.initializeSongList();

        Intent myIntent = new Intent(this, MusicActivity.class);
        startActivity(myIntent);
    }
}
