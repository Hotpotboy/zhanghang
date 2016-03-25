/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2013 YIXIA.COM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vov.vitamio.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.util.List;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.R;
import io.vov.vitamio.utils.ScreenResolution;
import io.vov.vitamio.utils.StringUtils;

/**
 * A view containing controls for a MediaPlayer. Typically contains the buttons
 * like "Play/Pause" and a progress slider. It takes care of synchronizing the
 * controls with the state of the MediaPlayer.
 * <p/>
 * The way to use this class is to a) instantiate it programatically or b)
 * create it in your xml layout.
 * <p/>
 * a) The MediaController will create a default set of controls and put them in
 * a window floating above your application. Specifically, the controls will
 * float above the view specified with setAnchorView(). By default, the window
 * will disappear if left idle for three seconds and reappear when the user
 * touches the anchor view. To customize the MediaController's style, layout and
 * controls you should extend MediaController and override the {#link
 * {@link #makeControllerView()} method.
 * <p/>
 * b) The MediaController is a FrameLayout, you can put it in your layout xml
 * and get it through {@link #findViewById(int)}.
 * <p/>
 * NOTES: In each way, if you want customize the MediaController, the SeekBar's
 * id must be mediacontroller_progress, the Play/Pause's must be
 * mediacontroller_pause, current time's must be mediacontroller_time_current,
 * total time's must be mediacontroller_time_total, file name's must be
 * mediacontroller_file_name. And your resources must have a pause_button
 * drawable and a play_button drawable.
 * <p/>
 * Functions like show() and hide() have no effect when MediaController is
 * created in an xml layout.
 */
