package kr.selfcontrol.selfwebfilter.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kr.selfcontrol.selfwebfilter.R;
import kr.selfcontrol.selfwebfilter.dao.WebFilterDao;
import kr.selfcontrol.selfwebfilter.util.SelfControlUtil;
import kr.selfcontrol.selfwebfilter.vo.BlockVo;
import kr.selfcontrol.selfwebfilter.vo.GroupVo;

public class BlockSettingActivity extends AppCompatActivity {

    int groupId;
    GroupVo groupVo;
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
                        if (passed) {
                            update();
                        }
                    }
                });
            } catch (Exception exc) {
                timer.cancel();
                timer.purge();
                timer = null;
            }
        }
    };
    List<BlockVo> urlBlockList=new ArrayList<BlockVo>();
    private ListView list;
    BaseAdapter adapter;
    EditText text;
    Button blockButton;
    boolean needTimer=false;
    String baseDbAdress;
    boolean passed=false;
    WebFilterDao webFilterDao;
    HashMap<String,String> encodeMap=new HashMap<>();

    void update(){
//        database.database.beginTransaction();
        List<BlockVo> urlList= webFilterDao.readBlockVoList(groupVo.id);
        for(BlockVo url:urlList){
            if(!url.isBlocked() && !url.isAffecting()) {
                webFilterDao.deleteBlockVo(url.groupId, url.key);
            }
        }
        encodeMap.clear();
        needTimer=false;

        urlList= webFilterDao.readBlockVoList(groupVo.id);
        urlBlockList.clear();
        for(BlockVo url:urlList){
            encodeMap.put(url.key,SelfControlUtil.decode(url.value));
            urlBlockList.add(url);
            if(url.isBlocked() && url.isUnlocking() || url.isAffecting()){
                needTimer=true;
            }
        }
        //database.database.setTransactionSuccessful();
        //database.database.endTransaction();
        adapter.notifyDataSetChanged();

        try {
            if (needTimer && timer == null) {
                timer = new Timer();
                timer.schedule(timerTask, 0, 1000);
            }
        } catch(Exception exc){}
    }

    public void showDialog(){
        final View innerView = getLayoutInflater().inflate(R.layout.swfilter_setting_password, null);
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle("password 입력해.");
        ab.setView(innerView);
        final EditText editText=(EditText)innerView.findViewById(R.id.password);

        ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                if (!groupVo.password.isEmpty()) {
                    if (editText.getText().toString().equals(groupVo.password.trim())) {
                        passed = true;
                        blockButton.setEnabled(true);
                        update();
                    } else {
                        finish();
                    }
                }
            }
        });

        ab.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
                arg0.cancel();
            }
        });

        ab.create().show();
    }

    private View.OnClickListener upButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            update();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_setting);



            webFilterDao=new WebFilterDao(this);

        groupId=getIntent().getExtras().getInt("group");
        groupVo=webFilterDao.readGroupVo(groupId);

        list = (ListView)findViewById(R.id.list_view);
        text=(EditText)findViewById(R.id.text);
        ////////////////up Area/////////////////////////

        TextView keyView, valueView,delayView,explainView;
        Button lockButton, editButton,editTimeButton;

        keyView=(TextView)findViewById(R.id.key);
        valueView=(TextView)findViewById(R.id.value);
        delayView=(TextView)findViewById(R.id.delay);
        explainView=(TextView)findViewById(R.id.explain);
        lockButton=(Button)findViewById(R.id.lock_button);
        editButton=(Button)findViewById(R.id.edit_button);
        editTimeButton=(Button)findViewById(R.id.time_edit_button);
        lockButton.setOnClickListener(upButtonClickListener);
        editButton.setOnClickListener(upButtonClickListener);
        editTimeButton.setOnClickListener(upButtonClickListener);
        ////////////////////////////////////////////////
        blockButton=(Button)findViewById(R.id.block_button);

        adapter = new GroupListAdapter(getLayoutInflater(), urlBlockList);
        list.setAdapter(adapter);
        if(!passed) {
            if(!groupVo.password.isEmpty()) {
                blockButton.setEnabled(false);
                showDialog();
            } else{
                passed=true;
                update();
            }
        } else{
            blockButton.setEnabled(true);
        }
        blockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!text.getText().toString().isEmpty()) {
                    webFilterDao.insertBlockVo(new BlockVo(groupVo.id, SelfControlUtil.md5(text.getText().toString()),SelfControlUtil.encode(text.getText().toString()),System.currentTimeMillis()+groupVo.affectTime,0));

                    if (passed) {
                        update();
                    }
                    text.setText("");
                }
            }
        });

    }

    public String getTimeToString(long time){
        time=time/1000;
        StringBuilder sb=new StringBuilder();
        if((int)(time/3600/24)>0){
            sb.append((int)time/3600/24+"days ");
        }
        time=time%(3600*24);
        if((int)(time/3600)>0){
            sb.append((int)time/3600+"hours ");
        }

        time=time%(3600);
        if((int)(time/60)>0){
            sb.append((int) time / 60 + "minitues ");
        }

        time=time%(60);
        if((int)(time)>0){
            sb.append((int) time + "seconds ");
        }
        return sb.toString();
    }
    public class GroupListAdapter extends BaseAdapter
    {
        private LayoutInflater inflater = null;
        List<BlockVo> blockList;

        public GroupListAdapter(LayoutInflater inflater, List<BlockVo> blockList){
            this.blockList=blockList;
            this.inflater=inflater;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){
                viewHolder=new ViewHolder();
                convertView = inflater.inflate(R.layout.internet_setting_urlblock_item, null);
                convertView.setTag(viewHolder);

                viewHolder.stringView=(TextView)convertView.findViewById(R.id.string);
                viewHolder.blockUrl=getItem(position);
                viewHolder.blockButton=(Button)convertView.findViewById(R.id.block_button);
                viewHolder.blockButton.setOnClickListener(buttonClickListener);
                viewHolder.blockButton.setTag(viewHolder);

            }
            viewHolder=(ViewHolder)convertView.getTag();
            BlockVo blockUrl=getItem(position);
            viewHolder.blockUrl=blockUrl;
            viewHolder.stringView.setText(encodeMap.get(blockUrl.key));
            viewHolder.isBlocked=blockUrl.isBlocked();
            viewHolder.isUnlocking=blockUrl.isUnlocking();
            if(blockUrl.isAffecting()) {
                viewHolder.blockButton.setText("Cancel");
                convertView.setBackgroundColor(0xFFABFFFF);
                viewHolder.stringView.setText(encodeMap.get(blockUrl.key) + "\n" + getTimeToString((blockUrl.dateAffect-System.currentTimeMillis() )) + " 이후 적용");

            } else if(!blockUrl.isBlocked()) {
                viewHolder.stringView.setText(encodeMap.get(blockUrl.key));
                viewHolder.blockButton.setText("Block");
                convertView.setBackgroundColor(0xFFABABFF);
            } else if(!blockUrl.isUnlocking()){
                viewHolder.stringView.setText(encodeMap.get(blockUrl.key));
                viewHolder.blockButton.setText("UnBlock");
                convertView.setBackgroundColor(0xFFFFABAB);
            } else {
                convertView.setBackgroundColor(0xFFABFFAB);
                viewHolder.blockButton.setText("Cancel");
                viewHolder.stringView.setText(encodeMap.get(blockUrl.key)+"\n"+getTimeToString((blockUrl.dateUnlock - System.currentTimeMillis())) + " 남음");
            }
            return convertView;
        }
        @Override
        public int getCount() {
            return blockList.size();
        }

        @Override
        public BlockVo getItem(int position) {
            return blockList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private View.OnClickListener buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder viewHolder=(ViewHolder) v.getTag();
                BlockVo url=viewHolder.blockUrl;
                if(url.isAffecting()) {
                    url.dateUnlock=System.currentTimeMillis();
                    url.dateAffect=System.currentTimeMillis();
                }else if (!url.isBlocked()) {
                    url.dateUnlock=0;
                } else if (url.isUnlocking()) {
                    url.dateUnlock=0;
                } else {
                    url.dateUnlock=System.currentTimeMillis()+groupVo.lockTime;
                }
                webFilterDao.insertBlockVo(url);
                update();
            }
        };
        class ViewHolder{
            public TextView stringView;
            public Button blockButton;
            public BlockVo blockUrl;
            public boolean isBlocked;
            public boolean isUnlocking;
        }
    };

}
