package kr.selfcontrol.selfwebfilter.vo;

/**
 * Created by owner2 on 2015-12-28.
 */
public class GroupVo {
    public int id;
    public String name;
    public long delay;
    public long dateUnlock;
    public long lockTime;
    public long affectTime;
    public String type;
    public String password;

    public GroupVo(){
    }
    public GroupVo(int id,String name,long delay,long lockTime,long affectTime, long dateUnlock,String type,String password){
        this.id=id;
        this.name=name;
        this.delay=delay;
        this.dateUnlock=dateUnlock;
        this.type=type;
        this.password=password;
        this.lockTime=lockTime;
        this.affectTime=affectTime;
    }

    public boolean isBlocked(){
        if(dateUnlock==0 || dateUnlock>System.currentTimeMillis()) {
            return true;
        }
        return false;
    }
    public boolean isUnlocking(){
        if(dateUnlock!=0){
            return true;
        }
        return false;
    }
}
/*
db.execSQL("CREATE TABLE group_list(" +
        "id integer not null"+
        ",name varchar(50) not null" +
        ",delay long" +
        ",date_unlock long" +
        ",primary key(id))");
*/