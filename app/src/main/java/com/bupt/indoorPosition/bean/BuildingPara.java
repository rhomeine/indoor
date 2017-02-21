package com.bupt.indoorPosition.bean;

/**
 * Created by rhomeine on 17/1/16.
 */

public class BuildingPara {
    private int buildingFloor;
    private int buildingCollectNum;
    private int buildingCollectTime;
    private int buildingCollectSleep;
    private int buildingDisThreshold;

    public int getBuildingFloor() {
        return buildingFloor;
    }

    public void setBuildingFloor(int buildingFloor) {
        this.buildingFloor = buildingFloor;
    }

    public int getBuildingCollectNum() {
        return buildingCollectNum;
    }

    public void setBuildingCollectNum(int buildingCollectNum) {
        this.buildingCollectNum = buildingCollectNum;
    }

    public int getBuildingCollectTime() {
        return buildingCollectTime;
    }

    public void setBuildingCollectTime(int buildingCollectTime) {
        this.buildingCollectTime = buildingCollectTime;
    }

    public int getBuildingCollectSleep() {
        return buildingCollectSleep;
    }

    public void setBuildingCollectSleep(int buildingCollectSleep) {
        this.buildingCollectSleep = buildingCollectSleep;
    }

    public int getBuildingDisThreshold() {
        return buildingDisThreshold;
    }

    public void setBuildingDisThreshold(int buildingDisThreshold) {
        this.buildingDisThreshold = buildingDisThreshold;
    }
}
