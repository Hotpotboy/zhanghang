package com.zhang.mediademo;

import android.app.Activity;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.SohuVideo;
import io.vov.vitamio.widget.VideoView;


public class MainActivity extends Activity {

    private String path = "http://file.bmob.cn/M02/06/CC/oYYBAFZ0v8CAJzzaASkllrnhays039.mp4";//Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"VID_20151123_152518.mp4";
    private SohuVideo mVideoView;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (!LibsChecker.checkVitamioLibs(this))
            return;
        setContentView(R.layout.activity_main);
        mVideoView = (SohuVideo) findViewById(R.id.sohu_video);
        if (path == "") {
            // Tell the user to provide a media file URL/path.
            Toast.makeText(this,"Please edit VideoBuffer Activity, and set path"+ " variable to your media file URL/path", Toast.LENGTH_LONG).show();
            return;
        } else {
      /*
       * Alternatively,for streaming media you can use
       * mVideoView.setVideoURI(Uri.parse(URLstring));
       */
            mVideoView.setVideoPath(path);
            mVideoView.requestFocus();
        }

    }
}
