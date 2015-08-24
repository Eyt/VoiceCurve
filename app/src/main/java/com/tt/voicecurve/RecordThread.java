package com.tt.voicecurve;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 声音记录
 */
public class RecordThread extends Thread {
    public static final int UPDATE_VIEW = 101;
    private static int SAMPLE_RATE_IN_HZ = 8000;
    private boolean mIsRun;
    private AudioRecord mAudioRecord;
    private int mBufferSize;
    private Handler mHandler;

    public RecordThread(Handler handler) {
        this.mHandler = handler;
        mBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, mBufferSize);
    }

    @Override
    public void run() {
        mAudioRecord.startRecording();
        byte[] bytes = new byte[mBufferSize];
        mIsRun = true;
        int count = 0;
        while (mIsRun) {
            int r = mAudioRecord.read(bytes, 0, mBufferSize);
            if (r == AudioRecord.ERROR_INVALID_OPERATION) {
                Log.i("kyson", "AudioRecord.ERROR_INVALID_OPERATION");
                return;
            }
            if (r == AudioRecord.ERROR_BAD_VALUE) {
                Log.i("kyson", "AudioRecord.ERROR_BAD_VALUE");
                return;
            }
            if (r == 0) {
                return;
            }
            int v = 0;
            for (int i = 0; i < bytes.length; i++) {
                v += bytes[i] * bytes[i];
            }
            //分贝大小
            double value = 10 * Math.log((double) v / (double) r);
            Log.i("kyson", "分贝大小:" + value);
            count += 3;
            Message msg = mHandler.obtainMessage();
            msg.what = UPDATE_VIEW;
            //做这个转换仅仅是显示需要，没有任何意义
            msg.arg1 = (int) ((value - 60)*30);
            msg.arg2 = count;
            mHandler.sendMessage(msg);
        }
    }

    public void stopRecord() {
        if (mIsRun == true) {
            mIsRun = false;
        }
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    public void startRecord() {
        if (mIsRun == true) {
            return;
        }
        this.start();
        mIsRun = true;
    }

}
