package dlmj.hideseek.UI.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import dlmj.hideseek.Common.Params.IntentExtraParam;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.View.LoadingDialog;

/**
 * 内置浏览器<br/>
 * Created on 2016/10/19
 *
 * @author yekangqi
 */

public class BrowserActivity extends Activity implements View.OnClickListener {
    private  String title;
    private String url;

    private View mBackImageView;
    private View mBackTextView;
    private WebView mWebView;
    private TextView mBrowserTextView;
    private LoadingDialog mLoadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser);
        findView();
        initData();
        setListener();
    }

    private void initData() {
        title=getIntent().getStringExtra(IntentExtraParam.TITLE);
        url=getIntent().getStringExtra(IntentExtraParam.URL);
        mWebView.loadUrl(url);
        mBrowserTextView.setText(title);
    }

    private void findView()
    {
        mBackImageView =  findViewById(R.id.backImageView);
        mBackTextView =  findViewById(R.id.backTextView);
        mWebView= (WebView) findViewById(R.id.browserWebView);
        mBrowserTextView = (TextView) findViewById(R.id.browserTextView);

        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());

        WebSettings settings=mWebView.getSettings();
        settings.setJavaScriptEnabled(true);

        mLoadingDialog = new LoadingDialog(this, getString(R.string.loading));

    }

    private void setListener() {
        mBackTextView.setOnClickListener(this);
        mBackImageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backImageView:
            case R.id.backTextView:
                finish();
                break;
        }
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (!mLoadingDialog.isShowing())
            {
                mLoadingDialog.show();
            }
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
           if (mLoadingDialog.isShowing())
           {
               mLoadingDialog.dismiss();
           }
            super.onPageFinished(view, url);
        }
    }

    class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                // 网页加载完成
            } else {
                // 加载中
            }

        }
    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack())
        {
            mWebView.goBack();
            return;
        }
        super.onBackPressed();
    }

    /**
     * Web跳转
     * @param context
     * @param title 标题
     * @param url 链接
     */
    public static void toWeb(Context context, String title, String url)
    {
        context.startActivity(new Intent(context,BrowserActivity.class)
                .putExtra(IntentExtraParam.TITLE,title)
                .putExtra(IntentExtraParam.URL,url));
    }
    /**
     * Web跳转
     * @param context
     * @param title 标题
     * @param url 链接
     */
    public static void toWeb(Context context,int title, int url)
    {
        toWeb(context,context.getString(title),context.getString(url));
    }
}
