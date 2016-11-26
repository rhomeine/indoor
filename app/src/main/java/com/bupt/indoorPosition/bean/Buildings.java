package com.bupt.indoorPosition.bean;

import com.sails.engine.core.model.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by WSPN on 2016/11/14.
 */

public class Buildings {

    public static Map<String, Integer> buildingName = new HashMap<>();
    public static Map<String, String> buildingCity = new HashMap<>();
    public static Map<String, LocalizationBeacon> InspectHistory = new HashMap<>();
    public static String currentBuilding = "郑州中原金融产业园1栋";
    public static String currentFloor = "1";


    public static HashMap<String, Buildings> BuildingsList = new HashMap<String, Buildings>();

    static {
//        BuildingsList.add("北邮科研大楼");
//        BuildingsList.add("郑州中原金融产业园1栋");
//        BuildingsMap.put("北邮科研大楼","57eb81cf08920f6b4b00053a");
//        BuildingsMap.put("郑州中原金融产业园1栋","582581a608920f6b4b00132a");
        buildingName.put("北邮科研大楼", 1);
        buildingName.put("郑州中原金融产业园1栋", 0);

        buildingCity.put("北邮科研大楼", "海淀");
        buildingCity.put("郑州中原金融产业园1栋", "郑州");

        HashMap<String, GeoPoint> LBGeoPointList = new HashMap<String, GeoPoint>();
        HashMap<String, GeoPoint> RTGeoPointList = new HashMap<String, GeoPoint>();
        LBGeoPointList.put("1层", new GeoPoint(34.7482914, 113.7926622));
        RTGeoPointList.put("1层", new GeoPoint(34.7500764, 113.7941206));

        LBGeoPointList.put("2层", new GeoPoint(34.7482886, 113.7926679));
        RTGeoPointList.put("2层", new GeoPoint(34.750066, 113.7941217));

        LBGeoPointList.put("3层", new GeoPoint(34.7482849, 113.7926703));
        RTGeoPointList.put("3层", new GeoPoint(34.7500722, 113.7941214));

        LBGeoPointList.put("4层", new GeoPoint(34.7482849, 113.7926703));
        RTGeoPointList.put("4层", new GeoPoint(34.7500722, 113.7941214));

        LBGeoPointList.put("5层", new GeoPoint(34.7482912, 113.7926698));
        RTGeoPointList.put("5层", new GeoPoint(34.7500686, 113.7941235));

        HashMap<String, GeoPoint> LBGeoPointList2 = new HashMap<String, GeoPoint>();
        HashMap<String, GeoPoint> RTGeoPointList2 = new HashMap<String, GeoPoint>();
        LBGeoPointList2.put("5层", new GeoPoint(39.96289894781549, 116.35293035811996));
        RTGeoPointList2.put("5层", new GeoPoint(39.96304388207584, 116.35312012440777));

        LBGeoPointList2.put("6层", new GeoPoint(39.96289894781549, 116.35293035811996));
        RTGeoPointList2.put("6层", new GeoPoint(39.96304388207584, 116.35312012440777));

        LBGeoPointList2.put("7层", new GeoPoint(39.96289894781549, 116.35293035811996));
        RTGeoPointList2.put("7层", new GeoPoint(39.96304388207584, 116.35312012440777));

        BuildingsList.put("北邮科研大楼", new Buildings("北邮科研大楼", "57eb81cf08920f6b4b00053a", LBGeoPointList2, RTGeoPointList2));
        BuildingsList.put("郑州中原金融产业园1栋", new Buildings("郑州中原金融产业园1栋", "582581a608920f6b4b00132a", LBGeoPointList, RTGeoPointList));

    }

    public Buildings() {
        new Buildings(null, null, null, null);
    }

    public Buildings(String name, String code, HashMap<String, GeoPoint> lb, HashMap<String, GeoPoint> rt) {
        this.Name = name;
        this.Code = code;
        this.LBGeoPointList = lb;
        this.RTGeoPointList = rt;
        floorList = null;
    }

    private String Name;
    private String Code;
    private ArrayList<String> floorList;
    private HashMap<String, GeoPoint> LBGeoPointList;
    private HashMap<String, GeoPoint> RTGeoPointList;

    public String getName() {
        return Name;
    }

    public String getCode() {
        return Code;
    }

    public GeoPoint getLBGeoPoint(String floor) {
        return LBGeoPointList.get(floor);
    }

    public GeoPoint getRTGeoPoint(String floor) {
        return RTGeoPointList.get(floor);
    }

    public static GeoPoint getLBGeoPoint(String buildingName, String floor) {
        return BuildingsList.get(buildingName).getLBGeoPoint(floor);
    }

    public static GeoPoint getRTGeoPoint(String buildingName, String floor) {
        return BuildingsList.get(buildingName).getRTGeoPoint(floor);
    }
}
