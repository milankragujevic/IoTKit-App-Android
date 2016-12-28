package com.cylan.jiafeigou.cache.pool;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.IDataPoint;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cylan-hunt on 16-12-26.
 */

public class GlobalDataPool implements IDataPool {

    private HashMap<String, JFGDevice> jfgDeviceMap = new HashMap<>();
    private static GlobalDataPool instance;
    private JFGAccount jfgAccount;
    private IDataPoint dataPointManager;
    private boolean isOnline;

    private GlobalDataPool() {
    }

    public static GlobalDataPool getInstance() {
        if (instance == null) {
            synchronized (GlobalDataPool.class) {
                if (instance == null) instance = new GlobalDataPool();
            }
        }
        return instance;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setJfgAccount(JFGAccount jfgAccount) {
        this.jfgAccount = jfgAccount;
    }

    public JFGAccount getJfgAccount() {
        return jfgAccount;
    }

    public void setDataPointManager(IDataPoint dataPointManager) {
        this.dataPointManager = dataPointManager;
    }

    @Override
    public void cacheDevice(JFGDevice jfgDevice) {
        checkAccount();
        jfgDeviceMap.put(jfgAccount.getAccount() + jfgDevice.uuid, jfgDevice);
    }

    @Override
    public JFGDevice fetch(String uuid) {
        checkAccount();
        return jfgDeviceMap.get(jfgAccount.getAccount() + uuid);
    }

    private void checkAccount() {
        if (jfgAccount == null || TextUtils.isEmpty(jfgAccount.getAccount())) {
            if (BuildConfig.DEBUG) throw new IllegalArgumentException("account is null");
            AppLogger.e("account is null");
        }
    }

    @Override
    public boolean insert(String uuid, BaseValue baseValue) {
        return dataPointManager.insert(uuid, baseValue);
    }

    @Override
    public boolean update(String uuid, BaseValue baseValue) {
        return dataPointManager.update(uuid, baseValue);
    }

    @Override
    public Object delete(String uuid, long id) {
        return dataPointManager.delete(uuid, id);
    }

    @Override
    public Object delete(String uuid, long id, long version) {
        return dataPointManager.delete(uuid, id, version);
    }

    @Override
    public BaseValue fetchLocal(String uuid, long id) {
        return dataPointManager.fetchLocal(uuid, id);
    }

    @Override
    public boolean deleteAll(String uuid, long id, ArrayList<Long> versions) {
        return false;
    }

    @Override
    public ArrayList<BaseValue> fetchLocalList(String uuid, long id) {
        return dataPointManager.fetchLocalList(uuid, id);
    }

    @Override
    public boolean isArrayType(int id) {
        return dataPointManager.isArrayType(id);
    }

    @Override
    public long robotGetData(String peer, ArrayList<JFGDPMsg> queryDps, int limit, boolean asc, int timeoutMs) throws JfgException {
        return dataPointManager.robotGetData(peer, queryDps, limit, asc, timeoutMs);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String uuid, int id) {
        if (isArrayType(id)) {
            throw new IllegalArgumentException(String.format("id:%s is an array type in the map", id));
        }
        try {
            BaseValue base = dataPointManager.fetchLocal(uuid, id);
            return base == null || base.getValue() == null ? null : (T) base.getValue();
        } catch (ClassCastException c) {
            return null;
        }
    }
}
