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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

import java.io.IOException;
import java.util.Map;

import io.vov.vitamio.MediaFormat;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.MediaPlayer.OnSeekCompleteListener;
import io.vov.vitamio.MediaPlayer.OnTimedTextListener;
import io.vov.vitamio.MediaPlayer.OnVideoSizeChangedListener;
import io.vov.vitamio.MediaPlayer.TrackInfo;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.utils.Log;

/**
 * Displays a video file. The VideoView class can load images from various
 * sources (such as resources or content providers), takes care of computing its
 * measurement from the video so that it can be used in any layout manager, and
 * provides various display options such as scaling and tinting.
 * <p/>
 * VideoView also provide many wrapper methods for
 * {@link MediaPlayer}, such as {@link #getVideoWidth()},
 * {@link #setTimedTextShown(boolean)}
 */
public class SohuVideoView extends SurfaceView implements MediaController.MediaPlayerControl {
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private static final int STATE_SUSPEND = 6;
    private static final int STATE_RESUME = 7;
    private static final int STATE_SUSPEND_UNSUPPORTED = 8;
    private static final String TAG = "SohuVideoView.class";
    OnVideoSizeChangedListener mSizeChangedListener = new OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            Log.d("onVideoSizeChanged: (%dx%d)", width, height);
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            mVideoAspectRatio = mp.getVideoAspectRatio();
            if (mVideoWidth != 0 && mVideoHeight != 0)
                setVideoLayout();
        }
    };
    OnPreparedListener mPreparedListener = new OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            Log.d("onPrepared");
            mCurrentState = STATE_PREPARED;
            // mTargetState = STATE_PLAYING;

            // Get the capabilities of the player for this stream
            //TODO mCanPause

            if (mOnPreparedListener != null)
                mOnPreparedListener.onPrepared(mMediaPlayer);
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            mVideoAspectRatio = mp.getVideoAspectRatio();

            long seekToPosition = mSeekWhenPrepared;
            if (seekToPosition != 0)
                seekTo(seekToPosition);

            if (mVideoWidth != 0 && mVideoHeight != 0) {
                setVideoLayout();
                if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                    if (mTargetState == STATE_PLAYING) {
                        start();
                    }
                }
            } else if (mTargetState == STATE_PLAYING) {
                start();
            }
        }
    };
    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0)
                    seekTo(mSeekWhenPrepared);
                start();
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            if (mMediaPlayer != null && mCurrentState == STATE_SUSPEND && mTargetState == STATE_RESUME) {
                mMediaPlayer.setDisplay(mSurfaceHolder);
                resume();
            } else {
                openVideo();
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            mSurfaceHolder = null;
            release(true);
        }
    };
    private Uri mUri;
    private long mDuration;
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;
    private SurfaceHolder mSurfaceHolder = null;
    private MediaPlayer mMediaPlayer = null;
    private int mVideoWidth;
    private int mVideoHeight;
    private float mVideoAspectRatio;
    private int mVideoChroma = MediaPlayer.VIDEOCHROMA_RGBA;
    private boolean mHardwareDecoder = false;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    private View mMediaBufferingIndicator;
    private OnCompletionListener mOnCompletionListener;
    private OnPreparedListener mOnPreparedListener;
    private OnErrorListener mOnErrorListener;
    private OnSeekCompleteListener mOnSeekCompleteListener;
    private OnTimedTextListener mOnTimedTextListener;
    private OnInfoListener mOnInfoListener;
    private OnBufferingUpdateListener mOnBufferingUpdateListener;
    private int mCurrentBufferPercentage;
    private long mSeekWhenPrepared; // recording the seek position while preparing
    private Context mContext;
    private Map<String, String> mHeaders;
    private int mBufSize;
    private GestureDetector mGestureDetector;
    private OnPlayerTurnChangeListener mOnPlayerTurnChangeListener;
    /**
     * 亮度
     */
    private float mBrightness = -1, mVolume = -1;
    private OnCompletionListener mCompletionListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            Log.d("onCompletion");
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;
            if (mOnCompletionListener != null)
                mOnCompletionListener.onCompletion(mMediaPlayer);
        }
    };
    private OnErrorListener mErrorListener = new OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            Log.d("Error: %d, %d", framework_err, impl_err);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;

            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err))
                    return true;
            }

            if (getWindowToken() != null) {
                int message = framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK ? getResources().getIdentifier("VideoView_error_text_invalid_progressive_playback", "string", mContext.getPackageName()) : getResources().getIdentifier("VideoView_error_text_unknown", "string", mContext.getPackageName());

                new AlertDialog.Builder(mContext).setTitle(getResources().getIdentifier("VideoView_error_title", "string", mContext.getPackageName())).setMessage(message).setPositiveButton(getResources().getIdentifier("VideoView_error_button", "string", mContext.getPackageName()), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (mOnCompletionListener != null)
                            mOnCompletionListener.onCompletion(mMediaPlayer);
                    }
                }).setCancelable(false).show();
            }
            return true;
        }
    };
    private OnBufferingUpdateListener mBufferingUpdateListener = new OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mCurrentBufferPercentage = percent;
            if (mOnBufferingUpdateListener != null)
                mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
        }
    };
    private OnInfoListener mInfoListener = new OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            Log.d("onInfo: (%d, %d)", what, extra);
            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(mp, what, extra);
            } else if (mMediaPlayer != null) {
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    mMediaPlayer.pause();
                    if (mMediaBufferingIndicator != null)
                        mMediaBufferingIndicator.setVisibility(View.VISIBLE);
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    mMediaPlayer.start();
                    if (mMediaBufferingIndicator != null)
                        mMediaBufferingIndicator.setVisibility(View.GONE);
                }
            }
            return true;
        }
    };
    private OnSeekCompleteListener mSeekCompleteListener = new OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            Log.d("onSeekComplete");
            if (mOnSeekCompleteListener != null)
                mOnSeekCompleteListener.onSeekComplete(mp);
        }
    };
    private OnTimedTextListener mTimedTextListener = new OnTimedTextListener() {
        @Override
        public void onTimedTextUpdate(byte[] pixels, int width, int height) {
            Log.i("onSubtitleUpdate: bitmap subtitle, %dx%d", width, height);
            if (mOnTimedTextListener != null)
                mOnTimedTextListener.onTimedTextUpdate(pixels, width, height);
        }

        @Override
        public void onTimedText(String text) {
            Log.i("onSubtitleUpdate: %s", text);
            if (mOnTimedTextListener != null)
                mOnTimedTextListener.onTimedText(text);
        }
    };

    public SohuVideoView(Context context) {
        super(context);
        initVideoView(context);
    }

    public SohuVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initVideoView(context);
    }

    public SohuVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initVideoView(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    public void setOnPlayerTurnChangeListener(OnPlayerTurnChangeListener listener) {
        mOnPlayerTurnChangeListener = listener;
    }

    /**
     * Set the display options
     */
    public void setVideoLayout() {
        LayoutParams lp = getLayoutParams();
        mSurfaceHeight = mVideoHeight;
        mSurfaceWidth = mVideoWidth;
        ViewGroup parent = (ViewGroup) getParent();
        lp.width = parent.getWidth();
        lp.height = parent.getHeight();
        setLayoutParams(lp);
        if (mVideoWidth != lp.width) mSurfaceWidth = lp.width;
        if (mSurfaceHeight != lp.height) mSurfaceHeight = lp.height;
        getHolder().setFixedSize(mSurfaceWidth, mSurfaceHeight);
    }

    @SuppressWarnings("deprecation")
    private void initVideoView(Context ctx) {
        mContext = ctx;
        mVideoWidth = 0;
        mVideoHeight = 0;
        getHolder().setFormat(PixelFormat.RGBA_8888); // PixelFormat.RGB_565
        getHolder().addCallback(mSHCallback);
        // this value only use Hardware decoder before Android 2.3
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB && mHardwareDecoder) {
            getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
        if (ctx instanceof Activity)
            ((Activity) ctx).setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mGestureDetector = new GestureDetector(mContext, new MyGestureListener());
    }

    public boolean isValid() {
        return (mSurfaceHolder != null && mSurfaceHolder.getSurface().isValid());
    }

    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    public void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
        }
    }

    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null || !Vitamio.isInitialized(mContext))
            return;

        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);

        release(false);
        try {
            mDuration = -1;
            mCurrentBufferPercentage = 0;
            mMediaPlayer = new MediaPlayer(mContext, mHardwareDecoder);
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
            mMediaPlayer.setOnTimedTextListener(mTimedTextListener);
            mMediaPlayer.setDataSource(mContext, mUri, mHeaders);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setBufferSize(mBufSize);
            mMediaPlayer.setVideoChroma(mVideoChroma == MediaPlayer.VIDEOCHROMA_RGB565 ? MediaPlayer.VIDEOCHROMA_RGB565 : MediaPlayer.VIDEOCHROMA_RGBA);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
        } catch (IOException ex) {
            Log.e("Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            Log.e("Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        }catch (Exception e){
            Log.e("Unable to open content: " + mUri, e);
        }
    }

    public void setMediaBufferingIndicator(View mediaBufferingIndicator) {
        if (mMediaBufferingIndicator != null)
            mMediaBufferingIndicator.setVisibility(View.GONE);
        mMediaBufferingIndicator = mediaBufferingIndicator;
    }

    public void setOnPreparedListener(OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    public void setOnCompletionListener(OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    public void setOnErrorListener(OnErrorListener l) {
        mOnErrorListener = l;
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener l) {
        mOnBufferingUpdateListener = l;
    }

    public void setOnSeekCompleteListener(OnSeekCompleteListener l) {
        mOnSeekCompleteListener = l;
    }

    public void setOnTimedTextListener(OnTimedTextListener l) {
        mOnTimedTextListener = l;
    }

    public void setOnInfoListener(OnInfoListener l) {
        mOnInfoListener = l;
    }

    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate)
                mTargetState = STATE_IDLE;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK && keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN && keyCode != KeyEvent.KEYCODE_MENU && keyCode != KeyEvent.KEYCODE_CALL && keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                } else {
                    start();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                }
                return true;
            } else {

            }
        }

        return super.onKeyDown(keyCode, event);
    }

    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    public void suspend() {
        if (isInPlaybackState()) {
            release(false);
            mCurrentState = STATE_SUSPEND_UNSUPPORTED;
            Log.d("Unable to suspend video. Release MediaPlayer.");
        }
    }

    public void resume() {
        if (mSurfaceHolder == null && mCurrentState == STATE_SUSPEND) {
            mTargetState = STATE_RESUME;
        } else if (mCurrentState == STATE_SUSPEND_UNSUPPORTED) {
            openVideo();
        }
    }

    public long getDuration() {
        if (isInPlaybackState()) {
            if (mDuration > 0)
                return mDuration;
            mDuration = mMediaPlayer.getDuration();
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
    }

    public long getCurrentPosition() {
        if (isInPlaybackState())
            return mMediaPlayer.getCurrentPosition();
        return 0;
    }

    public void seekTo(long msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    public int getBufferPercentage() {
        if (mMediaPlayer != null)
            return mCurrentBufferPercentage;
        return 0;
    }

    public void setVolume(float leftVolume, float rightVolume) {
        if (mMediaPlayer != null)
            mMediaPlayer.setVolume(leftVolume, rightVolume);
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public float getVideoAspectRatio() {
        return mVideoAspectRatio;
    }

    /**
     * Must set before {@link #setVideoURI}
     *
     * @param chroma
     */
    public void setVideoChroma(int chroma) {
        getHolder().setFormat(chroma == MediaPlayer.VIDEOCHROMA_RGB565 ? PixelFormat.RGB_565 : PixelFormat.RGBA_8888); // PixelFormat.RGB_565
        mVideoChroma = chroma;
    }

    public void setHardwareDecoder(boolean hardware) {
        mHardwareDecoder = hardware;
    }

    public void setVideoQuality(int quality) {
        if (mMediaPlayer != null)
            mMediaPlayer.setVideoQuality(quality);
    }

    public void setBufferSize(int bufSize) {
        mBufSize = bufSize;
    }

    public boolean isBuffering() {
        if (mMediaPlayer != null)
            return mMediaPlayer.isBuffering();
        return false;
    }

    public String getMetaEncoding() {
        if (mMediaPlayer != null)
            return mMediaPlayer.getMetaEncoding();
        return null;
    }

    public void setMetaEncoding(String encoding) {
        if (mMediaPlayer != null)
            mMediaPlayer.setMetaEncoding(encoding);
    }

    public SparseArray<MediaFormat> getAudioTrackMap(String encoding) {
        if (mMediaPlayer != null)
            return mMediaPlayer.findTrackFromTrackInfo(TrackInfo.MEDIA_TRACK_TYPE_AUDIO, mMediaPlayer.getTrackInfo(encoding));
        return null;
    }

    public int getAudioTrack() {
        if (mMediaPlayer != null)
            return mMediaPlayer.getAudioTrack();
        return -1;
    }

    public void setAudioTrack(int audioIndex) {
        if (mMediaPlayer != null)
            mMediaPlayer.selectTrack(audioIndex);
    }

    public void setTimedTextShown(boolean shown) {
        if (mMediaPlayer != null)
            mMediaPlayer.setTimedTextShown(shown);
    }

    public void setTimedTextEncoding(String encoding) {
        if (mMediaPlayer != null)
            mMediaPlayer.setTimedTextEncoding(encoding);
    }

    public int getTimedTextLocation() {
        if (mMediaPlayer != null)
            return mMediaPlayer.getTimedTextLocation();
        return -1;
    }

    public void addTimedTextSource(String subPath) {
        if (mMediaPlayer != null)
            mMediaPlayer.addTimedTextSource(subPath);
    }

    public String getTimedTextPath() {
        if (mMediaPlayer != null)
            return mMediaPlayer.getTimedTextPath();
        return null;
    }

    public void setSubTrack(int trackId) {
        if (mMediaPlayer != null)
            mMediaPlayer.selectTrack(trackId);
    }

    public int getTimedTextTrack() {
        if (mMediaPlayer != null)
            return mMediaPlayer.getTimedTextTrack();
        return -1;
    }

    public SparseArray<MediaFormat> getSubTrackMap(String encoding) {
        if (mMediaPlayer != null)
            return mMediaPlayer.findTrackFromTrackInfo(TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT, mMediaPlayer.getTrackInfo(encoding));
        return null;
    }

    protected boolean isInPlaybackState() {
        return (mMediaPlayer != null && mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }

        // 处理手势结束
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                endGesture();
                break;
            case MotionEvent.ACTION_DOWN:
                return true;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 手势结束
     */
    private void endGesture() {
        mVolume = -1;
        mBrightness = -1f;
        if (mOnPlayerTurnChangeListener != null) {
            mOnPlayerTurnChangeListener.endTurnChange();
        }
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            float mOldX = e1.getX(), mOldY = e1.getY();
            int y = (int) e2.getRawY();

            if (mOldX > mSurfaceWidth * 4.0 / 5) {// 右边滑动
                onVolumeSlide((mOldY - y) / mSurfaceHeight);
            } else if (mOldX < mSurfaceWidth / 5.0) {// 左边滑动
                onBrightnessSlide((mOldY - y) / mSurfaceHeight);
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        /**
         * 滑动改变声音大小
         *
         * @param percent
         */
        private void onVolumeSlide(float percent) {
            AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            int mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            if (mVolume == -1) {
                mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (mVolume < 0)
                    mVolume = 0;
            }

            float index = percent * mMaxVolume + mVolume;
            if (index > mMaxVolume)
                index = mMaxVolume;
            else if (index < 0)
                index = 0;

            // 变更声音
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) index, 0);
            if (mOnPlayerTurnChangeListener != null) {
                mOnPlayerTurnChangeListener.onTurnChange(VideoField.VOLUME, index / mMaxVolume);
            }
        }

        /**
         * 滑动改变亮度
         *
         * @param percent
         */
        private void onBrightnessSlide(float percent) {
            if (mBrightness < 0) {
                mBrightness = ((Activity) mContext).getWindow().getAttributes().screenBrightness;
                if (mBrightness <= 0.00f)
                    mBrightness = 0.50f;
                if (mBrightness < 0.01f)
                    mBrightness = 0.01f;
            }
            WindowManager.LayoutParams lpa = ((Activity) mContext).getWindow().getAttributes();
            lpa.screenBrightness = mBrightness + percent;
            android.util.Log.e(TAG, lpa.screenBrightness + "");
            if (lpa.screenBrightness > 1.0f)
                lpa.screenBrightness = 1.0f;
            else if (lpa.screenBrightness < 0.01f)
                lpa.screenBrightness = 0.01f;
            ((Activity) mContext).getWindow().setAttributes(lpa);
            if (mOnPlayerTurnChangeListener != null) {
                mOnPlayerTurnChangeListener.onTurnChange(VideoField.BRIGHTENESS, mBrightness + percent);
            }
        }
    }

    /**
     * 音量、亮度等视频属性调整接口
     */
    interface OnPlayerTurnChangeListener {
        /**
         * 发生改变
         *
         * @param videoField 修改的属性类型
         * @param percent    当前百分比（0~1.0）
         */
        void onTurnChange(VideoField videoField, float percent);

        /**
         * 结束改变
         */
        void endTurnChange();
    }

    enum VideoField {
        /**
         * 亮度
         */
        BRIGHTENESS,
        /**
         * 声音
         */
        VOLUME
    }
}