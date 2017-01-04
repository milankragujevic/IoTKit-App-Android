package com.cylan.jiafeigou.n.mvp.impl.bell;

import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellSettingContract;
import com.cylan.jiafeigou.n.mvp.model.BaseBean;
import com.cylan.jiafeigou.n.mvp.model.BeanBellInfo;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.rx.RxUiEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-8-3.
 */
public class BellSettingPresenterImpl extends BasePresenter<BellSettingContract.View>
        implements BellSettingContract.Presenter {
    private BeanBellInfo beanBellInfo;
//    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    private void fillData(DeviceBean bean) {
        beanBellInfo = new BeanBellInfo();
        BaseBean baseBean = new BaseBean();
        baseBean.alias = bean.alias;
        baseBean.pid = bean.pid;
        baseBean.uuid = bean.uuid;
        baseBean.sn = bean.sn;
        beanBellInfo.convert(baseBean, bean.dataList);
    }

    protected Subscription[] register() {
        return new Subscription[]{
                onBellInfoSubscription(),
                onLoginStateSubscription(),
                unbindDevSub()};
    }

    /**
     * 门铃解绑
     *
     * @return
     */
    private Subscription unbindDevSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.UnBindDeviceEvent.class)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<RxEvent.UnBindDeviceEvent, Object>() {
                    @Override
                    public Object call(RxEvent.UnBindDeviceEvent unBindDeviceEvent) {
                        mView.unbindDeviceRsp(unBindDeviceEvent.jfgResult.code);
                        if (unBindDeviceEvent.jfgResult.code == 0) {
                            //清理这个订阅
                            RxBus.getCacheInstance().removeStickyEvent(RxEvent.UnBindDeviceEvent.class);
                        }
                        return null;
                    }
                })
                .retry(new RxHelper.RxException<>("unbindDevSub"))
                .subscribe();
    }

    private Subscription onBellInfoSubscription() {
        //查询设备列表
        return RxBus.getCacheInstance().toObservableSticky(RxUiEvent.BulkDeviceListRsp.class)
                .subscribeOn(Schedulers.computation())
                .filter(new Func1<RxUiEvent.BulkDeviceListRsp, Boolean>() {
                    @Override
                    public Boolean call(RxUiEvent.BulkDeviceListRsp list) {
                        return list != null && list.allDevices != null;
                    }
                })
                .flatMap(new Func1<RxUiEvent.BulkDeviceListRsp, Observable<DpMsgDefine.DpWrap>>() {
                    @Override
                    public Observable<DpMsgDefine.DpWrap> call(RxUiEvent.BulkDeviceListRsp list) {
                        for (DpMsgDefine.DpWrap wrap : list.allDevices) {
                            if (beanBellInfo.deviceBase == null || wrap.baseDpDevice == null)
                                continue;
                            if (TextUtils.equals(wrap.baseDpDevice.uuid,
                                    beanBellInfo.deviceBase.uuid)) {
                                return Observable.just(wrap);
                            }
                        }
                        return null;
                    }
                })
                .filter(new Func1<DpMsgDefine.DpWrap, Boolean>() {
                    @Override
                    public Boolean call(DpMsgDefine.DpWrap dpWrap) {
                        return dpWrap != null && dpWrap.baseDpDevice != null;
                    }
                })
                .flatMap(new Func1<DpMsgDefine.DpWrap, Observable<BeanBellInfo>>() {
                    @Override
                    public Observable<BeanBellInfo> call(DpMsgDefine.DpWrap dpWrap) {
                        BeanBellInfo info = new BeanBellInfo();
                        info.convert(dpWrap.baseDpDevice, dpWrap.baseDpMsgList);
                        beanBellInfo = info;
                        AppLogger.i("BeanCamInfo: " + new Gson().toJson(info));
                        return Observable.just(info);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BeanBellInfo>() {
                    @Override
                    public void call(BeanBellInfo beanBellInfo) {
                        //刷新
                        mView.onSettingInfoRsp(beanBellInfo);
                    }
                });
    }

    /**
     * 查询登陆状态
     *
     * @return
     */
    private Subscription onLoginStateSubscription() {
        return RxBus.getCacheInstance().toObservable(RxEvent.LoginRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.LoginRsp>() {
                    @Override
                    public void call(RxEvent.LoginRsp o) {
                        mView.onLoginState(o.state);
                    }
                });
    }

    @Override
    public void unbindDevice() {
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        String uuid = beanBellInfo.deviceBase.uuid;
                        RxEvent.UnbindJFGDevice deletion = new RxEvent.UnbindJFGDevice();
                        deletion.uuid = uuid;
                        RxBus.getCacheInstance().post(deletion);
                        try {
                            JfgCmdInsurance.getCmd().unBindDevice(uuid);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                        AppLogger.i("unbind uuid: " + uuid);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("delete uuid failed: " + throwable.getLocalizedMessage());
                    }
                });
    }

    @Override
    public BeanBellInfo getBellInfo() {
        return null;
    }
}
