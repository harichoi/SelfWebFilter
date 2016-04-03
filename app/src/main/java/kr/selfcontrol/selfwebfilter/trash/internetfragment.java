package kr.selfcontrol.selfwebfilter.trash;

/**
 * Created by owner2 on 2016-04-02.
 */

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.Browser;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import kr.selfcontrol.selfwebfilter.R;
import kr.selfcontrol.selfwebfilter.dao.WebFilterDao;
import kr.selfcontrol.selfwebfilter.util.SelfControlUtil;
import kr.selfcontrol.selfwebfilter.view.MyWebView;
import kr.selfcontrol.selfwebfilter.vo.BlockVo;
import kr.selfcontrol.selfwebfilter.vo.GroupVo;

public class internetfragment extends Fragment implements TextView.OnEditorActionListener{

    List<BlockVo> blockUrlList=new ArrayList<>();
    List<BlockVo> blockCntsList=new ArrayList<>();
    List<BlockVo> cautionList=new ArrayList<>();
    List<BlockVo> trustList=new ArrayList<>();
    List<GroupVo> groupVoList;
    private EditText urlText;
    private ImageButton drawerButton;
    private ImageButton button;
    private ProgressBar progressBar;
    public MyWebView webView;
    public String userAgent;
    public String basicUrl="http://google.com";
    private View view;
    private OnUrlChanged mOnUrlChanged;
    private OnDrawerButtonListener mOnDrawerButtonListener;
    WebFilterDao webFilterDao;
    String baseDir="";
    /////////////////////////
    private FrameLayout mTargetView;
    private FrameLayout mContentView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    public View mCustomView;
    public MyWebChromeClient mClient;
    //////////////////////////
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            //   webView.loadUrl("about:blank");
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    public void updateBlockData(){
        if(webFilterDao!=null){
            webFilterDao.close();
        }
        webFilterDao=new WebFilterDao(getActivity().getApplicationContext());
        groupVoList=webFilterDao.readGroupVoList();
        if(groupVoList!=null){
            blockCntsList.clear();
            blockUrlList.clear();
            cautionList.clear();
            trustList.clear();
            for(GroupVo group : groupVoList) {
                List<BlockVo> blockList = webFilterDao.readBlockVoList(group.id);
                if (blockList != null) {
                    for (BlockVo block : blockList) {
                        if(block.isBlocked()) {
                            if (group.type.equals("url")) {
                                block.value = SelfControlUtil.decode(block.value);
                                blockUrlList.add(block);
                                //     Log.d("showUrl",block.value);
                            } else if (group.type.equals("html")) {
                                block.value = SelfControlUtil.decode(block.value);
                                blockCntsList.add(block);
                                //       Log.d("showHtml", block.value);
                            } else if (group.type.equals("trust")) {
                                block.value = SelfControlUtil.decode(block.value);
                                trustList.add(block);
                                //         Log.d("showTrust", block.value);
                            } else if (group.type.equals("caution")) {
                                block.value = SelfControlUtil.decode(block.value);
                                cautionList.add(block);
                                //           Log.d("showCaution", block.value);
                            }
                        }
                    }
                }
            }
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        updateBlockData();
    }
    public interface OnDrawerButtonListener{
        public void onDrawerButtonListener();
    }
    public void setOnDrawerButtonListener(OnDrawerButtonListener obj){
        mOnDrawerButtonListener=obj;
    }
    public void onUrlChangedListener(OnUrlChanged obj){
        mOnUrlChanged=obj;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        updateBlockData();
        view= inflater.inflate(R.layout.content_internet, null);

        urlText=(EditText)view.findViewById(R.id.urlText);
        button=(ImageButton)view.findViewById(R.id.urlMoveButton);
        webView = (MyWebView) view.findViewById(R.id.webView);
        drawerButton=(ImageButton)view.findViewById(R.id.drawer_button);
        progressBar=(ProgressBar)view.findViewById(R.id.progressBar);
        drawerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(mOnDrawerButtonListener!=null) {
                    mOnDrawerButtonListener.onDrawerButtonListener();
                }
            }

        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goUrl(urlText.getText().toString());
            }
        });
        if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        mClient=new MyWebChromeClient();
        mContentView = (FrameLayout) view.findViewById(R.id.main_content);
        mTargetView = (FrameLayout)view.findViewById(R.id.target_view);
        webView.setWebChromeClient(mClient);
        webView.setWebViewClient(new MyWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(basicUrl);
        //webView.getSettings().setBlockNetworkImage(true);
        webView.getSettings().setDisplayZoomControls(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setAppCacheEnabled(false);
        userAgent=webView.getSettings().getUserAgentString();
        webView.clearCache(true);
        webView.addJavascriptInterface(new LoadListener(), "HTMLOUT");

        webView.setDownloadListener(new DownloadListener() {

            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(url));

                final String[] separated = url.split("/");
                final String myFile = separated[separated.length - 1];
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, myFile);
                DownloadManager dm = (DownloadManager) getActivity().getSystemService(getActivity().DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); //This is important!
                intent.addCategory(Intent.CATEGORY_OPENABLE); //CATEGORY.OPENABLE
                intent.setType("*/*");//any application,any extension
                Toast.makeText(getActivity().getApplicationContext(), "Downloading File", //To notify the Client that the file is being downloaded
                        Toast.LENGTH_LONG).show();

            }
        });

        urlText.setOnEditorActionListener(this);

        return view;
    }

    public String getTitle(){
        if(webView != null && webView.getTitle()!=null)
            return webView.getTitle().toString();
        return null;
    }
    public void goUrl(String url){
        String full=url;
        String location="";
        if (full.contains("://")){
            location=full;
        } else{
            location="http://"+full;
        }
        webView.loadUrl(location);
        setUrlText(location);
        basicUrl=location;
    }
    public void setUrlText(String url){
        if(!urlText.isFocused()) {
            urlText.setText(url);
        }
    }


    public boolean onEditorAction(TextView v,int actionId,KeyEvent event){
        if((actionId== EditorInfo.IME_ACTION_DONE) || (actionId== EditorInfo.IME_ACTION_NEXT) || event!=null && event.getKeyCode()== KeyEvent.KEYCODE_ENTER){
            goUrl(urlText.getText().toString());
        }
        return false;
    }

    public interface OnUrlChanged{
        void onUrlChanged(String url);
    }

    public class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg)
        {
            /*
            final WebView newWebView = new WebView(getApplicationContext());
            //    addView(newWebView);
            newWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    Intent intent = new Intent(getApplicationContext(), PopupActivity.class);
                    intent.putExtra("URL", url);
                    startActivity(intent);

                    newWebView.destroy();
                }
            });
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(newWebView);
            resultMsg.sendToTarget();*/
            return true;
        }
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {

            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mCustomViewCallback = callback;
            mTargetView.addView(view);
            mCustomView = view;
            mContentView.setVisibility(View.GONE);
            mTargetView.setVisibility(View.VISIBLE);
            mTargetView.bringToFront();
        }

        @Override
        public void onHideCustomView() {
            if (mCustomView == null)
                return;

            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            mCustomView.setVisibility(View.GONE);
            mTargetView.removeView(mCustomView);
            mCustomView = null;
            mTargetView.setVisibility(View.GONE);
            mCustomViewCallback.onCustomViewHidden();
            mContentView.setVisibility(View.VISIBLE);
        }
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result)
        {
            final JsResult finalRes = result;
            new AlertDialog.Builder(view.getContext())
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new AlertDialog.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finalRes.confirm();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }
    }
    public void remove(){
        getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .remove(this).commit();
    }
    private void removeFramesAndBlockIfBadContents(){
        if(!isTrustUrl(webView.getUrl())) {
            removeFrames();
            webView.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
        }
    }
    public void changeUrl(boolean check){
        if(check) {
            removeFramesAndBlockIfBadContents();
        }
        if(webView.getUrl()==null) return;

        if(!urlText.getText().toString().equals(webView.getUrl())){
            removeFramesAndBlockIfBadContents();
            setUrlText(webView.getUrl());
        }

        if(mOnUrlChanged != null) {
            mOnUrlChanged.onUrlChanged(webView.getUrl());
        }
    }
    public static String remainOnlyKoreanAndEnglish(String str){
        StringBuffer sb=new StringBuffer();
        for(int i=0 ; i<str.length(); i++){
            char c=str.charAt(i);
            if((c>='\uAC00' && c<='\uD7AF') || (c>='\u1100' && c<='\u11FF') || (c>='\u3130' && c<='\u318F')){
                sb.append(c);
            } else if((c>='a' && c<='z') || (c>='A' && c<='Z') || (c>='0' && c<='9') || (c=='=')){
                sb.append(c);
            }
        }
        return sb.toString();
    }
    public static boolean isHangulHanjaJapaness(String str)
    {
        for(int i = 0 ; i < str.length() ; i++)
        {
            char ch = str.charAt(i);
            Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(ch);
            if( //Character.UnicodeBlock.HANGUL_SYLLABLES.equals(unicodeBlock) ||
                //      Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO.equals(unicodeBlock) ||
                //        Character.UnicodeBlock.HANGUL_JAMO.equals(unicodeBlock) ||
                    Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(unicodeBlock) ||
                            Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(unicodeBlock) ||
                            Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B.equals(unicodeBlock) ||
                            Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(unicodeBlock) ||
                            Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT.equals(unicodeBlock) ||
                            Character.UnicodeBlock.HIRAGANA.equals(unicodeBlock) ||
                            Character.UnicodeBlock.KATAKANA.equals(unicodeBlock) ||
                            Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS.equals(unicodeBlock)
                    ) return true;
        }
        return false;
    }

    public String hasBadUrl(String url){
        if(url==null) return null;
        if(!url.startsWith("http")) return null;

        String test="";
        try {
            if(isHangulHanjaJapaness(java.net.URLDecoder.decode(url.toLowerCase(), "utf-8"))){
                return "hanjaJapanese";
            }
            test=remainOnlyKoreanAndEnglish(java.net.URLDecoder.decode(url.toLowerCase(), "utf-8"));
        }catch(Exception exc){}

        for (BlockVo blockUrl : blockUrlList) {
            if (test.contains(blockUrl.value)) {
                return blockUrl.value;
            }
        }
        return null;
    }

    public String hasBadHtml(String html){
        if(html==null) return null;

        String test="";
        test=html.toLowerCase();

        for(BlockVo blockCnts : blockCntsList) {
            if (test.contains(blockCnts.value)) {
                return blockCnts.value;
            }
        }
        return null;
    }

    public boolean isTrustUrl(String url){
        if(url==null) return false;
        try {
            String test = "";
            test = url.toLowerCase();
            test = test.split("/")[2];

            for (BlockVo trust : trustList) {
                if (test.contains(trust.value)) {
                    return true;
                }
            }
        } catch(Exception exc){return false;}
        return false;
    }
    String cautionUrl="";
    public boolean isCautionUrl(String url){
        if(url==null) return false;

        try {
            String test="";
            test=test.toLowerCase();
            test=url.split("/")[2];

            for(BlockVo caution : cautionList) {
                if (test.contains(caution.value)) {
                    cautionUrl=caution.value;
                    return true;
                }
            }
        } catch(Exception exc){return false;}
        return false;
    }

    public void htmlCheck(String html){
        if(hasBadHtml(html)!=null){
            toastShow("html blocking\n" + hasBadHtml(html));
            blocking();
        }
    }

    private void removeFrames(){
        webView.loadUrl("javascript:(function(){removeTag(\"frameset\");removeTag(\"iframe\");removeTag(\"frame\");function removeTag(tagName){var iframes=document.getElementsByTagName(tagName);for(var i=0 ; i<iframes.length ; i++){iframes[i].parentNode.removeChild(iframes[i]);};}})();");
    }

    public void blocking(){
        try {
            if (mCustomView != null) {
                mClient.onHideCustomView();
            }
            webView.loadUrl("javascript:(function(){" +
                    "document.getElementsByTagName('html')[0].innerHTML='blocked';" +
                    "})();");
            //webView.loadData("BLOCKED", "text/html", "UTF-8");
        }catch(Exception exc){
            exc.printStackTrace();
        }
    }
    public class MyWebViewClient extends WebViewClient {
        String previousUrl;
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            Log.d("url_override", url);
            if(url.startsWith("http") || url.startsWith("about") || url.startsWith("javascript")) {
                view.loadUrl(url);
                basicUrl = url;
            } else if (url.startsWith("intent://")) {
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    Intent existPackage = getActivity().getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                    if (existPackage != null) {
                        startActivity(intent);
                    } else {
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                        marketIntent.setData(Uri.parse("market://details?id="+intent.getPackage()));
                        startActivity(marketIntent);
                    }
                    return true;
                }catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (url.startsWith("market://")) {
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    if (intent != null) {
                        startActivity(intent);
                    }
                    return true;
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                boolean override = false;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, getActivity().getPackageName());
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Log.d("url","Not Fouund");
                }
                return override;
            }

            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d("on_page_start",url);
            progressBar.setVisibility(View.VISIBLE);
            changeUrl(false);
        }

        @Override
        public void onPageFinished(WebView view,String url){
            progressBar.setVisibility(View.GONE);
            Log.d("on_page_finish", url);
            if ("about:blank".equals(url) && previousUrl !=null)
            {
                view.loadUrl(previousUrl);
            }
            else if(url.startsWith("http")){
                previousUrl=url;
            }
            else
            {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, getActivity().getPackageName());
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Log.d("url","Not Fouund");
                }

            }
            changeUrl(true);
        }

        @Override
        public void onLoadResource(WebView view,String url) {
            changeUrl(false);
        }
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view,
                                                          String url) {
            if(isTrustUrl(url)){
                Log.d("shouldTrust",url);
                return null;
            }

            if(hasBadUrl(url)!=null){
                return new WebResourceResponse("text/html", "UTF-8",
                        new ByteArrayInputStream(url.getBytes()));
            }
            if(1==1) return null;

            String error=null;
            String htmlCode=null;
            String type="text/html";
            String encoding="utf-8";
            URL ur = null;
            HttpURLConnection conn = null;
            BufferedReader in=null;
            Map<String,String> headers=new HashMap<String,String>();
            int respCode=0;
            try {
                ur = new URL(url);
                if(url.startsWith("http://"))
                    conn = (HttpURLConnection)ur.openConnection();
                else
                    conn= (HttpsURLConnection)ur.openConnection();
                conn.setConnectTimeout(1000);
                conn.setReadTimeout(1000);
                conn.setDoInput(true);
                conn.setRequestProperty("User-Agent", userAgent);
                conn.addRequestProperty("Referer", "httpReferer");

                String cookieString = CookieManager.getInstance().getCookie(Uri.parse(url).toString());
                if (cookieString != null) {
                    conn.setRequestProperty("Cookie", cookieString);
                }
/*
                Map<String,List<String>> map=conn.getHeaderFields();
                for(Map.Entry<String,List<String>> entry:map.entrySet()) {
                    String key=entry.getKey();
                    List<String> value=entry.getValue();
                    for(String str:value){
                        Log.d("shouldMap",key+","+str);
                        headers.put(key,str);
                    }
                }
*/
                type=conn.getContentType();
                encoding=conn.getContentEncoding();
                respCode = conn.getResponseCode();
                String inputLine;

                Log.d("shouldCheck",url+","+type+","+respCode);
                if(type.contains("html") || type.contains("text") || type.contains("script") ) {
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder content=new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                        //              Log.d("shouldHtml", inputLine);

                    }
                    //            Log.d("shouldContent",content.toString());
                    if (hasBadHtml(content.toString()) != null) {
                        error = hasBadHtml(content.toString());
                    } else{
                        htmlCode=content.toString();
                    }
                }
            } catch (Exception exc) {

                exc.printStackTrace();
                try {
                    in=new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    String inputLine;
                    htmlCode="";

                    while ((inputLine = in.readLine()) != null) {
                        //               Log.d("shouldError", inputLine);
                        htmlCode+=inputLine;
                    }
                }catch(Exception exc2){}

            }   finally {
                try {
                    if(in!=null) {
                        in.close();
                    }
                }catch(Exception exc){exc.printStackTrace();}
                if(conn!=null)
                    conn.disconnect();
            }
            if( respCode >= 300 && respCode < 400 ) {
                // redirect
                return null;
            }
            if(hasBadUrl(url)!=null){
                error=hasBadUrl(url);
            }
            if (error!=null){
                Log.d("shouldCheckBlocked",url);
                if(type!=null) {
                    return new WebResourceResponse(type.split(";")[0], encoding,
                            new ByteArrayInputStream(error.getBytes()));
                }else {
                    return new WebResourceResponse("text/html", "utf-8",
                            new ByteArrayInputStream(error.getBytes()));
                }
            }
            if (htmlCode!=null){
                Log.d("shouldCheckHtml",url);
                if(type==null) type="text/html;aa";
                return new WebResourceResponse(type.split(";")[0], encoding,
                        new ByteArrayInputStream(htmlCode.getBytes()));
            }

            return  null;
        }

    }

    class LoadListener{
        @JavascriptInterface
        public void processHTML(String html)
        {
            System.out.println(html);
            htmlCheck(html);
        }
    }

    public void messageBox(String title,String msg){
        new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        // Some stuff to do when ok got clicked
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        // Some stuff to do when cancel got clicked
                    }
                })
                .show();
    }
    public void toastShow(String str){
        Toast.makeText(getActivity().getApplicationContext(), str,
                Toast.LENGTH_SHORT).show();
    }

}

