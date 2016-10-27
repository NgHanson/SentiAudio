package com.microsoft.projectoxford.emotionsample.initialization;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;

import com.microsoft.projectoxford.emotionsample.tarsos.HandleMachineLearn;
import com.microsoft.projectoxford.emotionsample.thread.ProcessManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE;

/**
 * Created by christophE on 2016-10-22.
 */

public class SongListModel {

    private Handler mHandler;
    private SQLiteDatabase mDb;
    private SongListDbHelper mDbHelper;
    private Context mContext;
    private ProcessManager mManager;
    private int mTasksStarted = 0;
    private int mTasksCompleted = 0;
    private notifyMainClassListener mListener;


    public interface notifyMainClassListener{
        void stopLoading();
        void setInfoMessage(String message);
    }


    public SongListModel(Context context,notifyMainClassListener listener){
        mContext = context;
        mListener = listener;
        mDbHelper = new SongListDbHelper(context);
        mManager = new ProcessManager();
        mHandler = new Handler(Looper.getMainLooper()) {
            /*
             * handleMessage() defines the operations to perform when
             * the Handler receives a new Message to process.
             */
            @Override
            public void handleMessage(Message inputMessage) {
                mTasksCompleted++;
                if(mTasksCompleted == mTasksStarted){
                    //Notify UI;
                    mListener.stopLoading();
                    mListener.setInfoMessage("Analyzing songs please wait.\nSongs analyzed: "+mTasksCompleted + "/" + mTasksStarted);
                }
            }
        };

    }

    public void initializeSongList(){
        ContentResolver cr = mContext.getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cur = cr.query(uri, null, selection, null, sortOrder);
        int count = 0;


        mDb = mDbHelper.getWritableDatabase();
        if(cur != null)
        {
            count = cur.getCount();

            if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String ismusic = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
                    if(ismusic.equals("1")){
                        String data = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
                        String title = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE));
                        String artist = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST));


                        MusicObject obj = new MusicObject(title,data,artist,null);


                        ContentValues values = new ContentValues();
                        values.put(SongListContract.FeedEntry.COLUMN_NAME_TITLE, obj.getTitle());
                        values.put(SongListContract.FeedEntry.COLUMN_NAME_ARTIST,obj.getArtist());
                        values.put(SongListContract.FeedEntry.COLUMN_NAME_PATH, obj.getData());
                        values.put(SongListContract.FeedEntry.COLUMN_NAME_CATEGORY, "NONE");
                        values.put(SongListContract.FeedEntry.COLUMN_NAME_ANALYZED, SongListContract.NO );

                        // Insert the new row, returning the primary key value of the new row
                        long newRowId = mDb.insertWithOnConflict(SongListContract.FeedEntry.TABLE_NAME,null,values,CONFLICT_IGNORE);


                    }
                }
            }
        }
        cur.close();

        //See which songs to analyze
        String[] projection = {
                SongListContract.FeedEntry._ID,
                SongListContract.FeedEntry.COLUMN_NAME_TITLE,
                SongListContract.FeedEntry.COLUMN_NAME_ARTIST,
                SongListContract.FeedEntry.COLUMN_NAME_PATH,
                SongListContract.FeedEntry.COLUMN_NAME_CATEGORY,
                SongListContract.FeedEntry.COLUMN_NAME_ANALYZED
        };

        // Filter results WHERE "title" = 'My Title'
        String selectionDb = SongListContract.FeedEntry.COLUMN_NAME_ANALYZED + " = ?";
        String[] selectorDb = new String[1];
        selectorDb[0] = SongListContract.NO;
        // How you want the results sorted in the resulting Cursor
        String sortOrderDb =
                SongListContract.FeedEntry.COLUMN_NAME_TITLE + " DESC";

        Cursor c = mDb.query(
                SongListContract.FeedEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selectionDb,                                // The columns for the WHERE clause
                selectorDb,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrderDb                                 // The sort order
        );
        count = c.getCount();
        try {
            while (c.moveToNext()) {
                String title = c.getString(c.getColumnIndexOrThrow(SongListContract.FeedEntry.COLUMN_NAME_TITLE));
                String artist = c.getString(c.getColumnIndexOrThrow(SongListContract.FeedEntry.COLUMN_NAME_ARTIST));
                String data = c.getString(c.getColumnIndexOrThrow(SongListContract.FeedEntry.COLUMN_NAME_PATH));
                String category = c.getString(c.getColumnIndexOrThrow(SongListContract.FeedEntry.COLUMN_NAME_CATEGORY));
                //list.add(new MusicObject(title,path,artist,category));
                MusicObject obj = new MusicObject(title,data,artist,null);


                mTasksStarted++;
                mManager.executeTask(new HandleMachineLearn(obj,mDbHelper,mHandler));
            }
        } finally {
            mDb.close();
            c.close();
            if(mTasksStarted==0){
                mListener.stopLoading();
            }
        }

    }
    public List<MusicObject> getCategoryList(String categorySelection){
        List<MusicObject> list = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                SongListContract.FeedEntry._ID,
                SongListContract.FeedEntry.COLUMN_NAME_TITLE,
                SongListContract.FeedEntry.COLUMN_NAME_ARTIST,
                SongListContract.FeedEntry.COLUMN_NAME_PATH,
                SongListContract.FeedEntry.COLUMN_NAME_CATEGORY,
                SongListContract.FeedEntry.COLUMN_NAME_ANALYZED
        };
        String selection = "";
        String[] selectionArgs = null;
        if(categorySelection!=null) {
            selectionArgs = new String[1];
            selectionArgs[0] = categorySelection;
            // Filter results WHERE "title" = 'My Title'
            selection = SongListContract.FeedEntry.COLUMN_NAME_CATEGORY + " = ?";
            //String[] selectionArgs = { "My Title" };
        }
        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                SongListContract.FeedEntry.COLUMN_NAME_TITLE + " DESC";

        Cursor c = db.query(
                SongListContract.FeedEntry.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
        try {
            while (c.moveToNext()) {
                String title = c.getString(c.getColumnIndexOrThrow(SongListContract.FeedEntry.COLUMN_NAME_TITLE));
                String artist = c.getString(c.getColumnIndexOrThrow(SongListContract.FeedEntry.COLUMN_NAME_ARTIST));
                String path = c.getString(c.getColumnIndexOrThrow(SongListContract.FeedEntry.COLUMN_NAME_PATH));
                String category = c.getString(c.getColumnIndexOrThrow(SongListContract.FeedEntry.COLUMN_NAME_CATEGORY));
                list.add(new MusicObject(title,path,artist,category));
            }
        } finally {
            c.close();
        }
        db.close();

        Collections.shuffle(list);
        return list;
    }
}
