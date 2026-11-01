package be.wallshove;

import android.app.Activity;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * A thin shell around the live game on GitHub Pages, so every push to the repo
 * reaches the phone without reinstalling. When the phone is offline it falls
 * back to the copy of the game bundled in the APK.
 */
public class MainActivity extends Activity {

    private static final String LIVE_URL = "https://sypros.github.io/wallshove/";
    private static final String OFFLINE_URL = "file:///android_asset/index.html";

    private WebView web;
    private boolean fellBack = false;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        w.setStatusBarColor(Color.parseColor("#14121C"));
        w.setNavigationBarColor(Color.parseColor("#14121C"));

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#14121C"));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(true);
        s.setCacheMode(isOnline() ? WebSettings.LOAD_NO_CACHE : WebSettings.LOAD_CACHE_ELSE_NETWORK);
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
                if (req.isForMainFrame() && !fellBack) {
                    fellBack = true;
                    view.loadUrl(OFFLINE_URL);
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest req, WebResourceResponse resp) {
                // GitHub Pages not live yet (404) or down: play the bundled copy instead.
                if (req.isForMainFrame() && !fellBack && resp.getStatusCode() >= 400) {
                    fellBack = true;
                    view.loadUrl(OFFLINE_URL);
                }
            }
        });
        setContentView(web);
        immersive();

        if (state != null) web.restoreState(state);
        else web.loadUrl(isOnline() ? LIVE_URL : OFFLINE_URL);
    }

    private boolean isOnline() {
        ConnectivityManager cm = getSystemService(ConnectivityManager.class);
        if (cm == null || cm.getActiveNetwork() == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private void immersive() {
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.systemBars());
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) immersive();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    protected void onPause() { super.onPause(); web.onPause(); }

    @Override
    protected void onResume() { super.onResume(); web.onResume(); }

    /** Back pauses the game first; a second back on the pause screen resumes; back on the title closes the app. */
    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        web.evaluateJavascript("(window.wallshoveBack && window.wallshoveBack()) ? 'y' : 'n'", v -> {
            if (v == null || !v.contains("y")) MainActivity.super.onBackPressed();
        });
    }
}
