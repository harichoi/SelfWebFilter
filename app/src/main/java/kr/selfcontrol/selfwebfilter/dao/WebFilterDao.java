package kr.selfcontrol.selfwebfilter.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.selfcontrol.selfwebfilter.util.SelfControlUtil;
import kr.selfcontrol.selfwebfilter.vo.BlockVo;
import kr.selfcontrol.selfwebfilter.vo.GroupVo;

/**
 * Created by owner2 on 2015-12-28.
 */
public class WebFilterDao  extends SQLiteOpenHelper {

    public SQLiteDatabase database;

    public WebFilterDao(Context context){
        super(context, "selfcontrol.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        database=db;
        db.execSQL("CREATE TABLE block_list(" +
                "group_id integer not null" +
                ",key varchar(50) not null" +
                ",value varchar(200)" +
                ",date_unlock long" +
                ",date_affect long" +
                ",primary key(group_id,key))");
        db.execSQL("CREATE TABLE group_list(" +
                "id integer not null" +
                ",name varchar(50) not null" +
                ",delay long" +
                ",date_unlock long" +
                ",lock_time long" +
                ",affect_time long" +
                ",password varchar(50)" +
                ",type varchar(10)" +
                ",primary key(id))");
        //db.execSQL("CREATE INDEX idx1 on block_list(key)");
        insertGroupVo(new GroupVo(4, "publicUrl", 90000, 90000,5000, 1, "url", ""));
        insertGroupVo(new GroupVo(5, "publicHtml", 90000, 90000,5000, 1, "html", ""));
        insertGroupVo(new GroupVo(6, "publicCaution", 90000, 90000,5000, 1, "caution", ""));
        insertGroupVo(new GroupVo(7, "publicTrust", 90000, 5000,90000, 1, "trust", ""));

    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){

    }

    public List<BlockVo> readBlockVoList(int groupId){
        Log.d("readBlockVoList",groupId+"");

        List<BlockVo> blockVoList=new ArrayList<>();
        List<HashMap<String,String>> listMap = readSql("select * from block_list where group_id=?",String.valueOf(groupId));
        for(HashMap<String,String> map : listMap){
            BlockVo blockVo=new BlockVo();
            blockVo.groupId=Integer.parseInt(map.get("group_id"));
            blockVo.dateUnlock=Long.parseLong(map.get("date_unlock"));
            blockVo.dateAffect=Long.parseLong(map.get("date_affect"));
            blockVo.key=map.get("key");
            blockVo.value=map.get("value");
            blockVoList.add(blockVo);
        }
        return blockVoList;
    }

    public BlockVo readBlockVo(int groupId,String key){
        Log.d("readBlockVo",groupId+","+key);

        List<HashMap<String,String>> listMap = readSql("select * from block_list where group_id=? and key=?", String.valueOf(groupId),key);
        for(HashMap<String,String> map : listMap){
            BlockVo blockVo=new BlockVo();
            blockVo.groupId=Integer.parseInt(map.get("group_id"));
            blockVo.dateUnlock=Long.parseLong(map.get("date_unlock"));
            blockVo.dateAffect=Long.parseLong(map.get("date_affect"));
            blockVo.key=map.get("key");
            blockVo.value=map.get("value");
            return blockVo;
        }
        return null;
    }
    public void insertBlockVo(BlockVo blockVo){
        Log.d("insertBlockVo",blockVo.key);
        BlockVo temp=readBlockVo(blockVo.groupId,blockVo.key);
        if(temp==null) {
            Log.d("insertBlockVo","inserted");
            writeSql("insert into block_list(group_id,key,value,date_unlock,date_affect) values (?,?,?,?,?)", String.valueOf(blockVo.groupId), blockVo.key, blockVo.value, String.valueOf(blockVo.dateUnlock),String.valueOf(blockVo.dateAffect));
        } else {
            Log.d("insertBlockVo","updated");
            writeSql("update block_list set value=?,date_unlock=?,date_affect=? where group_id=? and key=?",blockVo.value,String.valueOf(blockVo.dateUnlock),String.valueOf(blockVo.dateAffect),String.valueOf(blockVo.groupId),blockVo.key);
        }
    }
    public void insertGroupVo(GroupVo groupVo){
        Log.d("insertGroupVo",groupVo.name);
        GroupVo temp=readGroupVo(groupVo.id);
        Log.d("hhaha",String.valueOf(groupVo.id)+","+groupVo.name+","+String.valueOf(groupVo.delay)+","+String.valueOf(groupVo.dateUnlock)+","+String.valueOf(groupVo.lockTime)+","+groupVo.password);
        if(temp==null) {
            writeSql("insert into group_list(id,name,delay,date_unlock,affect_time,lock_time,type,password) values (?,?,?,?,?,?,?,?)", String.valueOf(groupVo.id), groupVo.name, String.valueOf(groupVo.delay), String.valueOf(groupVo.dateUnlock), String.valueOf(groupVo.affectTime), String.valueOf(groupVo.lockTime), groupVo.type,groupVo.password);
        } else {
            writeSql("update group_list set name=?,delay=?,date_unlock=?,lock_time=?,affect_time=?,type=?,password=? where id=?", groupVo.name, String.valueOf(groupVo.delay), String.valueOf(groupVo.dateUnlock), String.valueOf(groupVo.lockTime),String.valueOf(groupVo.affectTime), groupVo.type,groupVo.password,String.valueOf(groupVo.id));
        }
    }
    public void deleteBlockVo(int groupId,String key){
        Log.d("deleteBlockVo",key);
        writeSql("delete from block_list where group_id=? and key=?",String.valueOf(groupId),key);
    }
    public List<BlockVo> readBlockVoList(){

        List<BlockVo> BlockVoList=new ArrayList<>();
        List<HashMap<String,String>> listMap = readSql("select * from block_list");
        for(HashMap<String,String> map : listMap){
            BlockVo blockVo=new BlockVo();
            blockVo.groupId=Integer.parseInt(map.get("group_id"));
            blockVo.dateUnlock=Long.parseLong(map.get("date_unlock"));
            blockVo.dateAffect=Long.parseLong(map.get("date_affect"));
            blockVo.key=map.get("key");
            blockVo.value=map.get("value");
            Log.d("key,value",blockVo.key+","+blockVo.value);
            BlockVoList.add(blockVo);
        }
        return BlockVoList;
    }
    public List<GroupVo> readGroupVoList(){

        List<GroupVo> groupVoList=new ArrayList<>();
        List<HashMap<String,String>> listMap = readSql("select * from group_list");
        for(HashMap<String,String> map : listMap){
            GroupVo groupVo=new GroupVo();
            groupVo.id=Integer.parseInt(map.get("id"));
            groupVo.delay=Long.parseLong(map.get("delay"));
            groupVo.name=map.get("name");
            groupVo.dateUnlock=Long.parseLong(map.get("date_unlock"));
            groupVo.type=map.get("type");
            groupVo.password=map.get("password");
            groupVo.lockTime=Long.parseLong(map.get("lock_time"));
            groupVo.affectTime=Long.parseLong(map.get("affect_time"));
            groupVoList.add(groupVo);
        }
        return groupVoList;
    }
    public GroupVo readGroupVo(int groupId){
        List<HashMap<String,String>> listMap = readSql("select * from group_list where id=?",String.valueOf(groupId));
        for(HashMap<String,String> map : listMap){
            GroupVo groupVo=new GroupVo();
            groupVo.id=Integer.parseInt(map.get("id"));
            groupVo.delay=Long.parseLong(map.get("delay"));
            groupVo.name=map.get("name");
            groupVo.dateUnlock=Long.parseLong(map.get("date_unlock"));
            groupVo.name=map.get("value");
            groupVo.type=map.get("type");
            groupVo.password=map.get("password");
            groupVo.lockTime=Long.parseLong(map.get("lock_time"));
            groupVo.affectTime=Long.parseLong(map.get("affect_time"));
            return groupVo;
        }
        return null;
    }
    private boolean writeSql(String str,String ...args){
        if(database==null) {
            try {
                database = getWritableDatabase();
            } catch (Exception exc) {
                database.close();
                database = getWritableDatabase();
            }
        }
        // database=getWritableDatabase();
        database.execSQL(str, args);
        // database.close();
        return true;
    }
    private List<HashMap<String,String>> readSql(String str,String ...args){

        if(database==null) {
            try {
                database = getWritableDatabase();
            } catch (Exception exc) {
                exc.printStackTrace();
                database.close();
                database = getWritableDatabase();
            }
        }
        Cursor cursor=database.rawQuery(str, args);
        List<HashMap<String,String>> result=new ArrayList<HashMap<String,String>>();
        while(cursor.moveToNext()) {
            HashMap<String,String> row=new HashMap<String,String>();
            for(int i=0 ; i<cursor.getColumnCount() ; i++){
                row.put(cursor.getColumnName(i),cursor.getString(i));
            }
            result.add(row);
        }
        cursor.close();
        //       database.close();
        return result;
    }
}
