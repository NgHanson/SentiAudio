package com.microsoft.projectoxford.emotionsample.musicPlayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.projectoxford.emotionsample.initialization.MusicObject;

/**
 * Created by christophE on 2016-10-22.
 */

public class PlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    //media player
    private MediaPlayer player;
    //song list
    private List<MusicObject> songs = new ArrayList<>();
    //current position
    private int songPosn;
    //Binder
    private final IBinder musicBind = new MusicBinder();
    /**
     * Called when the service has been started from an activity
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        return START_STICKY; //Only stop explicitly
    }
    /**
     * One time setup commands
     */
    public void onCreate(){
        super.onCreate();
        songPosn=0;
        player = new MediaPlayer();
        initMusicPlayer();
    }
    /**
     * Destruction commands
     */
    public void onDestroy(){
        super.onDestroy();

    }

    /**
     * Binding commands
     */
    @Override
    public IBinder onBind(Intent intent){
        return musicBind;
    }
    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    /**
     * Application methods
     */
    public void initMusicPlayer(){
        //Set the player properties
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(List<MusicObject> theSongs){
        songs.clear();
        songs.addAll(theSongs);
    }

    public class MusicBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    public void playSong(){
        player.reset();
        //get song
        MusicObject playSong = songs.get(songPosn);
        /*get id
        long currSong = playSong.getD();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        */
        try {
            player.setDataSource(playSong.getData());
            player.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void skipSong(){
        songPosn++;
        if(songPosn>songs.size()){
            songPosn=0;
        }
        playSong();
    }
    public void prevSong(){
        songPosn--;
        if(songPosn<0){
            songPosn=songs.size();
        }
    }

    public void setSong(int index){
        songPosn = index;
    }


    /**
     * Interface methods
     */
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        skipSong();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        player.start();
    }
}
