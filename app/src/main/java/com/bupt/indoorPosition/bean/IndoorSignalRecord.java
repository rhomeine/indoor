package com.bupt.indoorPosition.bean;

import java.sql.Timestamp;

/**
 * Created by delove on 2016/11/8.
 * 定位使用的信号参数
 */

public class IndoorSignalRecord implements Cloneable {

    private int positionX;
    private int positionY;
    private Timestamp time;
    private String netType;
    private String networkType;
    private int cid;// Cell Identity，基站编号，是个16位的数据（范围是0到65535）
    private int lac;// Location Area Code，位置区域码
    private int signalStrength;
    // private String mcc;// Mobile Country Code，移动国家代码（中国的为460）
    private String mnc;// Mobile Network Code，移动网络号码（中国移动为00，中国联通为01）
    private String uuid;
    private int rsrp;
    private int rsrq;
    private int sinr;
    private String imsi;

    public IndoorSignalRecord() {
    }

    public IndoorSignalRecord(int signalStrength, int cid, int positionX, int positionY, Timestamp time, String netType, String networkType, int lac,  String mnc, String uuid, int rsrp, int rsrq, int sinr, String imsi) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.time = time;
        this.netType = netType;
        this.networkType = networkType;
        this.cid = cid;
        this.lac = lac;
        this.signalStrength = signalStrength;
        this.mnc = mnc;
        this.uuid = uuid;
        this.rsrp = rsrp;
        this.rsrq = rsrq;
        this.imsi = imsi;
        this.sinr = sinr;
    }

    @Override
    public Object clone() {

        IndoorSignalRecord ir = null;
        try {
            ir = (IndoorSignalRecord) super.clone();
        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (ir == null)
            return null;
        if (this.time != null)
            ir.time = (Timestamp) this.time.clone();
        return ir;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getNetType() {
        return netType;
    }

    public void setNetType(String netType) {
        this.netType = netType;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getLac() {
        return lac;
    }

    public void setLac(int lac) {
        this.lac = lac;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMnc() {
        return mnc;
    }

    public void setMnc(String mnc) {
        this.mnc = mnc;
    }

    public int getRsrp() {
        return rsrp;
    }

    public void setRsrp(int rsrp) {
        this.rsrp = rsrp;
    }

    public int getRsrq() {
        return rsrq;
    }

    public void setRsrq(int rsrq) {
        this.rsrq = rsrq;
    }

    public int getSinr() {
        return sinr;
    }

    public void setSinr(int sinr) {
        this.sinr = sinr;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }
    @Override
    public String toString() {
        return this.time + "," + this.signalStrength + "," + this.cid + "...";
    }
}
