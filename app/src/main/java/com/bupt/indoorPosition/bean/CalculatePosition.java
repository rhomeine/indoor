package com.bupt.indoorPosition.bean;

/**
 * Created by WSPN on 2016/10/12.
 */

public class CalculatePosition {
    private int maxLikehoodX;
    private int maxLikehoodY;
    private int dealedMLX;
    private int dealedMLY;
    private int threeX;
    private int threeY;
    private int dealedThreeX;
    private int dealedThreeY;

    public CalculatePosition(int maxLikehoodX, int maxLikehoodY, int dealedMLX, int dealedMLY, int threeX, int threeY, int dealedThreeX,
                             int dealedThreeY) {
        this.maxLikehoodX = maxLikehoodX;
        this.maxLikehoodY = maxLikehoodY;
        this.dealedMLX = dealedMLX;
        this.dealedMLY = dealedMLY;
        this.threeX = threeX;
        this.threeY = threeY;
        this.dealedThreeX = dealedThreeX;
        this.dealedThreeY = dealedThreeY;
    }

    public int getMaxLikehoodX() {
        return maxLikehoodX;
    }

    public void setMaxLikehoodX(int maxLikehoodX) {
        this.maxLikehoodX = maxLikehoodX;
    }

    public int getMaxLikehoodY() {
        return maxLikehoodY;
    }

    public void setMaxLikehoodY(int maxLikehoodY) {
        this.maxLikehoodY = maxLikehoodY;
    }

    public int getDealedMLX() {
        return dealedMLX;
    }

    public void setDealedMLX(int dealedMLX) {
        this.dealedMLX = dealedMLX;
    }

    public int getDealedMLY() {
        return dealedMLY;
    }

    public void setDealedMLY(int dealedMLY) {
        this.dealedMLY = dealedMLY;
    }

    public int getThreeX() {
        return threeX;
    }

    public void setThreeX(int threeX) {
        this.threeX = threeX;
    }

    public int getThreeY() {
        return threeY;
    }

    public void setThreeY(int threeY) {
        this.threeY = threeY;
    }

    public int getDealedThreeX() {
        return dealedThreeX;
    }

    public void setDealedThreeX(int dealedThreeX) {
        this.dealedThreeX = dealedThreeX;
    }

    public int getDealedThreeY() {
        return dealedThreeY;
    }

    public void setDealedThreeY(int dealedThreeY) {
        this.dealedThreeY = dealedThreeY;
    }
}
