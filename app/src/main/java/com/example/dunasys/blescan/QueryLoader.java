package com.example.dunasys.blescan;

import java.util.LinkedHashMap;

public class QueryLoader {

    LinkedHashMap<String,byte[]> query_db = new LinkedHashMap<String, byte[]>();
    LinkedHashMap<String,String> query_db_msg = new LinkedHashMap<String, String>();
     byte[] test = null;

    public QueryLoader() {

        add_Query();
        add_Query_msg();

    }

    public void add_Query(){
        query_db.put("SELECT THE QUERY TO WRITE",test);
        query_db.put("Lecture Defauts CMM",new byte[]{0x00,0x04,0x10,0x19,0x02,(byte)0xFF});
        query_db.put("Lecture Defauts OBD",new byte[]{0x00,0x02,0x10,0x03});
        query_db.put("Effacement defauts",new byte[]{0x00,0x05,0x10,0x14,(byte)0xFF,(byte)0xFF,(byte)0xFF});
        query_db.put("Effacement defauts OBD",new byte[]{0x00,0x02,0x10,0x04});
        query_db.put("Lecture regime moteur",new byte[]{0x00,0x03,0x10,0x01,0x0C});
        query_db.put("Lecture nom ECU",new byte[]{0x00,0x03,0x10,0x09,0x0A});
        query_db.put("Lecture type de carburant",new byte[]{0x00,0x03,0x10,0x01,0x51});
        query_db.put("Lecture information VIN",new byte[]{0x00,0x03,0x10,0x09,0x02});
        query_db.put("Ouverture Actionneur",new byte[]{0x00,0x03,0x10,0x30,0x32,0x30});
        query_db.put("Lecture position EGR",new byte[]{0x00,0x02,0x10,0x22,0x30,0x30});
    }

    public void add_Query_msg(){
        query_db_msg.put("SELECT THE QUERY TO WRITE","PLEASE SELECT SOME QUERY TO ACTIVATE AND CLICK ON WRITE BUTTON");
        query_db_msg.put("Lecture Defauts CMM","00:04:10:19:02:FF");
        query_db_msg.put("Lecture Defauts OBD","00:02:10:03");
        query_db_msg.put("Effacement defauts","00:05:10:14:FF:FF:FF");
        query_db_msg.put("Effacement defauts OBD","00:02:10:04");
        query_db_msg.put("Lecture regime moteur","00:03:10:01:0C");
        query_db_msg.put("Lecture nom ECU","00:03:10:09:0A");
        query_db_msg.put("Lecture type de carburant","00:03:10:01:51");
        query_db_msg.put("Lecture information VIN","00:03:10:09:02");
        query_db_msg.put("Ouverture Actionneur","00:03:10:30:32:30");
        query_db_msg.put("Lecture position EGR","00:02:10:22:30:30");
    }

    public byte[] getQuery(String key){

        return query_db.get(key);
    }

    public String getQueryMsg(String key){

        return query_db_msg.get(key);
    }
}
