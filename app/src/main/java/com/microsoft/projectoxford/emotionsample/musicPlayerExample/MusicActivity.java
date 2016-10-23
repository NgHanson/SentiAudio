package com.microsoft.projectoxford.emotionsample.musicPlayerExample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.microsoft.projectoxford.emotionsample.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.projectoxford.emotionsample.initialization.MusicObject;
import com.microsoft.projectoxford.emotionsample.initialization.SongListModel;
import com.microsoft.projectoxford.emotionsample.musicPlayer.PlayerService;
import com.microsoft.projectoxford.emotionsample.tarsos.HandleMachineLearn;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AndroidAudioPlayer;
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
/**
 * Created by christophE on 2016-10-22.
 */

public class MusicActivity extends AppCompatActivity implements SongListModel.notifyMainClassListener {
    private PlayerService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private List<MusicObject> songList = new ArrayList<>();
    private SongListModel mModel;

    @Override
    public void onCreate(Bundle savedInstances){
        super.onCreate(savedInstances);
        setContentView(R.layout.activity_music);
        mModel = new SongListModel(this,this);

        Button play = (Button)findViewById(R.id.play_song);
        /*play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(musicSrv!=null)
                    musicSrv.playSong();
            }
        });*/

        Build.SUPPORTED_ABIS[0].equals("x86");
        new com.microsoft.projectoxford.emotionsample.tarsos.AndroidFFMPEGLocator(this);
        songList.clear();
        songList.addAll(mModel.getCategoryList(null));
        final MusicObject obj = songList.get(0);
        //new Thread(new HandleMachineLearn(obj)).start();
    }

    @Override
    public void stopLoading(){}
    //connect to the service
    /*
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
    };*/
}
