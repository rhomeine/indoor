package com.bupt.indooranalysis.Util;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.animation.MenuAnimationHandler;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by rhomeine on 16/10/10.
 */

public class MyFloatingActionMenu extends FloatingActionMenu {

    public MyFloatingActionMenu(View mainActionView, int startAngle, int endAngle, int radius, ArrayList<Item> subActionItems, MenuAnimationHandler animationHandler, boolean animated, FloatingActionMenu.MenuStateChangeListener stateChangeListener) {
        super(mainActionView,startAngle,endAngle,radius,subActionItems,animationHandler,animated,stateChangeListener);
    }

    private void calculateItemPositions(){

    }

}
