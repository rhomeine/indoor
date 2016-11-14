package com.bupt.indoorPosition.bean;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by WSPN on 2016/11/14.
 */

public class Buildings {
    public static ArrayList<String> BuildingsList = new ArrayList<String>();
    public static HashMap<String,String> BuildingsMap = new HashMap<>();
    static {
        BuildingsList.add("北邮科研大楼");
        BuildingsList.add("郑州中原金融产业园1栋");
        BuildingsMap.put("北邮科研大楼","57eb81cf08920f6b4b00053a");
        BuildingsMap.put("郑州中原金融产业园1栋","582581a608920f6b4b00132a");
    }
}
