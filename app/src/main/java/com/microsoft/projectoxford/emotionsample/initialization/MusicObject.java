package com.microsoft.projectoxford.emotionsample.initialization;

/**
 * Created by christophE on 2016-10-22.
 */

public class MusicObject {
    private String mTitle;
    private String mData;
    private String mArtist;
    private String mCategory;

    public MusicObject(String title,String data, String artist, String category){
        mTitle = title;
        mData = data;
        mArtist = artist;
        mCategory = category;
    }

    public String getTitle(){
        return mTitle;
    }
    public String getData(){
        return mData;
    }
    public String getArtist(){
        return mArtist;
    }
    public String getCategory(){
        return mCategory;
    }

}
