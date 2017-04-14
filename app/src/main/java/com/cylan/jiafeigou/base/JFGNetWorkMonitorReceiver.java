package com.cylan.jiafeigou.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

public class JFGNetWorkMonitorReceiver extends BroadcastReceiver {

    public JFGNetWorkMonitorReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        JFGSourceManager sourceManager = BaseApplication.getAppComponent().getSourceManager();
        if (sourceManager.getAccount() == null) {//只在有账号信息的情况下发送网络状态变化通知
            return;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mobNetInfo != null && !mobNetInfo.isConnected() && wifiNetInfo != null && !wifiNetInfo.isConnected() && !sourceManager.isOnline()) {
//                BSToast.showLong(context, "网络不可以用");
            AppLogger.d("当前网络不可用");
            RxEvent.NetConnectionEvent connectionEvent = new RxEvent.NetConnectionEvent(false);
            connectionEvent.mobile = mobNetInfo;
            connectionEvent.wifi = wifiNetInfo;
            connectionEvent.isOnLine = false;
            RxBus.getCacheInstance().post(connectionEvent);
            //改变背景或者 处理网络的全局变量
        } else if (sourceManager.isOnline() && ((mobNetInfo != null && mobNetInfo.isConnected()) || (wifiNetInfo != null && wifiNetInfo.isConnected()))) {
            //改变背景或者 处理网络的全局变量
            AppLogger.d("当前网络可用");
            RxEvent.NetConnectionEvent connectionEvent = new RxEvent.NetConnectionEvent(true);
            connectionEvent.mobile = mobNetInfo;
            connectionEvent.wifi = wifiNetInfo;
            connectionEvent.isOnLine = true;
            RxBus.getCacheInstance().post(connectionEvent);
        }
    }
}