public class SohuVideo extends FrameLayout implements MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener, ViewTreeObserver.OnPreDrawListener,SohuVideoView.OnPlayerTurnChangeListener {
    private static final int SHOW_PROGRESS = 2;
    /**声音、亮度等属性的最大展示值*/
    private static final int MAX_PLAYER_FIELD_VALUE = 1000;
    private static final String TAG = "SohuVideo";
    private boolean isFullScreen = false;
    /**
     * 原始宽度和原始高度
     */
    private int orgWidth, orgHeight;
    /**
     * 窗口宽度和窗口高度
     */
    private int windowWidth, windowHeight;
    private SohuVideoView mPlayer;
    private Context mContext;
    private FrameLayout mRoot;
    private SeekBar mProgress;
    private TextView mEndTime, mCurrentTime;
    private TextView mFileName;
    private String mTitle;
    private long mDuration;
    private boolean mDragging;
    private ImageButton mPauseButton;
    private AudioManager mAM;
    private ProgressBar pb;
    private TextView downloadRateView, loadRateView;
    /**全屏按钮*/
    private ImageView fullScreenButton;
    /**调整声音、亮度等属性的展示背景*/
    private ImageView turnBgImage;
    /**调整声音、亮度等属性的数值进度条*/
    private ProgressBar turnNumPro;
    /**关注按钮*/
    private ImageView guanzhuButton;
    /**关注行为改变监听器*/
    private OnGuanzhuChangeListener mOnGuanzhuChangeListener;
    /**收藏按钮*/
    private ImageView souCangButton;
    /**收藏行为改变监听器*/
    private OnSoucangChangeListener mOnSoucangChangeListener;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_PROGRESS:
                    long position = mPlayer.getCurrentPosition();
                    long duration = mPlayer.getDuration();
                    mDuration = duration;
                    setProgress(position);
                    if (!mDragging && position < mDuration) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000);
                    }
                    updatePausePlay();
                    break;
            }
        }
    };
    private OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            int i = v.getId();
            if (i == R.id.sohu_view_mediacontroller_play_pause) {
                doPauseResume();
            } else if (i == R.id.sohu_view_full_screen) {
                doFullScreen();
            }else if(i == R.id.sohu_view_soucang){
                doSouCang();
            }else if(i == R.id.sohu_view_guanzhu){
                doGuanZhu();
            }
        }
    };

    private void doGuanZhu() {
        if(guanzhuButton.isSelected()){
            guanzhuButton.setSelected(false);
        }else{
            guanzhuButton.setSelected(true);
        }
        invokeGuanzhuChangeListener();
    }

    private void doSouCang() {
        if(souCangButton.isSelected()){
            souCangButton.setSelected(false);
        }else{
            souCangButton.setSelected(true);
        }
        invokeSoucangChangeListener();
    }

    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            mDragging = true;
            mPlayer.pause();
            mHandler.removeMessages(SHOW_PROGRESS);
            updatePausePlay();
            mAM.setStreamMute(AudioManager.STREAM_MUSIC, true);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser)
                return;

            long newposition = (mDuration * progress) / 1000;
            String time = StringUtils.generateTime(newposition);
            mPlayer.seekTo(newposition);
            if (mCurrentTime != null)
                mCurrentTime.setText(time);
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mPlayer.seekTo((mDuration * bar.getProgress()) / 1000);
            mAM.setStreamMute(AudioManager.STREAM_MUSIC, false);
            mDragging = false;
            mPlayer.start();
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
            updatePausePlay();
        }
    };

    public SohuVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SohuVideo(Context context) {
        super(context);
        init(context);
    }

    private boolean init(Context context) {
        getViewTreeObserver().addOnPreDrawListener(this);
        mContext = context;
        Pair<Integer, Integer> res = ScreenResolution.getResolution(mContext);
        windowWidth = res.first;
        windowHeight = res.second;
        mRoot = this;
        makeControllerView();
        try {
            mAM = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        } catch (Exception e) {
            mAM = null;
        }
        return true;
    }

    /**
     * Create the view that holds the widgets that control playback. Derived
     * classes can override this to create their own.
     *
     * @return The controller view.
     */
    protected void makeControllerView() {
        LayoutInflater.from(mContext).inflate(R.layout.sohu_video, this);
    }

    @Override
    public void onFinishInflate() {
        if (mRoot != null)
            initControllerView(mRoot);
    }

    private void initControllerView(View v) {
        mPlayer = (SohuVideoView) v.findViewById(R.id.sohu_view_buffer);
        mPauseButton = (ImageButton) v.findViewById(R.id.sohu_view_mediacontroller_play_pause);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mOnClickListener);
        }

        mProgress = (SeekBar) v.findViewById(R.id.sohu_view_mediacontroller_seekbar);
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }

        fullScreenButton = (ImageView) v.findViewById(R.id.sohu_view_full_screen);
        if (fullScreenButton != null) {
            fullScreenButton.setOnClickListener(mOnClickListener);
        }

        mEndTime = (TextView) v.findViewById(R.id.sohu_view_mediacontroller_time_total);
        mCurrentTime = (TextView) v.findViewById(R.id.sohu_view_mediacontroller_time_current);
        mFileName = (TextView) v.findViewById(R.id.sohu_view_mediacontroller_file_name);
        if (mFileName != null)
            mFileName.setText(mTitle);
        pb = (ProgressBar) findViewById(R.id.sohu_view_probar);

        downloadRateView = (TextView) findViewById(R.id.sohu_view_download_rate);
        loadRateView = (TextView) findViewById(R.id.sohu_view_load_rate);

        turnBgImage = (ImageView) findViewById(R.id.sohu_view_turn_image);
        turnNumPro = (ProgressBar) findViewById(R.id.sohu_view_turn_progressBar);

        souCangButton = (ImageView) findViewById(R.id.sohu_view_soucang);
        souCangButton.setOnClickListener(mOnClickListener);
        setSoucang(GONE, false);
        guanzhuButton = (ImageView) findViewById(R.id.sohu_view_guanzhu);
        guanzhuButton.setOnClickListener(mOnClickListener);
        setGuanzhu(GONE, false);

        mPlayer.setOnInfoListener(this);
        mPlayer.setOnBufferingUpdateListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnPlayerTurnChangeListener(this);
    }

    /**
     * Set the content of the file_name TextView
     *
     * @param name
     */
    public void setFileName(String name) {
        mTitle = name;
        if (mFileName != null)
            mFileName.setText(mTitle);
    }

    private void setProgress(long position) {
        if (mPlayer == null || mDragging)
            return;


        if (mProgress != null) {
            if (mDuration > 0) {
                long pos = 1000L * position / mDuration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null)
            mEndTime.setText(StringUtils.generateTime(mDuration));
        if (mCurrentTime != null)
            mCurrentTime.setText(StringUtils.generateTime(position));
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.getRepeatCount() == 0 && (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE)) {
            doPauseResume();
            if (mPauseButton != null)
                mPauseButton.requestFocus();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                updatePausePlay();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        } else {
        }
        return super.dispatchKeyEvent(event);
    }

    private void updatePausePlay() {
        if (mRoot == null || mPauseButton == null)
            return;

        if (mPlayer.isPlaying())
            mPauseButton.setImageResource(R.drawable.focus_mediacontroller_pause);
        else
            mPauseButton.setImageResource(R.drawable.focus_mediacontroller_play);
    }

    private void doPauseResume() {
        if (mPlayer.isPlaying()) {
            mHandler.removeMessages(SHOW_PROGRESS);
            mPlayer.pause();
        } else {
            if(mPlayer.getCurrentPosition()/1000<mPlayer.getDuration()/1000) {
                mPlayer.start();
                mHandler.sendEmptyMessage(SHOW_PROGRESS);
            }
        }
        updatePausePlay();
    }

    private void doFullScreen() {
        ViewGroup.LayoutParams params = getLayoutParams();
        if (!isFullScreen) {//全屏
            isFullScreen = true;
            ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            ((Activity) mContext).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            params.width = windowHeight;
            params.height = windowWidth;
            fullScreenButton.setSelected(true);
            setSoucang(VISIBLE, souCangButton.isSelected());
            setGuanzhu(VISIBLE,guanzhuButton.isSelected());
        } else {//非全屏
            isFullScreen = false;
            ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            ((Activity) mContext).getWindow().setFlags(~WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            params.width = orgWidth;
            params.height = orgHeight;
            fullScreenButton.setSelected(false);
            setSoucang(GONE, souCangButton.isSelected());
            setGuanzhu(GONE, guanzhuButton.isSelected());
        }
        requestLayout();
        post(new Runnable() {
            @Override
            public void run() {
                mPlayer.setVideoLayout();
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null)
            mPauseButton.setEnabled(enabled);
        if (mProgress != null)
            mProgress.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    public void setVideoPath(String path) {
        Uri url = Uri.parse(path);
        mPlayer.setVideoURI(url);

        if (url != null) {
            List<String> paths = url.getPathSegments();
            String name = paths == null || paths.isEmpty() ? "null" : paths.get(paths.size() - 1);
            setFileName(name);
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                    pb.setVisibility(View.VISIBLE);
                    downloadRateView.setText("");
                    loadRateView.setText("");
                    downloadRateView.setVisibility(View.VISIBLE);
                    loadRateView.setVisibility(View.VISIBLE);

                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                mPlayer.start();
                pb.setVisibility(View.GONE);
                downloadRateView.setVisibility(View.GONE);
                loadRateView.setVisibility(View.GONE);
                break;
            case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                downloadRateView.setText("" + extra + "kb/s" + "  ");
                break;
        }
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        loadRateView.setText(percent + "%");
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.setPlaybackSpeed(1.0f);
        setEnabled(true);
        updatePausePlay();
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
    }

    @Override
    public boolean onPreDraw() {
        orgWidth = getMeasuredWidth();
        orgHeight = getMeasuredHeight();
        getViewTreeObserver().removeOnPreDrawListener(this);
        return true;
    }

    @Override
    public void onTurnChange(SohuVideoView.VideoField videoField, float percent) {
        if(turnBgImage.getVisibility()!=VISIBLE){
            turnBgImage.setVisibility(VISIBLE);
            if(videoField== SohuVideoView.VideoField.BRIGHTENESS){
                turnBgImage.setImageResource(R.drawable.focus_video_brightness_bg);
            }else if(videoField== SohuVideoView.VideoField.VOLUME){
                turnBgImage.setImageResource(R.drawable.focus_video_volumn_bg);
            }
        }
        if(turnNumPro.getVisibility()!=VISIBLE){
            turnNumPro.setVisibility(VISIBLE);
        }
        turnNumPro.setProgress((int) (percent * MAX_PLAYER_FIELD_VALUE));
    }

    @Override
    public void endTurnChange() {
        if(turnBgImage.getVisibility()!=GONE){
            turnBgImage.setVisibility(GONE);
        }
        if(turnNumPro.getVisibility()!=GONE){
            turnNumPro.setVisibility(GONE);
        }
    }

    public void setOnGuanzhuChangeListener(OnGuanzhuChangeListener onGuanzhuChangeListener) {
        mOnGuanzhuChangeListener = onGuanzhuChangeListener;
    }

    public void setOnSoucangChangeListener(OnSoucangChangeListener onSoucangChangeListener) {
        mOnSoucangChangeListener = onSoucangChangeListener;
    }

    private void invokeGuanzhuChangeListener(){
        if(mOnGuanzhuChangeListener!=null){
            mOnGuanzhuChangeListener.onGuanzhuChange(guanzhuButton.isSelected());
        }
    }

    private void invokeSoucangChangeListener(){
        if(mOnSoucangChangeListener!=null){
            mOnSoucangChangeListener.onSoucangChange(souCangButton.isSelected());
        }
    }

    /**
     *
     * @param visibile   关注按钮的显示状态（是否可见）
     * @param isGuanzhu  是否关注
     */
    public void setGuanzhu(int visibile,boolean isGuanzhu){
        guanzhuButton.setVisibility(visibile);
        if(guanzhuButton.isSelected()!=isGuanzhu){
            guanzhuButton.setSelected(isGuanzhu);
            invokeGuanzhuChangeListener();
        }
    }

    /**
     *
     * @param visibile   收藏按钮的显示状态（是否可见）
     * @param isSoucang  是否收藏
     */
    public void setSoucang(int visibile,boolean isSoucang){
        souCangButton.setVisibility(visibile);
        if(souCangButton.isSelected()!=isSoucang){
            souCangButton.setSelected(isSoucang);
            invokeSoucangChangeListener();
        }
    }


    /**
     * 点击关注按钮的回调接口
     */
    public interface OnGuanzhuChangeListener{
        void onGuanzhuChange(boolean isGuanzhu);
    }
    /**点击收藏按钮的回调接口*/
    public interface OnSoucangChangeListener{
        void onSoucangChange(boolean isSoucang);
    }
}
