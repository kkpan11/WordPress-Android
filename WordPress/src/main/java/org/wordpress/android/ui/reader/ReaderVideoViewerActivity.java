package org.wordpress.android.ui.reader;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.wordpress.android.R;
import org.wordpress.android.fluxc.network.UserAgent;
import org.wordpress.android.ui.main.BaseAppCompatActivity;
import org.wordpress.android.util.helpers.WebChromeClientWithVideoPoster;

import javax.inject.Inject;

/**
 * Full screen video player for the reader
 */
public class ReaderVideoViewerActivity extends BaseAppCompatActivity {
    private String mVideoUrl;
    private WebView mWebView;
    private ProgressBar mProgress;

    @Inject UserAgent mUserAgent;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reader_activity_video_player);

        mWebView = (WebView) findViewById(R.id.web_view);
        mProgress = (ProgressBar) findViewById(R.id.progress);

        mWebView.setBackgroundColor(Color.TRANSPARENT);
        mWebView.getSettings().setJavaScriptEnabled(true);
        if (mUserAgent != null) {
            mWebView.getSettings().setUserAgentString(mUserAgent.toString());
        }

        mWebView.setWebChromeClient(new WebChromeClientWithVideoPoster(
                mWebView,
                org.wordpress.android.editor.R.drawable.media_movieclip
        ) {
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    mProgress.setVisibility(View.GONE);
                } else {
                    mProgress.setProgress(progress);
                    if (mProgress.getVisibility() != View.VISIBLE) {
                        mProgress.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        if (savedInstanceState == null) {
            mVideoUrl = getIntent().getStringExtra(ReaderConstants.ARG_VIDEO_URL);
            mWebView.loadUrl(mVideoUrl);
        } else {
            mVideoUrl = savedInstanceState.getString(ReaderConstants.ARG_VIDEO_URL);
            mWebView.restoreState(savedInstanceState);
        }
    }

    @Override
    protected void onDestroy() {
        // the video must be paused here or else the audio will continue to play
        // even though the activity has been destroyed
        mWebView.onPause();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(ReaderConstants.ARG_VIDEO_URL, mVideoUrl);
        mWebView.saveState(outState);
        super.onSaveInstanceState(outState);
    }
}
