package com.eighthour.makers.dreamgacha_android.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * 녹음을 하는 실질적 객체
 * Created by BH on 2015-02-13.
 */
public class RecordUtil {

    public final static int STATE_PREV = 0;     //녹음 시작 전
    public final static int STATE_RECORDING = 1;    //녹음 중
    public final static int STATE_PAUSE = 2;        // 일시 정지 중

    //결과 파일
    public File resultFile;
    //결과 파일 경로
    public String resultFilePath;
    //녹음 파일 명
    private String rname;
    //일시 중지하는 동안 저장된 파일 수
    private int count;
    //현재 오디오 상태
    public int state;

    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;

    String audioSaveDirInDevice = null;
    String audioSavePathInDevice = null;
    String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
    Random random;

    Context context;

    private ArrayList<String> outputFileList;   // 임시 저장 파일 리스트

    public RecordUtil(Context context) {
        this.context = context;
        random = new Random();
        outputFileList = new ArrayList<String>();
    }

    /**
     * 녹음 초기화
     */
    public void mediaRecorderReady() {

        random = new Random();
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setAudioEncodingBitRate(320000);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setOutputFile(audioSavePathInDevice);

//        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); //저장 방식 MPEG4
//        myAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

    }


    /**
     * 녹음 시작
     */
    public void start() {

        count += 1;

        audioSaveDirInDevice =
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp/";//임시 파일 저장 경로
        audioSavePathInDevice = audioSaveDirInDevice + count + ".mp4"; // 파일 명 (no) .mp4

        //디렉토리 존재 유무 체크
        File dir = new File(audioSaveDirInDevice);
        if(!dir.exists())
        {
            dir.mkdirs();
        }

        outputFileList.add(audioSavePathInDevice);    //임시파일 리스트에 파일 경로 추가

        mediaRecorderReady();

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();        //녹음 시작

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        state = STATE_RECORDING; //녹음 중 상태로 바꿈

    }

    /**
     * 녹음 정지
     */
    public void stop() {

        if (state == STATE_PREV) {     //
            //녹음 시작안한 상태에서 정지 버튼 누르기
            return;
        } else if (state == STATE_PAUSE) {
            //일시 정지 상태일 때,
        } else {
            // 재생 중 정지 버튼을 눌렀을 때 = 정상 작동
            //카운트 초기화
            //레코더 중지

            try {
                mediaRecorder.stop();
                // FileObserver here
            } catch (RuntimeException e) {
                e.printStackTrace();
                //녹음 실패
                Toast.makeText(context, "녹음 실패!", Toast.LENGTH_SHORT).show();
            }
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }

        //여기 서부턴 일시정지중 -> 정지, 재생중 -> 정지 랑 동일
        count = 0;
        //stop.setEnabled(false);
        //play.setEnabled(true);
        try {
            append(outputFileList);     //현재 임시 파일 리스트에 있는 파일들을 하나로 합침( 최종 결과파일)
        } catch (IOException e) {
            Toast.makeText(context, "Append Error!!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        saveResultFile();

    }

    /**
     * 녹음 일시 정지
     */
    public void pause() {

        mediaRecorder.stop();     //현재 녹음 중인 파일 종료 (임시파일)
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;
        state = STATE_PAUSE;

    }

    /**
     * audio 재생
     */
    public void audioPlay(){
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(getResultFilePath());
            mediaPlayer.prepare();
        }catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.start();
    }

    /**
     * audio 정지
     */
    public void audioStop(){
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaRecorderReady();
        }

    }

    public File getResultFile(){
        return resultFile;
    }

    public String getResultFilePath(){
        return resultFilePath;
    }
    /**
     * 랜덤으로 파일이름을 생성해줌
     */
    private String CreateRandomAudioFileName(int string) {
        StringBuilder stringBuilder = new StringBuilder(string);
        int i = 0;
        while (i < string) {
            stringBuilder.append(RandomAudioFileName.
                    charAt(random.nextInt(RandomAudioFileName.length())));
            i++;
        }
        return stringBuilder.toString();
    }

    /**
     * mp4를 하나로 만들어서 저장하는 메소드
     * 파일 패스 리스트를 받아서 합쳐서 outputfile 에 패스를 담음
     *
     * @param list 하나로 합칠 파일 리스트
     * @throws IOException
     */
    public void append(List<String> list) throws IOException {

        Movie[] inMovies;
        inMovies = new Movie[list.size()];
        for (int i = 0; i < list.size(); i++) {
            inMovies[i] = MovieCreator.build(list.get(i));
        }

        List<Track> videoTracks = new LinkedList<Track>();
        List<Track> audioTracks = new LinkedList<Track>();

        for (Movie m : inMovies) {
            for (Track t : m.getTracks()) {
                if (t.getHandler().equals("soun")) {
                    audioTracks.add(t);
                }
                if (t.getHandler().equals("vide")) {
                    videoTracks.add(t);
                }
            }
        }

        Movie result = new Movie();

        if (audioTracks.size() > 0) {
            result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
        }
        if (videoTracks.size() > 0) {
            result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
        }

        Container out = new DefaultMp4Builder().build(result);

        RandomAccessFile ram = new RandomAccessFile(String.format(Environment.getExternalStorageDirectory() + "/output.mp4"), "rw");
        //최종적으로 output.mp4 라는 파일로 다 합친 파일을 저장하게 됨
        FileChannel fc = ram.getChannel();
        out.writeContainer(fc);
        ram.close();
        fc.close();
    }

    /**
     * 최종 결과 오디오 파일을 저장
     */
    private void saveResultFile(){
        double fileSize = 0;
        String name = rname;

        Date date = new Date();
        Log.d("DayTest", date.getYear() + " " + date.getMonth() + " " + date.getDate());


        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd");


        String strDate = sim.format(date).toString();
        String rName = name + "_" + strDate + "-" + date.getHours() + "-" + date.getMinutes();

        //file1 -> file2 로 복사
        String outPath = Environment.getExternalStorageDirectory() + "/output.mp4";
        File file = new File(outPath);
        resultFilePath = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/" + rName + ".mp4";
        Log.d("PATH_TEST", context.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath());


        MediaPlayer mp = new MediaPlayer();
        try {
            //재생 시간 구하기
            FileInputStream fs;
            FileDescriptor fd;
            fs = new FileInputStream(file);
            fd = fs.getFD();
            mp.setDataSource(fd);
            mp.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int duration = mp.getDuration();
        duration /= 1000;
        mp.release();

        File file2 = new File(resultFilePath);
        fileSize = file.length();   //파일 크기 구하기(Byte)
        double fileMb = fileSize / (1024 * 1024);
        fileMb = Double.parseDouble(String.format("%.3f", fileMb));


        String info = strDate + " " + duration + " " + fileMb + "MB";
        Log.d("duration", info);
        try {
            FileInputStream fis = new FileInputStream(file);
            FileOutputStream newfos = new FileOutputStream(file2);
            int readcount = 0;
            byte[] buffer = new byte[1024];
            while ((readcount = fis.read(buffer, 0, 1024)) != -1) {
                newfos.write(buffer, 0, readcount);
            }
            newfos.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //file1 -> file2 로 복사(END)
        file.delete();
        resultFile = file2;

    }

}
