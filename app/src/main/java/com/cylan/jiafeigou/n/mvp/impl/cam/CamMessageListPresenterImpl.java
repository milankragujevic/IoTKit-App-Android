package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskResult;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPTaskResult;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamMessageListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.AESUtil;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.widget.wheel.WonderIndicatorWheelView;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Response;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-7-13.
 */
public class CamMessageListPresenterImpl extends AbstractPresenter<CamMessageListContract.View>
        implements CamMessageListContract.Presenter {

    private List<WonderIndicatorWheelView.WheelItem> dateItemList = new ArrayList<>();

    public CamMessageListPresenterImpl(CamMessageListContract.View view, String uuid) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{sdcardStatusSub()};
    }

    @Override
    protected boolean registerTimeTick() {
        return true;
    }

    @Override
    protected void onTimeTick() {

    }

    /**
     * sd卡状态更新
     *
     * @return
     */
    private Subscription sdcardStatusSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .filter((RxEvent.DeviceSyncRsp data) -> (getView() != null && TextUtils.equals(uuid, data.uuid)))
                .filter(ret -> ret.dpList != null)
                .flatMap(ret -> Observable.from(ret.dpList))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(msg -> {
                    try {
                        getView().deviceInfoChanged((int) msg.id, msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    AppLogger.d("收到,属性同步了");
                }, e -> AppLogger.d(e.getMessage()));
    }


    private List<IDPEntity> buildEntity(long timeStart, boolean asc, boolean isMaxTime, boolean useMaxLimit) {
        List<IDPEntity> list = new ArrayList<>();
        list.add(new DPEntity()
                .setMsgId(222)
                .setUuid(uuid)
                .setAction(DBAction.CAM_MULTI_QUERY)
                .setOption(new DBOption.MultiQueryOption(timeStart, asc, isMaxTime, useMaxLimit))
                .setAccount(BaseApplication.getAppComponent().getSourceManager().getJFGAccount().getAccount()));
        list.add(new DPEntity()
                .setMsgId(505)
                .setUuid(uuid)
                .setAction(DBAction.CAM_MULTI_QUERY)
                .setOption(new DBOption.MultiQueryOption(timeStart, asc, isMaxTime, useMaxLimit))
                .setAccount(BaseApplication.getAppComponent().getSourceManager().getJFGAccount().getAccount()));
        list.add(new DPEntity()
                .setMsgId(512)
                .setUuid(uuid)
                .setAction(DBAction.CAM_MULTI_QUERY)
                .setOption(new DBOption.MultiQueryOption(timeStart, asc, isMaxTime, useMaxLimit))
                .setAccount(BaseApplication.getAppComponent().getSourceManager().getJFGAccount().getAccount()));

        //adapter for doorbell
        list.add(new DPEntity()
                .setMsgId(401)
                .setUuid(uuid)
                .setAction(DBAction.CAM_MULTI_QUERY)
                .setOption(new DBOption.MultiQueryOption(timeStart, asc, isMaxTime, useMaxLimit))
                .setAccount(BaseApplication.getAppComponent().getSourceManager().getJFGAccount().getAccount()));

        return list;
    }

    /**
     * 消息列表请求
     *
     * @param timeStart
     * @param asc:true:从timeStart开始，往前查，直到timeEnd.,false:从timeStart开始，到现在。 false:向后查。true:向前查。
     * @return
     */
    private Observable<IDPTaskResult> getMessageListQuery(long timeStart, boolean asc) {
        Log.d("getMessageListQuery", "getMessageListQuery:" + timeStart + ",asc: " + asc);
        try {
            boolean isMax = TimeUtils.getSpecificDayEndTime(timeStart) == timeStart;
            boolean useMaxLimit = !JFGRules.isFaceFragment(getDevice().pid);
            return BaseApplication.getAppComponent().getTaskDispatcher().perform(buildEntity(timeStart, asc, isMax, useMaxLimit));
        } catch (Exception e) {
            return Observable.just(BaseDPTaskResult.ERROR);
        }
    }

    /**
     * 初次进入页面，先要确定第一天的时间。
     */
    private void loadDataListFirst() {
        refreshDateList(true);
    }

    @Override
    public void fetchMessageList(long timeStart, boolean asc, boolean refresh) {
        //1.timeStart==0->服务器，本地
        //服务器：1.日历。2.偏移到最靠近有数据的一天。开始查。以后，点击开始查。
        //本地，查出日历。
        if (asc) {
            // TODO: 2017/10/13 说明是刷新操作 ,则请求面孔信息
            getFaceGroupInformation();
        }
        if (timeStart == 0 && !JFGRules.isFaceFragment(getDevice().pid)) {
            loadDataListFirst();
            return;
        }

        Subscription subscription = getMessageListQuery(timeStart, false)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(baseDPTaskResult -> {
                    List<DataPoint> result = baseDPTaskResult.getResultResponse();
                    AppLogger.d("fetchLocalList: " + ListUtils.getSize(result));
                    if (ListUtils.getSize(result) == 0) {
                        return Observable.just(new ArrayList<CamMessageBean>());
                    }
                    ArrayList<CamMessageBean> list = new ArrayList<>();
                    for (DataPoint dataPoint : result) {
                        CamMessageBean bean = new CamMessageBean();
                        bean.id = dataPoint.getMsgId();
                        bean.version = dataPoint.getVersion();
                        if (bean.id == 222) {
                            bean.sdcardSummary = (DpMsgDefine.DPSdcardSummary) dataPoint;
                        }
                        if (bean.id == 512 || bean.id == 505) {
                            bean.alarmMsg = (DpMsgDefine.DPAlarm) dataPoint;
                        }
                        if (bean.id == 401) {
                            bean.bellCallRecord = ((DpMsgDefine.DPBellCallRecord) dataPoint);
                        }
                        if (!list.contains(bean))//防止重复
                        {
                            list.add(bean);
                        }
                    }
                    return Observable.just(list);
                })
                .filter(result -> mView != null && result != null)
                .flatMap(camList -> {
                    //需要和列表里面的items 融合
                    ArrayList<CamMessageBean> list = new ArrayList<>(mView.getList());
                    camList.removeAll(list);//camList是降序
                    AppLogger.d("uiList: " + ListUtils.getSize(list) + ",newList: " + ListUtils.getSize(camList));
                    if (camList.size() > 0) {
                        //检查是否 append 或者insert
                        if (ListUtils.getSize(list) == 0) {//已有的列表为空,直接append
                            return Observable.just(new Pair<>(camList, true));
                        }
                        if (camList.get(camList.size() - 1).version >= list.get(0).version) {
                            return Observable.just(new Pair<>(camList, false));//insert(0)
                        }
                        return Observable.just(new Pair<>(camList, true));//append
                    }
                    return Observable.just(new Pair<>(camList, true));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> mView != null && ret != null)
                .map(result -> {
//                    if (refresh) {
//                        mView.onListInsert(result.first, 0);
//                    } else
                    if (result.second) {
                        mView.onListAppend(result.first);
                    } else {
                        mView.onListInsert(result.first, 0);
                    }
                    return result;
                })
                .subscribeOn(Schedulers.io())
                .delay(500, TimeUnit.MILLISECONDS)
                .filter(ret -> mView != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> mView.loadingDismiss(),
                        throwable -> {
                            AppLogger.e("err: " + throwable.getLocalizedMessage());
                            mView.onErr();
                        });
        addSubscription(subscription, "DPCamMultiQueryTask");
    }

    String mock = "[{group_id:\"1000\",face_id:\"2000\",face_name:\"小明\"}]";

    //这个方法需要在子线程调用,可能涉及到有网操作
    public static String blockGetServiceKey() throws Exception {
        String serviceKey = PreferencesUtils.getString(JConstant.ROBOT_SERVICES_KEY, null);
        if (TextUtils.isEmpty(serviceKey)) {
            long seq = BaseApplication.getAppComponent().getCmd().sendUniservalDataSeq(4, DpUtils.pack(Security.getVId()));
            RxEvent.UniversalDataRsp dataRsp = RxBus.getCacheInstance().toObservable(RxEvent.UniversalDataRsp.class)
                    .filter(rsp -> rsp.seq == seq)
                    .first()
                    .timeout(10, TimeUnit.SECONDS, Observable.just(null))
                    .toBlocking().first();

            DpMsgDefine.DPAIService dpaiService = DpUtils.unpackData(dataRsp.data, DpMsgDefine.DPAIService.class);
            if (dpaiService != null) {
                serviceKey = dpaiService.service_key;//返回的有问题?
                PreferencesUtils.putString(JConstant.ROBOT_SERVICES_KEY, dpaiService.service_key);
                PreferencesUtils.putString(JConstant.ROBOT_SERVICES_SECERET, dpaiService.service_key_seceret);
            }
        }
        return serviceKey;
    }

    public void getFaceGroupInformation() {
        Observable.create((Observable.OnSubscribe<DpMsgDefine.FaceQueryResponse>) subscriber -> {
            try {
                String account = DataSourceManager.getInstance().getAccount().getAccount();
                String vid = Security.getVId();
                String serviceKey = blockGetServiceKey();
                String timestamp = String.valueOf(System.currentTimeMillis() / 1000);//这里的时间是秒
                String seceret = PreferencesUtils.getString(JConstant.ROBOT_SERVICES_SECERET, null);
                if (TextUtils.isEmpty(serviceKey) || TextUtils.isEmpty(seceret)) {
                    subscriber.onError(new IllegalArgumentException("ServiceKey或Seceret为空"));
                } else {
                    String sign = AESUtil.sign(JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_QUERY_API, seceret, timestamp);
                    String url = OptionsImpl.getRobotServer() + JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_QUERY_API;
                    if (!url.startsWith("http://")) {
                        url = "http://" + url;
                    }
                    Response response = OkGo.post(url)
                            .cacheMode(CacheMode.REQUEST_FAILED_READ_CACHE)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_VID, vid)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SERVICE_KEY, serviceKey)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_BUSINESS, "1")
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SERVICETYPE, "1")
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SIGN, sign)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_TIMESTAMP, timestamp)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_ACCOUNT, account)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SN, uuid)
                            .execute();

                    ResponseBody body = response.body();

                    if (body != null) {
                        String string = body.string();
                        AppLogger.w(string);
                        Gson gson = new Gson();
                        DpMsgDefine.ResponseHeader header = gson.fromJson(string, DpMsgDefine.ResponseHeader.class);
                        if (header.ret == 0) {
                            DpMsgDefine.FaceQueryResponse queryResponse = new Gson().fromJson(string, DpMsgDefine.FaceQueryResponse.class);
                            subscriber.onNext(queryResponse);
                            subscriber.onCompleted();
                        } else {
                            if (header.ret == 100) {
                                PreferencesUtils.remove(JConstant.ROBOT_SERVICES_KEY);
                                PreferencesUtils.remove(JConstant.ROBOT_SERVICES_SECERET);
                            }
                            subscriber.onError(new IllegalArgumentException("ret:" + header.ret + ",msg:" + header.msg));
                        }
                    } else {
                        subscriber.onError(null);
                    }
                }
            } catch (Exception e) {
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    if (rsp != null && rsp.ret == 0) {
                        mView.onFaceInformationReady(rsp.data);
                    } else {
                        // TODO: 2017/10/13 怎么处理呢? 最好不处理
                    }
                }, e ->

                {
                    AppLogger.e(MiscUtils.getErr(e));
                });


    }

    private List<IDPEntity> buildMultiEntities(ArrayList<CamMessageBean> beanList) {
        List<IDPEntity> entities = new ArrayList<>();
        for (CamMessageBean bean : beanList) {
            DPEntity dpEntity = new DPEntity();
            dpEntity.setUuid(uuid);
            dpEntity.set_id(bean.id);
            dpEntity.setAccount(BaseApplication.getAppComponent().getSourceManager().getJFGAccount().getAccount());
            dpEntity.setMsgId((int) bean.id);
            dpEntity.setVersion(bean.version);
            dpEntity.setAction(DBAction.DELETED);
            entities.add(dpEntity);
            Log.d(TAG, "删除:" + bean.id + ",version:" + bean.version);
        }
        return entities;
    }

    public Observable<IDPTaskResult> perform(List<? extends IDPEntity> entity) {
        return BaseApplication.getAppComponent().getTaskDispatcher().perform(entity);
    }

    @Override
    public void removeItems(ArrayList<CamMessageBean> beanList) {
        if (ListUtils.isEmpty(beanList)) {
            return;
        }
        List<IDPEntity> list = buildMultiEntities(beanList);
        Subscription subscription = BaseApplication.getAppComponent().getTaskDispatcher().perform(list)
                .subscribeOn(Schedulers.io())
                .filter(result -> mView != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(idpTaskResult -> {
                    if (idpTaskResult.getResultCode() == 0) {
                        //good
                        mView.onMessageDeleteSuc();
                        AppLogger.d("需要加载下一页");
                        if (ListUtils.getSize(mView.getList()) == 0) {
                            //需要刷新日期.
                            refreshDateList(false);
                        }
                    }
                }, throwable -> {
                    AppLogger.e("err:" + throwable.getLocalizedMessage());
                    mView.onErr();
                });
        addSubscription(subscription, "removeItems");
    }

    @Override
    public void removeAllItems(ArrayList<CamMessageBean> beanList, boolean removeAll) {

    }


    @Override
    public List<WonderIndicatorWheelView.WheelItem> getDateList() {
        return dateItemList;
    }

    /**
     * 日历列表请求
     *
     * @return
     */
    private Observable<IDPTaskResult> getDateListQuery() {
        DPEntity entity = new DPEntity();
        JFGAccount account = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        entity.setAccount(account == null ? "" : account.getAccount());
        entity.setUuid(uuid);
//        if (device.available()) {
//        if (JFGRules.isFaceFragment(device.pid)) {
//            entity.setOption(DBOption.BaseDBOption.CamMultiDateOption.CAMERA_FACE_ANY_DAYS);
//        } else
        if (JFGRules.isBell(device.pid)) {
            entity.setOption(DBOption.BaseDBOption.CamMultiDateOption.BELL_7_DAYS);
        } else if (JFGRules.isCamera(device.pid)) {
            entity.setOption(DBOption.BaseDBOption.CamMultiDateOption.BELL_7_DAYS);
        }
//        } else {
//            entity.setOption(DBOption.BaseDBOption.CamMultiDateOption.BELL_7_DAYS);
//        }
        entity.setAction(DBAction.CAM_DATE_QUERY);
        try {
            return BaseApplication.getAppComponent().getTaskDispatcher().perform(entity);
        } catch (Exception e) {
            return Observable.just(BaseDPTaskResult.ERROR);
        }
    }

    @Override
    public void refreshDateList(boolean needToLoadList) {
        BaseApplication.getAppComponent().getSourceManager().syncDeviceProperty(uuid, 204);
        Subscription subscription = getDateListQuery()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                .filter(ret -> mView != null && ret != null && ret.getResultResponse() != null)
                .map(result -> {
                    if (result.getResultCode() == 0) {
//                        mView.onDateMapRsp(dateItemList = result.getResultResponse());
                        dateItemList = result.getResultResponse();
                    }
                    return dateItemList;
                })
                .filter(ret -> needToLoadList)
                .subscribeOn(Schedulers.computation())
                .map(wheelItems -> {
                    long timeHit = 0;
                    WonderIndicatorWheelView.WheelItem theItem = null;
                    for (WonderIndicatorWheelView.WheelItem item : wheelItems) {
                        if (item.wonderful) {//找出最近的一天.就可以了.
                            if (item.time > timeHit) {
                                timeHit = item.time;
                                theItem = item;
                            }
                        }
                    }
                    if (timeHit != 0) {
                        //需要保证这个timeHit是当天的最大的一个,干脆用一天的最后一秒
                        AppLogger.d("Max dateList timeHit before:" + timeHit);
                        timeHit = TimeUtils.getSpecificDayEndTime(timeHit);
                        AppLogger.d("Max dateList timeHit:" + timeHit);
                        fetchMessageList(timeHit, false, false);
                    }
                    if (theItem != null) {
                        int index = dateItemList.indexOf(theItem);
                        theItem.selected = true;
                        if (index >= 0) {
                            dateItemList.get(index).selected = true;
                        }
                        Log.d("selected", "selected?" + dateItemList);
                    }
                    return new Pair<>(timeHit, theItem);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> mView != null)
                .subscribe(pair -> {
                    if (pair.first == 0) {
                        mView.onErr();//no date list
                    } else {
                        mView.onDateMapRsp(dateItemList);
                    }
                }, AppLogger::e);
        addSubscription(subscription, "DPCamDateQueryTask");
    }

    @Override
    public void deleteFace(String face_id, String person_id, String group_id) {
        Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            try {
                String account = DataSourceManager.getInstance().getAccount().getAccount();
                String vid = Security.getVId();
                String serviceKey = blockGetServiceKey();
                String timestamp = String.valueOf(System.currentTimeMillis() / 1000);//这里的时间是秒
                String seceret = PreferencesUtils.getString(JConstant.ROBOT_SERVICES_SECERET, null);
                if (TextUtils.isEmpty(serviceKey) || TextUtils.isEmpty(seceret)) {
                    subscriber.onError(new IllegalArgumentException("ServiceKey或Seceret为空"));
                } else {
                    String sign = AESUtil.sign(JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_DELETE_API, seceret, timestamp);
                    String url = OptionsImpl.getRobotServer() + JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_DELETE_API;
                    if (!url.startsWith("http://")) {
                        url = "http://" + url;
                    }
                    Response response = OkGo.post(url)
                            .cacheMode(CacheMode.REQUEST_FAILED_READ_CACHE)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_VID, vid)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SERVICE_KEY, serviceKey)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_BUSINESS, "1")
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SERVICETYPE, "1")
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SIGN, sign)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_TIMESTAMP, timestamp)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_FACE_ID, face_id)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_PERSON_ID, person_id)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_GROUP_ID, group_id)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_ACCOUNT, account)
                            .params(JConstant.RobotCloudApi.ROBOTSCLOUD_SN, uuid)
                            .execute();

                    ResponseBody body = response.body();

                    if (body != null) {
                        String string = body.string();
                        AppLogger.w(string);
                        Gson gson = new Gson();
                        DpMsgDefine.ResponseHeader header = gson.fromJson(string, DpMsgDefine.ResponseHeader.class);
                        subscriber.onNext(header == null ? -1 : header.ret);
                    } else {
                        subscriber.onError(null);
                    }
                }
            } catch (Exception e) {
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    if (rsp == 0) {
                        // TODO: 2017/10/14 删除成功了
                    } else {
                        // TODO: 2017/10/14 删除失败了
                    }
                }, e ->

                {
                    AppLogger.e(MiscUtils.getErr(e));
                });


    }

    @Override
    public void stop() {
        super.stop();
    }

    public static class GFN {
        public String group_id;
        public String face_id;
        public String face_name;
    }
}
