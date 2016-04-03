package kr.selfcontrol.selfwebfilter.vo;

import kr.selfcontrol.selfwebfilter.util.SelfControlUtil;

/**
 * Created by owner2 on 2015-12-28.
 */
public class BlockVo {
    public int groupId;
    public String key;
    public String value;
    public long dateAffect;
    public long dateUnlock;

    public BlockVo(){

    }

    public BlockVo(int groupId,String key,long dateAffect, long dateUnlock){
        this.groupId=groupId;
        this.key= SelfControlUtil.md5(key);
        this.value= SelfControlUtil.encode(key);
        this.dateUnlock=dateUnlock;
        this.dateAffect=dateAffect;
    }
    public BlockVo(int groupId,String key,String value,long dateAffect,long dateUnlock){
        this.groupId=groupId;
        this.key=key;
        this.value=value;
        this.dateUnlock=dateUnlock;
        this.dateAffect=dateAffect;
    }
    public boolean isAffecting(){
        if(dateAffect>System.currentTimeMillis()) return true;
        return false;
    }
    public boolean isBlocked(){
        if(isAffecting()) return false;
        if(dateUnlock==0 || dateUnlock>System.currentTimeMillis()) {
            return true;
        }
        return false;
    }
    public boolean isUnlocking(){
        if(dateUnlock>System.currentTimeMillis()){
            return true;
        }
        return false;
    }
}
/*
db.execSQL("CREATE TABLE block_list(" +
        "group_id integer not null"+
        ",key varchar(50) not null" +
        ",value varchar(200)" +
        ",delay long" +
        ",date_unlock long" +*/