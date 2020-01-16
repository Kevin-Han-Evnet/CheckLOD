package com.netmania.checklod.general;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

/**
 * Created by hansangcheol on 2017. 7. 12..
 */

public class AlarmPlayer {

    public static AlarmPlayer k;
    private Context mContext;

    private MediaPlayer emergencyPlayer;
    private MediaPlayer cautionPlayer;
    private MediaPlayer blePlayer;
    private MediaPlayer scanBeepPlayer;


    public AlarmPlayer(Context mContext) {
        this.mContext = mContext;
        setMediaPlayer();
    }


    /** 싱글톤 고고싱
     *
     * @param mContext
     * @return
     */
    public static AlarmPlayer getInstance (Context mContext) {
        if (k == null) k = new AlarmPlayer(mContext);
        return k;
    }



    /** 미디어 플레이어
     *
     */
    private void setMediaPlayer () {
        emergencyPlayer = new MediaPlayer();         // 객체생성
        cautionPlayer = new MediaPlayer();
        blePlayer = new MediaPlayer();
        scanBeepPlayer = new MediaPlayer();

        try {

            Uri alert = Uri.parse ("android.resource://" + mContext.getApplicationContext().getPackageName() + "/" + R.raw.emergency_001);
            //Uri alert = Uri.parse ("android.resource://" + mContext.getApplicationContext().getPackageName() + "/" + R.raw.test_emergency);
            emergencyPlayer.setDataSource(mContext.getApplicationContext(), alert);
            emergencyPlayer.setAudioStreamType(AudioManager.STREAM_RING);
            emergencyPlayer.setLooping(true);

            emergencyPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    //mp.start();
                }
            });
            emergencyPlayer.prepareAsync();

            Uri caution = Uri.parse ("android.resource://" + mContext.getApplicationContext().getPackageName() + "/" + R.raw.caution_001);
            //Uri caution = Uri.parse ("android.resource://" + mContext.getApplicationContext().getPackageName() + "/" + R.raw.test_cautino);
            cautionPlayer.setDataSource(mContext.getApplicationContext(), caution);
            cautionPlayer.setAudioStreamType(AudioManager.STREAM_RING);
            cautionPlayer.setLooping(true);

            cautionPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    //mp.start();
                }
            });
            cautionPlayer.prepareAsync();

            Uri ble = Uri.parse ("android.resource://" + mContext.getApplicationContext().getPackageName() + "/" + R.raw.emergency_002);
            //Uri ble = Uri.parse ("android.resource://" + mContext.getApplicationContext().getPackageName() + "/" + R.raw.test_ble);
            blePlayer.setDataSource(mContext.getApplicationContext(), ble);
            blePlayer.setAudioStreamType(AudioManager.STREAM_RING);
            blePlayer.setLooping(false);

            blePlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    //mp.start();
                }
            });
            blePlayer.prepareAsync();


            Uri scacned_beep = Uri.parse ("android.resource://" + mContext.getApplicationContext().getPackageName() + "/" + R.raw.scaned_beep);
            scanBeepPlayer.setDataSource(mContext.getApplicationContext(), scacned_beep);
            scanBeepPlayer.setAudioStreamType(AudioManager.STREAM_RING);
            scanBeepPlayer.setLooping(false);

            scanBeepPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    //mp.start();
                }
            });
            scanBeepPlayer.prepareAsync();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** 비상 비상
     *
     */
    public void playEmergency () {
        if (!emergencyPlayer.isPlaying()) {
            emergencyPlayer.seekTo(0);
            emergencyPlayer.start();
        }
    }


    /** 비상 해제
     *
     */
    public void stopEmergency () {
        if (emergencyPlayer.isPlaying()) emergencyPlayer.pause();
    }


    /** 주의 주의
     *
     */
    public void playCaution () {
        if (!cautionPlayer.isPlaying()) {
            cautionPlayer.seekTo(0);
            cautionPlayer.start();
        }
    }


    /** 주의 해제
     *
     */
    public void stopCaution () {
        if (cautionPlayer.isPlaying()) cautionPlayer.pause();
    }


    /** 주의 주의
     *
     */
    public void playBle () {
        if (!blePlayer.isPlaying()) {
            blePlayer.seekTo(0);
            blePlayer.start();
        }
    }


    /** 주의 해제
     *
     */
    public void stopBle () {
        if (blePlayer.isPlaying()) blePlayer.pause();
    }


    /** 주의 주의
     *
     */
    public void playtScanedBeep () {
        if (!scanBeepPlayer.isPlaying()) {
            scanBeepPlayer.seekTo(0);
            scanBeepPlayer.start();
        }
    }


    /** 주의 해제
     *
     */
    public void stopScanedBeep () {
        if (scanBeepPlayer.isPlaying()) scanBeepPlayer.pause();
    }


    /** 모두 멈춰!!
     *
     */
    public void stopAll () {
        stopEmergency();
        stopCaution();
        stopBle ();
        stopScanedBeep ();
    }
}
