package com.example.arslan.browser;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;

import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ViewTreeObserver.OnScrollChangedListener, View.OnFocusChangeListener {

//    final GestureDetector gdt = new GestureDetector(new GestureListener());
//    private static final int SWIPE_MIN_DISTANCE = 20;
//    private static final int SWIPE_THRESHOLD_VELOCITY = 400;


    private WebView webView;
    private TextView address, titleBar;
    private ImageButton goOn, goBack, goForward, reload, share;
    private ProgressBar progressBar;
    private ImageView imageView;
    private LinearLayout topSide, titleSide;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String myCurrentUrl;
    boolean isDownloading;

    private static final String TAG = "Действие";
    private static final String http = "http://";
    private static final String noTabs = "No tabs";
    private static final String searchEngine = "https://www.google.com/search?q=";
    private static final String mainPage = "https://www.google.kz";
    private static final String textWhileDownload = "File is downloading...";

//    private final WebViewClient wbClient = new WebViewClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, String.valueOf(swipeRefreshLayout.getNestedScrollAxes()));
                webView.reload();
            }
        });
        topSide = (LinearLayout)findViewById(R.id.topSide);

        goOn = (ImageButton)findViewById(R.id.goButton);
        goOn.setOnClickListener(this);
        goBack = (ImageButton)findViewById(R.id.backButton);
        goBack.setOnClickListener(this);
        goForward = (ImageButton)findViewById(R.id.forwardButton);
        goForward.setOnClickListener(this);
        reload = (ImageButton)findViewById(R.id.reloadButton);
        reload.setOnClickListener(this);
        share = (ImageButton)findViewById(R.id.shareButton);
        share.setOnClickListener(this);

        titleBar = (TextView)findViewById(R.id.title);

        titleSide = (LinearLayout)findViewById(R.id.titleSide);

        imageView = (ImageView)findViewById(R.id.imageView);


        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setMax(100);





        address = (TextView)findViewById(R.id.editText);
        address.setOnFocusChangeListener(this);

        webView = (WebView)findViewById(R.id.web);
        address.setText(webView.getUrl());
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        webView.loadUrl(mainPage);
        webView.getViewTreeObserver().addOnScrollChangedListener(this);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                titleBar.setText(title);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
                imageView.setImageBitmap(icon);
            }

        });

//        webView.setOnTouchListener(this);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                webView.loadUrl(request.getUrl().toString());
                return true;
//                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                Log.d(TAG, "ggggg");
                String searchString = request.getUrl().toString().replaceAll(http, "");
                searchString = searchString.substring(0,searchString.length()-1);
                webView.loadUrl(searchEngine + searchString);

            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                isDownloading = true;
                reload.setImageResource(R.drawable.ic_clear_black_24dp);
                progressBar.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                isDownloading = false;
                reload.setImageResource(R.drawable.ic_refresh_black_24dp);
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                super.onPageFinished(view, url);
                myCurrentUrl = url;
                String ur = myCurrentUrl.replaceAll("https://", "");
                String finalUr = ur.substring(0, ur.indexOf("/"));
                address.setText(finalUr);
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                Log.d(TAG, "page commit");
                super.onPageCommitVisible(view, url);
            }
        });
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                DownloadManager.Request downloadRequest = new DownloadManager.Request(Uri.parse(url));
                downloadRequest.allowScanningByMediaScanner();
                downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);




                DownloadManager downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                downloadManager.enqueue(downloadRequest);


                Toast.makeText(MainActivity.this, textWhileDownload, Toast.LENGTH_SHORT).show();


            }
        });

        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    }


    @Override
    public void onBackPressed() {
        if(webView.canGoBack()){
            webView.goBack();
        }else {
            new AlertDialog.Builder(this).setMessage("Exit?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).setNegativeButton("No", null).show();

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.goButton:
                webView.loadUrl(http + address.getText().toString());
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                break;
            case R.id.backButton:
                if(webView.canGoBack()){
                    webView.goBack();
                }else{
                    Toast.makeText(this, noTabs, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.forwardButton:
                if(webView.canGoForward()){
                    webView.goForward();
                }else{
                    Toast.makeText(this, noTabs, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.reloadButton:
                if(isDownloading){
                    webView.stopLoading();
                }else {
                    webView.reload();
                }
                break;
            case R.id.shareButton:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, myCurrentUrl);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Copied URL");
                startActivity(Intent.createChooser(shareIntent, "Share URL"));
                break;
        }
    }

    @Override
    public void onScrollChanged() {
        if(webView.getScrollY()<10){
            Log.d("Visibile", String.valueOf(View.VISIBLE));
            topSide.setVisibility(View.VISIBLE);
        }else{
            Log.d("Visibile", String.valueOf(View.GONE));
            topSide.setVisibility(View.GONE);
        }
        if(webView.getScrollY()==0){
            swipeRefreshLayout.setEnabled(true);
        } else {
            swipeRefreshLayout.setEnabled(false);

        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    }

//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        Log.d(TAG, "Провел");
//        gdt.onTouchEvent(event);
//        return true;
//    }

//    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
//        @Override
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                return true; // справа налево
//            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                return true; // слева направо
//            }
//
//            if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//                Log.d(TAG, "снизу вверх");
//                return true; // снизу вверх
//
//            }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//                Log.d(TAG, "сверху вниз");
//                return true; // сверху вниз
//            }
//            return true;
//        }
//    }



//    @Override
//    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//        Log.d(TAG, "scrollX " + scrollX +
//                "\nscrollY " + scrollY +
//                "\noldScrollX " + oldScrollX +
//                "\noldScrollY" + oldScrollY);
//    }
}
