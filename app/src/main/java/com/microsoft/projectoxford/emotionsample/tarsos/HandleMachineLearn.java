package com.microsoft.projectoxford.emotionsample.tarsos;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioFormat;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.microsoft.projectoxford.emotionsample.initialization.MusicObject;
import com.microsoft.projectoxford.emotionsample.initialization.SongListContract;
import com.microsoft.projectoxford.emotionsample.initialization.SongListDbHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.beatroot.BeatRootOnsetEventHandler;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

import static android.content.ContentValues.TAG;
import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;

/**
 * Created by christophE on 2016-10-23.
 */

public class HandleMachineLearn implements Runnable , OnsetHandler {


    private Handler mHandler;
    private SongListDbHelper mDbHelper;
    private SQLiteDatabase mDb;
    final MusicObject obj;
    private float[] dataArray = new float[250];
    private List<Double> mList = new ArrayList();
    private AudioDispatcher mADP;
    public HandleMachineLearn(MusicObject obj2,SongListDbHelper mDbHelper, Handler handler){
        obj = obj2;
        this.mDbHelper = mDbHelper;
        mHandler = handler;
    }

    @Override
    public void run() {
        int fftSize = 1024;
        int samplingRate = 16000;
        File mp3 =new File(obj.getData());
        //Get the metadata
        /*
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(mp3.getAbsolutePath());
        String duration = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        float seconds = ((Float.parseFloat(duration)))/1000;
        */


        mADP = AudioDispatcherFactory.fromPipe(mp3.getAbsolutePath(),samplingRate,fftSize,0);

        //PitchDetectionHandler pitchDetectionHandler = this;
        //mADP.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,44100,1024,pitchDetectionHandler));
        //adp.addAudioProcessor(new AndroidAudioPlayer(adp.getFormat(),5000, AudioManager.STREAM_MUSIC));


        //Beat process
        ComplexOnsetDetector detector = new ComplexOnsetDetector(fftSize);
        BeatRootOnsetEventHandler handler = new BeatRootOnsetEventHandler();
        detector.setHandler(handler);
        mADP.addAudioProcessor(detector);
        mADP.run();
        handler.trackBeats(this);
        writeToDatabase();
    }

    /**
     *  Tempo tracker
     * @param time beats per second
     * @param salience
     */
    @Override
    public void handleOnset(double time, double salience){
        mList.add(time);
        /**
         Melancholy / Sadness / Loss / Sorrow / Pain: 50 to 85 BPM
         Thoughtful / Introspective: 90-105 BPM
         Happy / Party / Celebration: 110-125 BPM
         Excitement / Energy / Danger / Anger: 130 BPM and up
         */
    }

    /*
     * Pitch detector
     * @param pitchDetectionResult the pitch at the time
     * @param audioEvent audio event details
     */
    /*
    @Override
    public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
        float pitch = pitchDetectionResult.getPitch();
        double i = audioEvent.getTimeStamp();
        double t2 = audioEvent.getEndTimeStamp();

                if(pitch!=-1){
                    //dataArray[itterator] = pitch;
                    //mList.add(pitch);
                    itterator++;

                    //Stop when out of bounds
                    if(itterator>=dataArray.length){
                        //mADP.stop();
                    }
                }
    }
    */


    public void writeToDatabase(){


        String emotion;
        if(mList.size()==0) {
            emotion="NONE";
        }else {
            double[] differences = new double[mList.size() - 1];
            for (int i = 0; i < mList.size() - 1; i++) {
                differences[i] = mList.get(i + 1) - mList.get(i);
            }
            Arrays.sort(differences);
            double median;
            if (differences.length % 2 == 0)
                median = (differences[differences.length / 2] + differences[differences.length / 2 - 1]) / 2;
            else
                median = differences[differences.length / 2];

            double bpm = (60 / median);
            if (bpm <= 110) {
                emotion = "Sadness";
            } else if (110 < bpm && bpm <= 140) {
                emotion = "Happiness";
            } else {
                emotion = "Anger";
            }
        }
        /**
         *
         Melancholy / Sadness / Loss / Sorrow / Pain: 50 to 85 BPM
         Thoughtful / Introspective: 90-105 BPM
         Happy / Party / Celebration: 110-125 BPM
         Excitement / Energy / Danger / Anger: 130 BPM and up

         Neutral=  All

         Sadness/Contempt/Disgust
         Happiness / Surprised
         Anger Fear
         */


        mDb = mDbHelper.getWritableDatabase();
         ContentValues values = new ContentValues();
         values.put(SongListContract.FeedEntry.COLUMN_NAME_TITLE, obj.getTitle());
         values.put(SongListContract.FeedEntry.COLUMN_NAME_ARTIST,obj.getArtist());
         values.put(SongListContract.FeedEntry.COLUMN_NAME_PATH, obj.getData());
         values.put(SongListContract.FeedEntry.COLUMN_NAME_CATEGORY, emotion);
         values.put(SongListContract.FeedEntry.COLUMN_NAME_ANALYZED, SongListContract.YES );

         // Insert the new row, returning the primary key value of the new row
         int u =mDb.update(SongListContract.FeedEntry.TABLE_NAME,values,SongListContract.FeedEntry.COLUMN_NAME_PATH+"=\""+obj.getData()+"\"",null);
         mDb.close();
         mHandler.sendEmptyMessage(2);

    }
}
