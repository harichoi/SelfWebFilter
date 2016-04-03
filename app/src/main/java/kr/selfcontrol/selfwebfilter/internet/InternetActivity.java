package kr.selfcontrol.selfwebfilter.internet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.selfcontrol.selfwebfilter.R;
import kr.selfcontrol.selfwebfilter.setting.SettingActivity;

public class InternetActivity extends AppCompatActivity implements InternetFragment.OnUrlChanged, TabAdapter.OnTabAdapterListener, InternetFragment.OnDrawerButtonListener{
    TabAdapter adapter;

    Toolbar toolbar;
    LinearLayout llDrawer;
    DrawerLayout dlDrawer;
    ActionBarDrawerToggle dtToggle;
    ListView drawerListView;
    InternetFragment fragment;
    Button addTabButton;
    Button exitButton;
    Button settingButton;
    List<InternetFragment> fragmentList=new ArrayList<InternetFragment>();
    HashMap<InternetFragment, String> urlMap=new HashMap<>();


    @Override
    public void onDrawerButtonListener(){
        dlDrawer.openDrawer(llDrawer);
    }
    @Override
    public void onClickTabListener(InternetFragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container,fragment)
                .commit();

        dlDrawer.closeDrawer(llDrawer);
    }
    @Override
    public void onDeleteTabListener(InternetFragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .remove(fragment)
                .commit();
        fragmentList.remove(fragment);
        urlMap.remove(fragment);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
     //   messageBox("1","1");
        super.onCreate(savedInstanceState);

        //this.deleteDatabase("webview.db");
        //this.deleteDatabase("webviewCookiesChromium.db");
        //this.deleteDatabase("webviewCookiesChromiumPrivate.db");

        setContentView(R.layout.activity_internet);

     //   messageBox("1","1");

        addTabButton=(Button) findViewById(R.id.add_tab_button);
        exitButton=(Button) findViewById(R.id.exit_button);
        settingButton=(Button)findViewById(R.id.setting_button);
        dlDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        llDrawer = (LinearLayout) findViewById(R.id.linear_drawer_layout);

        addTabButton.setText("New Window");
        exitButton.setText("Exit");
        settingButton.setText("Setting");

        addTabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Log.d("hi", "hhh");
                ViewGroup viewGroup=(ViewGroup)findViewById(R.id.fragment_container);
                viewGroup.removeAllViewsInLayout();
                Log.d("hi", "hhh2");*/
                dlDrawer.closeDrawer(llDrawer);
                fragment = new InternetFragment();
                fragment.onUrlChangedListener(InternetActivity.this);
                fragment.setOnDrawerButtonListener(InternetActivity.this);
                fragmentList.add(fragment);
                adapter.notifyDataSetChanged();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
        });
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),SettingActivity.class);
                startActivity(intent);
            }
        });

        dtToggle = new ActionBarDrawerToggle(this, dlDrawer, R.string.app_name, R.string.app_name);
        dlDrawer.setDrawerListener(dtToggle);

        drawerListView=(ListView)findViewById(R.id.drawer);


        adapter=new TabAdapter(getApplicationContext(),fragmentList);
        adapter.setListener(this);

        fragment=new InternetFragment();
        if(urlMap.get(fragment)!=null) {
            fragment.basicUrl=urlMap.get(fragment);
        }
        try {
            Intent intent = getIntent();
            Uri data = intent.getData();
            fragment.basicUrl = data.getPath();
        }catch(Exception exc){}
        fragment.onUrlChangedListener(this);
        fragment.setOnDrawerButtonListener(this);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container,fragment)
                .commit();

        fragmentList.add(fragment);

        drawerListView.setAdapter(adapter);
    }

    @Override
    public void onUrlChanged(String url){
        urlMap.put(fragment,url);
        adapter.notifyDataSetChanged();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.isTracking()
                && !event.isCanceled()) {
            // *** Your Code ***
            try {
                if (fragment.mCustomView != null){
                    fragment.mClient.onHideCustomView();
                } else {
                    WebBackForwardList mWebBackForwardList = fragment.webView.copyBackForwardList();
                    if (mWebBackForwardList.getCurrentIndex() > 0) {
                        for (int i = 1; i <= mWebBackForwardList.getCurrentIndex(); i++) {
                            String historyUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex() - i).getUrl();
                            if (historyUrl.startsWith("http")) {
                                fragment.webView.goBackOrForward(-i);
                                break;
                            }
                        }
                    }
                }

            } catch(Exception exc){}
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_internet, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

// Sync the toggle state after onRestoreInstanceState has occurred.
        dtToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        dtToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.exit:
                this.finish();
                break;
            case R.id.setting:
                Intent intent=new Intent(getApplicationContext(),SettingActivity.class);
                startActivity(intent);
                break;
        }
        if (dtToggle.onOptionsItemSelected(item)) {
            Log.d("urlurl",item.getTitle().toString());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void titleChanged(InternetFragment fragment){
        fragment.getTitle();
    }

    public void messageBox(String title,String msg){
        new AlertDialog.Builder(this)
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
        Toast.makeText(getApplicationContext(), str,
                Toast.LENGTH_SHORT).show();
    }
}
