package com.microsoft.projectoxford.emotionsample.musicPlayerExample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.microsoft.projectoxford.emotionsample.R;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.projectoxford.emotionsample.initialization.MusicObject;
import com.microsoft.projectoxford.emotionsample.initialization.SongListModel;
import com.microsoft.projectoxford.emotionsample.musicPlayer.PlayerService;

/**
 * Created by christophE on 2016-10-22.
 */

public class MusicActivity extends AppCompatActivity{
    private PlayerService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private List<MusicObject> songList = new ArrayList<>();
    private SongListModel mModel;

    @Override
    public void onCreate(Bundle savedInstances){
        super.onCreate(savedInstances);
        setContentView(R.layout.activity_music);
        mModel = new SongListModel(this);

        Button play = (Button)findViewById(R.id.play_song);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(musicSrv!=null)
                    musicSrv.playSong();
            }
        });
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.MusicBinder binder = (PlayerService.MusicBinder) service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(mModel.getCategoryList(null));
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public void onStart(){
        super.onStart();
        if(playIntent==null) {
            playIntent = new Intent(this, PlayerService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }
    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }

}
