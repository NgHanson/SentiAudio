package com.microsoft.projectoxford.emotionsample.tarsos;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Handler;

import com.microsoft.projectoxford.emotionsample.initialization.MusicObject;
import com.microsoft.projectoxford.emotionsample.initialization.SongListContract;
import com.microsoft.projectoxford.emotionsample.initialization.SongListDbHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;

/**
 * Created by christophE on 2016-10-23.
 */

public class HandleMachineLearn implements Runnable {


    private SongListDbHelper mDbHelper;
    private Handler mHandler;
    final MusicObject obj;
    private float[] dataArray = new float[250];
    private int itterator = 0;
    public HandleMachineLearn(MusicObject obj2,SongListDbHelper mDbHelper, Handler handler){
        obj = obj2;
        this.mDbHelper = mDbHelper;
        mHandler = handler;
    }

    @Override
    public void run() {
        //File mp3 = new File(externalStorage.getAbsolutePath() , "/audio.mp3");
        File mp3 =new File(obj.getData());
        final AudioDispatcher adp;

        adp = AudioDispatcherFactory.fromPipe(mp3.getAbsolutePath(),44100,1024,0);
        PitchDetectionHandler pitchDetectionHandler = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                float pitch = pitchDetectionResult.getPitch();
                double i = audioEvent.getTimeStamp();
                double t2 = audioEvent.getEndTimeStamp();
                if(pitch!=-1){
                    dataArray[itterator] = pitch;
                    itterator++;
                    if(itterator>=dataArray.length){
                        adp.stop();
                        /**
                         * Do network call here
                         */
                        try
                        {
                            ;
                            FileWriter writer = new FileWriter(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/"+obj.getTitle()+".csv",true);
                            for(float pitchVal : dataArray){
                                writer.write(Float.toString(pitchVal)+",");
                            }
                            writer.close();
                        }
                        catch (IOException e)
                        {
                            //error
                        }


                        /**
                         * Write to database
                         */

                        SQLiteDatabase db = mDbHelper.getWritableDatabase();
                        // Create a new map of values, where column names are the keys
                        ContentValues values = new ContentValues();
                        values.put(SongListContract.FeedEntry.COLUMN_NAME_TITLE, obj.getTitle());
                        values.put(SongListContract.FeedEntry.COLUMN_NAME_ARTIST,obj.getArtist());
                        values.put(SongListContract.FeedEntry.COLUMN_NAME_PATH, obj.getData());
                        values.put(SongListContract.FeedEntry.COLUMN_NAME_CATEGORY, "NONE");

                        // Insert the new row, returning the primary key value of the new row
                        long newRowId = db.insertWithOnConflict(SongListContract.FeedEntry.TABLE_NAME,null,values,CONFLICT_IGNORE);
                        db.close();
                        mHandler.sendEmptyMessage(2);


                    }
                }

            }
        };
        adp.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,44100,1024,pitchDetectionHandler));
        //adp.addAudioProcessor(new AndroidAudioPlayer(adp.getFormat(),5000, AudioManager.STREAM_MUSIC));

        adp.run();
    }
}
