package kr.selfcontrol.selfwebfilter.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kr.selfcontrol.selfwebfilter.R;
import kr.selfcontrol.selfwebfilter.dao.WebFilterDao;
import kr.selfcontrol.selfwebfilter.picker.HorizontalPicker;
import kr.selfcontrol.selfwebfilter.vo.GroupVo;

public class SettingActivity extends AppCompatActivity {

    boolean needTimer;
    private Timer timer;
    TimerTask timerTask=new TimerTask() {
        @Override
        public void run() {

            if(!needTimer){
                timer.cancel();
                timer.purge();
                timer = null;
            }
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateKV();
                    }
                });
            } catch (Exception exc) {
                timer.cancel();
                timer.purge();
                timer = null;
            }
        }
    };
    WebFilterDao webFilterDao;
    public List<GroupVo> groupVoList;
    private ListView listView;
    BaseAdapter adapter;

    HashMap<String,String> kVExplain=new HashMap<String,String>();

    public void updateKV(){
        if(webFilterDao==null) {
            webFilterDao = new WebFilterDao(this);
        }


        groupVoList=webFilterDao.readGroupVoList();

        needTimer=false;
        for(GroupVo groupVo : groupVoList){
            if(groupVo.isBlocked() && groupVo.isUnlocking())
                needTimer=true;
        }
        if(adapter!=null) {
            adapter.notifyDataSetChanged();
        }
       // database.printAll();

        if(needTimer && timer==null) {
            timer = new Timer();
            timer.schedule(timerTask, 0, 1000);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.reload) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_setting);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        updateKV();

        listView = (ListView)findViewById(R.id.settingList);
        adapter = new SettingListAdapter(getLayoutInflater(), groupVoList);
        listView.setAdapter(adapter);

    }

    public void showDialog(GroupVo _groupVo){
        final GroupVo groupVo=_groupVo;

        final String[] options=new String[]{"none","30seconds", "1minute", "5minutes", "10minutes", "30minutes", "1hour", "2hours", "4hours", "6hours","9hours", "12hours", "1day", "2days", "5days"};
        final long[] optionValues=new long[]{0,30000,60000,5*60000,10*60000,30*60000,60*60000,2*60*60000,4*60*60000,6*60*60000,9*60*60000,12*60*60000,24*60*60000,2*24*60*60000,5*24*60*60000};
        final View innerView = getLayoutInflater().inflate(R.layout.swfilter_setting_time_dialog, null);
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle(groupVo.name);
        ab.setView(innerView);
        final HorizontalPicker hpValue=(HorizontalPicker) innerView.findViewById(R.id.value);
        hpValue.setValues(options);
        long valueTime=groupVo.lockTime;
        if(groupVo.type.equals("trust")) valueTime=groupVo.affectTime;
        for(int i=0 ; i<optionValues.length ; i++){
            if(optionValues[i]==valueTime) {
                hpValue.setSelectedItem(i);
            }
        }
        final HorizontalPicker hpDelay=(HorizontalPicker) innerView.findViewById(R.id.delay);
        for(int i=0 ; i<optionValues.length ; i++){
            if(optionValues[i]==groupVo.delay) {
                hpDelay.setSelectedItem(i);
            }
        }
        hpDelay.setValues(options);

//time to delay setting permission

        ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                if(groupVo.type.equals("trust"))
                    groupVo.affectTime=optionValues[hpValue.getSelectedItem()];
                else
                    groupVo.lockTime=optionValues[hpValue.getSelectedItem()];
                groupVo.delay=optionValues[hpDelay.getSelectedItem()];
                webFilterDao.insertGroupVo(groupVo);
                updateKV();
            }
        });

        ab.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.cancel();
            }
        });

        ab.create().show();
    }
    public String getTimeToString(long time){
        time=time/1000;

        StringBuilder sb=new StringBuilder();

        if(time==0) sb.append("none");

        if((int)(time/3600/24)>0){
            sb.append((int)time/3600/24+"days ");
        }
        time=time%(3600*24);
        if((int)(time/3600)>0){
            sb.append((int)time/3600+"hours ");
        }

        time=time%(3600);
        if((int)(time/60)>0){
            sb.append((int)time/60+"minitues ");
        }

        time=time%(60);
        if((int)(time)>0){
            sb.append((int)time+"seconds ");
        }
        return sb.toString();
    }
    public class SettingListAdapter extends BaseAdapter
    {
        private LayoutInflater inflater = null;
        List<GroupVo> groupVoList;

        public SettingListAdapter(LayoutInflater inflater, List<GroupVo> groupVoList){
            this.groupVoList=groupVoList;
            this.inflater=inflater;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){

                viewHolder=new ViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.swfilter_setting_time_item, null);
                     convertView.setTag(viewHolder);

                viewHolder.keyView=(TextView)convertView.findViewById(R.id.key);
                viewHolder.valueView=(TextView)convertView.findViewById(R.id.value);
                viewHolder.delayView=(TextView)convertView.findViewById(R.id.delay);
                viewHolder.explainView=(TextView)convertView.findViewById(R.id.explain);
                viewHolder.blockButton=(Button)convertView.findViewById(R.id.lock_button);
                viewHolder.editButton=(Button)convertView.findViewById(R.id.edit_button);
                viewHolder.editTimeButton=(Button)convertView.findViewById(R.id.time_edit_button);
                viewHolder.blockButton.setOnClickListener(buttonClickListener);
                viewHolder.editButton.setOnClickListener(buttonClickListener);
                viewHolder.editTimeButton.setOnClickListener(buttonClickListener);
                viewHolder.blockButton.setTag(viewHolder);
                viewHolder.editTimeButton.setTag(viewHolder);
                viewHolder.editButton.setTag(viewHolder);

            }
            viewHolder=(ViewHolder)convertView.getTag();
            GroupVo groupVo=getItem(position);
            viewHolder.groupVo=groupVo;
            viewHolder.explainView.setText(groupVo.name);
            viewHolder.keyView.setText(groupVo.name);

            if(!groupVo.type.equals("trust"))
                viewHolder.valueView.setText("Lock Time : " +getTimeToString(groupVo.lockTime));
            else
                viewHolder.valueView.setText("Affect Time : " +getTimeToString(groupVo.affectTime));

            viewHolder.delayView.setText("Delay : " + getTimeToString(groupVo.delay));

            if(!groupVo.isBlocked()) {
                viewHolder.editTimeButton.setEnabled(true);
                viewHolder.blockButton.setText("Lock Time Edit");
                convertView.setBackgroundColor(0xFFABABFF);
            } else if(!groupVo.isUnlocking()){
                viewHolder.editTimeButton.setEnabled(false);
                viewHolder.blockButton.setText("Unlock Time Edit");
                convertView.setBackgroundColor(0xFFFFABAB);
            } else {
                viewHolder.editTimeButton.setEnabled(false);
                convertView.setBackgroundColor(0xFFABFFAB);
                viewHolder.blockButton.setText("Cancel");
                viewHolder.delayView.setText(getTimeToString((groupVo.dateUnlock - System.currentTimeMillis())) + "초 남음");
            }
            return convertView;
        }
        @Override
        public int getCount() {
            return groupVoList.size();
        }

        @Override
        public GroupVo getItem(int position) {
            return groupVoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private View.OnClickListener buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId()==R.id.lock_button) {
                    ViewHolder viewHolder = (ViewHolder) v.getTag();
                    GroupVo groupVo = viewHolder.groupVo;
                    if (!groupVo.isBlocked()) {
                        groupVo.dateUnlock=0;
                    } else if (groupVo.isUnlocking()) {
                        groupVo.dateUnlock=0;
                    } else {
                        groupVo.dateUnlock=System.currentTimeMillis()+groupVo.delay;
                    }
                    webFilterDao.insertGroupVo(groupVo);
                    updateKV();
                } else if(v.getId()==R.id.time_edit_button){
                    ViewHolder viewHolder = (ViewHolder) v.getTag();
                    showDialog(viewHolder.groupVo);
                } else if(v.getId()==R.id.edit_button){
                    ViewHolder viewHolder=(ViewHolder) v.getTag();
                    GroupVo groupVo=viewHolder.groupVo;
                    Intent intent=new Intent(getApplicationContext(),BlockSettingActivity.class);
                    intent.putExtra("group",groupVo.id);
                    startActivity(intent);
                }
            }
        };
        class ViewHolder{
            public GroupVo groupVo;
            public ViewGroup outLayout;
            public TextView keyView;
            public TextView delayView;
            public TextView valueView;
            public TextView explainView;
            public Button editButton;
            public Button editTimeButton;
            public Button blockButton;
        }
    };
}
