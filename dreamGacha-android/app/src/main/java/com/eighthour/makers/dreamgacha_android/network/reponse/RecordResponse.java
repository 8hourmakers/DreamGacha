package com.eighthour.makers.dreamgacha_android.network.reponse;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ohdoking on 2017. 7. 30..
 */


/**
 *
 "id": integer <dream id>,
 "dream_audio_url": string <음성 URL>,
 "title": string <꿈 민폐>,
 "content": string <꿈 텍스트 컨텐츠>,
 "created_timestamp": timestamp <꿈 생성 시간 : YYYY-mm-dd HH:MM:SS>,
 */
public class RecordResponse {

    @SerializedName("id")
    public String id;

    @SerializedName("dream_audio_url")
    public String dreamAudioUrl;

    @SerializedName("content")
    public String content;

    @SerializedName("title")
    public String title;

    @SerializedName("created_timestamp")
    public String createdTimestamp;

}
