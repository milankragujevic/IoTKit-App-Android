package com.cylan.jiafeigou.base.wrapper;

import android.support.annotation.CallSuper;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPTaskResult;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.HandlerThreadUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


/**
 * Created by yzd on 16-12-28.
 */

public abstract class BasePresenter<V extends JFGView> implements JFGPresenter {
    protected String TAG = getClass().getName();

    protected String mUUID;

    protected JFGSourceManager mSourceManager;

    private CompositeSubscription mSubscriptions;
    private LongSparseArray<ResponseParser> mResponseParserMap = new LongSparseArray<>(32);
    private Map<String, LocalUDPMessageParser> mLocalMessageParserMap = new HashMap<>(32);

    protected V mView;
    private ArrayList<Long> mRequestSeqs = new ArrayList<>(32);

    protected void unSubscribe(Subscription... subscriptions) {
        if (subscriptions != null) {
            for (Subscription subscription : subscriptions) {
                if (subscription != null && !subscription.isUnsubscribed()) {
                    subscription.unsubscribe();
                }
            }
        }
    }


    @Override
    public void onViewAttached(JFGView view) {
        mView = (V) view;
        onRegisterResponseParser();
        mSourceManager = DataSourceManager.getInstance();
    }

    @Override
    @CallSuper
    public void onStart() {
        onRegisterSubscription();
    }

    @CallSuper
    protected void onRegisterSubscription() {
        registerSubscription(
                getDeviceSyncSub(),
                getLoginStateSub(),
                getDeleteDataRspSub(),
                getLocalUDPMessageSub()
        );
    }

    @CallSuper
    protected void onRegisterResponseParser() {
    }

    /**
     * 如果不需要在onStop中进行反注册,可以重写这个方法,然后在自定义的地方反注册
     */
    protected void onUnRegisterSubscription() {
        unSubscribe(mSubscriptions);
        mRequestSeqs.clear();//清空请求队列
        mSubscriptions = null;
    }

    @Override
    @CallSuper
    public void onStop() {
        onUnRegisterSubscription();
    }

    @Override
    public void onViewDetached() {
        mView = null;
        mSourceManager = null;
    }

    protected void onLoginStateChanged(RxEvent.OnlineStatusRsp loginState) {
        mView.onLoginStateChanged(loginState.state);
    }


    /**
     * 监听登录状态的变化时基本的功能,所以提取到基类中
     */
    private Subscription getLoginStateSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.OnlineStatusRsp.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onLoginStateChanged, e -> {
                    e.printStackTrace();
                    registerSubscription(getLoginStateSub());//出现异常了要重现注册
                });
    }

    protected Subscription getLocalUDPMessageSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(msg -> {
                    try {
                        JfgUdpMsg.UdpHeader header = DpUtils.unpackData(msg.data, JfgUdpMsg.UdpHeader.class);
                        LocalUDPMessageParser parser;
                        if (header != null && (parser = mLocalMessageParserMap.get(header.cmd)) != null) {
                            parser.onParseLocalUDPMsg(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, throwable -> {
                    registerSubscription(getLocalUDPMessageSub());
                    throwable.printStackTrace();
                });
    }

    /**
     * 监听设备同步消息是基本功能,所以提取到基类中
     */
    private Subscription getDeviceSyncSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .subscribeOn(Schedulers.io())
                .filter(rsp -> accept(rsp.uuid))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    onDeviceSync();
                }, e -> {
                    e.printStackTrace();//打印错误日志以便排错
                    registerSubscription(getDeviceSyncSub());//基类不能崩,否则一些功能异常
                });
    }

    protected void onDeviceSync() {
    }

    private Subscription getDeleteDataRspSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeleteDataRsp.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deleteDataRsp -> {

                }, e -> {
                    e.printStackTrace();
                    registerSubscription(getDeleteDataRspSub());
                });
    }

    protected void onParseResponseCompleted(long seq) {

    }

    public boolean hasReadyForExit() {
        return true;
    }

    public Observable<IDPTaskResult> perform(IDPEntity entity) {
        return BaseDPTaskDispatcher.getInstance().perform(entity);
    }

    public Observable<IDPTaskResult> perform(List<? extends IDPEntity> entity) {
        return BaseDPTaskDispatcher.getInstance().perform(entity);
    }

    /**
     * 获取代表当前view的cid有些feature需要这个cid属性来进行过滤
     *
     * @deprecated 现在view会自动设置uuid到presenter中, 因此这个方法也就意义不大了
     */
    @Deprecated
    protected String onResolveViewIdentify() {
        return "This Method Should Be Override If The View Should Use The Identify To Filter";
    }

    @Override
    public void onSetViewUUID(String uuid) {
        mUUID = uuid;
    }

    @Override
    public void onViewAction(int action, String handle, Object extra) {
    }

    @Override
    public void onScreenRotationChanged(boolean land) {
    }

    @Override
    public void onSetContentView() {
    }

    /**
     * 用户判断当前uuid是否是自己需要的
     */
    protected boolean accept(String uuid) {
        return TextUtils.equals(mUUID, uuid);
    }

    /**
     * 工作在非UI线程,可以简化rxjava的使用
     */
    protected void post(Runnable action) {
        HandlerThreadUtils.post(action);
    }

    public interface ResponseParser {
        /**
         * @param response 可能为BaseValue或者HashSet<BaseValue> 取决于消息类型,需要自己强转
         */
        void onParseResponse(DataPoint... response);
    }

    public interface LocalUDPMessageParser {
        void onParseLocalUDPMsg(RxEvent.LocalUdpMsg msg);
    }

    protected void registerSubscription(Subscription... subscriptions) {
        if (mSubscriptions == null) {
            synchronized (this) {
                if (mSubscriptions == null) {
                    mSubscriptions = new CompositeSubscription();
                }
            }
        }
        if (subscriptions != null) {
            for (Subscription subscription : subscriptions) {
                mSubscriptions.add(subscription);
            }
        }
    }

    /**
     * 不在主线程中请求数据,因为可能卡住
     */
    protected void robotGetData(String peer, ArrayList<JFGDPMsg> queryDps, int limit, boolean asc, int timeoutMs, long[] rs) {
        post(() -> {
            try {
                long seq = JfgCmdInsurance.getCmd().robotGetData(peer, queryDps, limit, asc, timeoutMs);
                mRequestSeqs.add(seq);
                if (rs != null) rs[0] = seq;
            } catch (JfgException e) {
                e.printStackTrace();
            }
        });
    }

    protected void robotGetData(String peer, ArrayList<JFGDPMsg> queryDps, int limit, boolean asc, int timeoutMs) {
        robotGetData(peer, queryDps, limit, asc, timeoutMs, null);
    }

}
