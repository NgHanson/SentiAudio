package com.microsoft.projectoxford.emotionsample.musicPlayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.projectoxford.emotionsample.RecognizeActivity;
import com.microsoft.projectoxford.emotionsample.initialization.MusicObject;

/**
 * Created by christophE on 2016-10-22.
 */

public class PlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private final static int TIME_DELAY_FOR_UPDATE = 1000;//Time delay for progress bar in ms
    //media player
    private MediaPlayer player;
    //song list
    private List<MusicObject> songs = new ArrayList<>();
    //current position
    private int songPosn;
    //Binder
    private final IBinder musicBind = new MusicBinder();
    //Listener
    private MusicPlayerListener mListener;

    //To keep track of progress
    private boolean mGetTimeResults = true;
    private boolean timerStarted = false;
    private Handler timerHandler;
    private CheckProgressRunnable timerRunnable;

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
        timerHandler = new Handler();
        timerRunnable = new CheckProgressRunnable();
        songPosn=0;
        player = new MediaPlayer();
        initMusicPlayer();
    }
    /**
     * Destruction commands
     */
    public void onDestroy(){
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
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
        public void setListener(MusicPlayerListener listener){
            mListener = listener;
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
            mListener.sendPlayerInfo(playSong.getTitle(),playSong.getArtist());
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
        if(player.getCurrentPosition()>1500){
            playSong();
            return;
        }
        songPosn--;
        if(songPosn<0){
            songPosn=songs.size();
        }
        playSong();
    }
    public void pauseSong(){
        player.pause();
    }
    public void resumeSong(){
        player.start();

    }
    public boolean pause_startSong(){
        if(player.isPlaying()){
            pauseSong(); //Makes button look like a play button
            return true;
        }else{
            resumeSong(); //Makes button look like a stop button
            return false;
        }
    }

    public void setSong(int index){
        songPosn = index;
    }

    public void setSongTime(int time){
        player.seekTo(time);
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
        mListener.setMax(player.getDuration());
        if(!timerStarted){
            timerHandler.postDelayed(timerRunnable,0);
            timerStarted = true;
        }
    }

    /**
     * Check timer runnable
     */
    public void setGetTimeResults(boolean getTime){
        mGetTimeResults = getTime;
    }
    class CheckProgressRunnable implements Runnable{
        @Override
        public void run(){
            if(mGetTimeResults) {
                mListener.sendProgress(player.getCurrentPosition());
            }
            timerHandler.postDelayed(this,TIME_DELAY_FOR_UPDATE);
        }

    }

    /**
     * Interface methods for activity
     */
    public interface MusicPlayerListener {
        void sendProgress(int progress);
        void sendPlayerInfo(String title, String artist);
        void setMax(int maxTime);
    }
}
