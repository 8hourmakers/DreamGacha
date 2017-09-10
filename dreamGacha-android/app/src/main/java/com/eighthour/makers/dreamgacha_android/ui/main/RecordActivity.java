package com.eighthour.makers.dreamgacha_android.ui.main;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.eighthour.makers.dreamgacha_android.R;
import com.eighthour.makers.dreamgacha_android.network.ServerQuery;
import com.eighthour.makers.dreamgacha_android.network.reponse.RecordResponse;
import com.eighthour.makers.dreamgacha_android.util.RecordUtil;
import com.triggertrap.seekarc.SeekArc;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class RecordActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int RequestPermissionCode = 1;

    Button buttonStart;
    Button buttonStop;
    Button buttonPause;
    Button buttonPlayLastRecordAudio;
    Button buttonStopPlayingRecording;

    ImageView playBtn;
    ImageView recordBtn;
    ImageView saveAudioBtn;


    SeekArc seekArc;

    ServerQuery serverQuery;

    File recordFile;
    RecordUtil recordUtil;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        recordUtil = new RecordUtil(getBaseContext());
        initView();


        clickEventInit();

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void initView() {


        buttonStart = (Button) findViewById(R.id.button);
        buttonStop = (Button) findViewById(R.id.button2);
        buttonPause = (Button) findViewById(R.id.button5);
        buttonPlayLastRecordAudio = (Button) findViewById(R.id.button3);
        buttonStopPlayingRecording = (Button) findViewById(R.id.button4);

        playBtn = (ImageView) findViewById(R.id.playBtn);
        recordBtn = (ImageView) findViewById(R.id.recordBtn);
        saveAudioBtn = (ImageView) findViewById(R.id.saveAudioBtn);

        seekArc = (SeekArc) findViewById(R.id.seekArc);

        buttonStop.setEnabled(false);
        buttonPlayLastRecordAudio.setEnabled(false);
        buttonStopPlayingRecording.setEnabled(false);

        playBtn.setEnabled(false);
        saveAudioBtn.setEnabled(false);

        seekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {

            }
        });

    }


    public void clickEventInit() {

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playBtn.isEnabled()) {
                    v.setSelected(!v.isSelected());
                    if (RecordUtil.STATE_AUDIO_STOP == recordUtil.audioState) {
                        recordUtil.audioPlay(v);
                        setButtonState(true, false, false);

//                        seekArc.(recordUtil.getResultFileDuration());
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                if (recordUtil.recordState == RecordUtil.STATE_RECORDING)  // call ui only when  the progress is not stopped
                                {
                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            try {
                                                seekArc.setProgress(recordUtil.mCurrentMin++);
                                            } catch (Exception e) {

                                            }
                                        }
                                    });
                                }
                            }
                        }, 1, 10);



                    } else {
                        recordUtil.audioStop();
                        setButtonState(true, true, false);
                    }
                }
            }
        });

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recordBtn.isEnabled()) {
                    v.setSelected(!v.isSelected());
                    if (RecordUtil.STATE_RECORDING == recordUtil.recordState) {
                        recordUtil.pause();
                        setButtonState(true, true, true);
                    } else {
                        recordUtil.start();
                        setButtonState(false, true, false);

                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                if (recordUtil.recordState == RecordUtil.STATE_RECORDING)  // call ui only when  the progress is not stopped
                                {
                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            try {
                                                seekArc.setProgress(recordUtil.mCurrentMin++);
                                            } catch (Exception e) {

                                            }
                                        }
                                    });
                                }
                            }
                        }, 1, 10);

                    }
                }
            }
        });

        saveAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (saveAudioBtn.isEnabled()) {
                    v.setSelected(!v.isSelected());
                    recordUtil.stop();
                    recordFile = recordUtil.getResultFile();
                    setButtonState(true, true, false);

                    IConvertCallback callback = new IConvertCallback() {
                        @Override
                        public void onSuccess(File convertedFile) {
                            // So fast? Love it!

                            byte[] bytes = readFile(convertedFile);
                            Log.i("ohdoking", bytes + "");

                            recordFile = convertedFile;
                            Toast.makeText(RecordActivity.this, "Recording Completed",
                                    Toast.LENGTH_LONG).show();

                            serverQuery.saveRecordFile(recordFile, new Callback<RecordResponse>() {
                                @Override
                                public void onResponse(Call<RecordResponse> call, Response<RecordResponse> response) {
                                    // Do Something
                                    Log.d("ohdoking",response.body().dreamAudioUrl);

                                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                                    Date date = new Date();
                                    String listName = dateFormat.format(date) + "날의 꿈";

                                    serverQuery.saveDream(listName, response.body().content, response.body().dreamAudioUrl, new Callback<RecordResponse>() {
                                        @Override
                                        public void onResponse(Call<RecordResponse> call, Response<RecordResponse> response) {
                                            Log.d("ohdoking",response.message());

                                            if(response.message().equals("OK")){
//                                                Intent i = new Intent(RecordActivity.this, );
//                                                startActivity(i);
                                            }
                                            else{
                                                Toast.makeText(getBaseContext(), response.message(),Toast.LENGTH_SHORT).show();
                                            }

                                        }

                                        @Override
                                        public void onFailure(Call<RecordResponse> call, Throwable t) {

                                        }
                                    });

                                }

                                @Override
                                public void onFailure(Call<RecordResponse> call, Throwable t) {
                                    t.printStackTrace();
                                }
                            });

                        }
                        @Override
                        public void onFailure(Exception error) {
                            // Oops! Something went wrong
                        }
                    };

                    recordUtil.convertFileToWav(callback);




//                serverQuery.saveRecordFile(recordFile, new Callback<RecordResponse>() {
//                    @Override
//                    public void onResponse(Call<RecordResponse> call, Response<RecordResponse> response) {
//                        // Do Something
//                        Log.d("ohdoking",response.body().dreamAudioUrl);
//
//
//
//
//                    }
//
//                    @Override
//                    public void onFailure(Call<RecordResponse> call, Throwable t) {
//                        t.printStackTrace();
//                    }
//                });
                }
            }
        });

    }

    public byte[] readFile(File file)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {

            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));

            int read;
            byte[] buff = new byte[1024];
            while ((read = in.read(buff)) > 0)
            {
                out.write(buff, 0, read);
            }

            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }


    /**
     * Button 상태 변경
     *
     * @param saveAudioState
     * @param playState
     * @param recordState
     */
    public void setButtonState(boolean playState, boolean recordState, boolean saveAudioState) {
        saveAudioBtn.setEnabled(saveAudioState);
        playBtn.setEnabled(playState);
        recordBtn.setEnabled(recordState);
    }

    /**
     * permission
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(RecordActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(RecordActivity.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(RecordActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onClick(View v) {
        v.setSelected(!v.isSelected());
        Log.i("test", v.isSelected() + "");
    }
}
