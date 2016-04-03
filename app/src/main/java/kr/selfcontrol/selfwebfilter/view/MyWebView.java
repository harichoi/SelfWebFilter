package kr.selfcontrol.selfwebfilter.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;

/**
 * Created by owner2 on 2016-01-17.
 */
public class MyWebView extends WebView {

    public MyWebView(Context context) {
        super(context, null);
    }

    public MyWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void  postUrl(String  url, byte[] postData)
    {
        Log.d("shouldPost",url+":"+postData);
        super.postUrl(url, postData);
    }
}
