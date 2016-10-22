package initialization;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by christophE on 2016-10-22.
 */

public class SongListModel {

    private SongListDbHelper mDbHelper;
    private Context mContext;

    public SongListModel(Context context){
        mContext = context;
        mDbHelper = new SongListDbHelper(context);

    }

    public void initializeSongList(){
        ContentResolver cr = mContext.getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cur = cr.query(uri, null, selection, null, sortOrder);
        int count = 0;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

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


                        // Create a new map of values, where column names are the keys
                        ContentValues values = new ContentValues();
                        values.put(SongListContract.FeedEntry.COLUMN_NAME_TITLE, title);
                        values.put(SongListContract.FeedEntry.COLUMN_NAME_ARTIST,artist);
                        values.put(SongListContract.FeedEntry.COLUMN_NAME_PATH, data);
                        values.put(SongListContract.FeedEntry.COLUMN_NAME_CATEGORY, "NONE");

                        // Insert the new row, returning the primary key value of the new row
                        long newRowId = db.insert(SongListContract.FeedEntry.TABLE_NAME, null, values);
                    }
                }
            }
        }
        cur.close();
    }
}
