package com.akhnaton.foodvisits.ui.home;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.akhnaton.foodvisits.R;

public class WebOrderActivity extends AppCompatActivity {
    private static final String TAG = "Main";

    String employee_id;
    boolean doubleBackToExitPressedOnce;
    private WebView webview;
    private SwipeRefreshLayout swipeView;
    private ProgressBar progressBar;
    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_order);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        webview = findViewById(R.id.web_view);
        swipeView = findViewById(R.id.swipeRefreshLayout3);
        WebSettings settings = webview.getSettings();
        //settings.setJavaScriptEnabled(true);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);
        progressBar.setMax(100);
        webview.setWebViewClient(new WebViewClientDemo());
        webview.setWebChromeClient(new WebChromeClientDemo());
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setJavaScriptEnabled(true);
        //webview.loadUrl("http://www.google.com/");
        webview.loadUrl(getResources().getString(R.string.web_order_activity) + employee_id);
//        webview.loadUrl("https://oso.akhnatontrade.com/menu.php?emp_id=" + employee_id);
        swipeView.setColorSchemeColors(Color.GRAY, Color.GREEN, Color.BLUE,
                Color.RED, Color.CYAN);
        swipeView.setDistanceToTriggerSync(20);// in dips
        swipeView.setSize(SwipeRefreshLayout.DEFAULT);// LARGE also can be used
        //getLocation();
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webview.reload();
                //Webview.this.webview.loadUrl("https://oso.akhnatontrade.com/payMob.php?user_id=" + employee_id + "&party_site=" + party_site);

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mOnScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = webview.getScrollY();
                if (scrollY == 0)
                    swipeView.setEnabled(true);
                else
                    swipeView.setEnabled(false);

            }
        };
        swipeView.getViewTreeObserver().addOnScrollChangedListener(mOnScrollChangedListener);

    }

    @Override
    protected void onStop() {
        swipeView.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollChangedListener);
        super.onStop();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.i(TAG, "onBackPressed");

        if (doubleBackToExitPressedOnce) {
            Log.i(TAG, "double click");

            finish();

            return;
        } else {
            Log.i(TAG, "single click");
            if (webview.canGoBack()) {
                Log.i(TAG, "canGoBack");
                webview.goBack();
                swipeView.setRefreshing(false);
            } else {
                Log.i(TAG, "nothing to canGoBack");
            }
        }

        this.doubleBackToExitPressedOnce = true;
        if (getApplicationContext() == null) {
            return;
        } else {
            Toast.makeText(this, "Please click BACK again to exit",
                    Toast.LENGTH_SHORT).show();
        }
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    private class WebViewClientDemo extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);

            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(100);
            swipeView.setRefreshing(false);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if (error.toString() == "piglet")
                handler.cancel();
            else
                handler.proceed();
        }
    }

    private class WebChromeClientDemo extends WebChromeClient {
        public void onProgressChanged(WebView view, int progress) {
            progressBar.setProgress(progress);
        }

    }
}

