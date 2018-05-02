package com.zhixin.locationlib;

import android.app.Application;

import com.zhixin.roav.location.output.LocationInstanceManager;
import com.zhixin.roav.parser.DexLoader;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/4/17.
 *         Copyright © 2017年 Oceanwing. All rights reserved.
 */

public class MyApplication extends Application {
    private static final String DEX_PATH = "encryptdex";
    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }
    private void init() {
        ClassLoader load = DexLoader.getInstance().load(this, DEX_PATH);
        LocationInstanceManager.getInstance().init(this,load);
    }
}
