package ru.finkontrol.app;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;

public class MainActivity extends Activity {
  private WebView web;
  @Override public void onCreate(Bundle state) {
    super.onCreate(state);
    getWindow().setStatusBarColor(0xff070b12);
    getWindow().setNavigationBarColor(0xff070b12);
    web = new WebView(this);
    web.setBackgroundColor(0xff070b12);
    WebSettings s = web.getSettings();
    s.setJavaScriptEnabled(true);
    s.setDomStorageEnabled(true);
    s.setAllowFileAccess(true);
    s.setAllowContentAccess(true);
    s.setDefaultTextEncodingName("UTF-8");
    web.setWebViewClient(new WebViewClient());
    web.setWebChromeClient(new WebChromeClient());
    setContentView(web);
    web.loadUrl("file:///android_asset/index.html");
  }
  @Override public void onBackPressed() {
    if (web != null && web.canGoBack()) web.goBack();
    else super.onBackPressed();
  }
  @Override protected void onDestroy() {
    if(web != null){web.destroy();web=null;}
    super.onDestroy();
  }
}
