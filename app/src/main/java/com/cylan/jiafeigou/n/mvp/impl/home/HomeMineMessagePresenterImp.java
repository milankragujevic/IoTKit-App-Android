package com.cylan.jiafeigou.n.mvp.impl.home;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.db.DataBaseUtil;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineMessageContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MineMessageBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.db.DbManager;
import com.cylan.jiafeigou.support.db.ex.DbException;
import com.cylan.jiafeigou.support.db.sqlite.WhereBuilder;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class HomeMineMessagePresenterImp extends AbstractPresenter<HomeMineMessageContract.View> implements HomeMineMessageContract.Presenter {

    private boolean hasNewMesg;
    private DbManager dbManager;
    private ArrayList<MineMessageBean> results = new ArrayList<MineMessageBean>();
    private long seq;

    public HomeMineMessagePresenterImp(HomeMineMessageContract.View view, boolean hasNewMesg) {
        super(view);
        view.setPresenter(this);
//        this.hasNewMesg = hasNewMesg;
        this.hasNewMesg = true;
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                getAccount(),
                getMesgDpDataCallBack()

        };
    }

    /**
     * 加载消息数据
     */
    @Override
    public void initMesgData(String account) {
        if (hasNewMesg) {
            getMesgDpData(account);
        } else {
            handlerDataResult(findAllFromDb());
        }
    }

    /**
     * 处理数据的显示
     *
     * @param list
     */
    private void handlerDataResult(ArrayList<MineMessageBean> list) {
        if (getView() != null) {
            if (list.size() != 0) {
                getView().hideNoMesgView();
                getView().initRecycleView(list);
            } else {
                getView().showNoMesgView();
                getView().initRecycleView(new ArrayList<>());
            }
        }
    }

    /**
     * 拿到数据库的操作对象
     *
     * @return
     */
    @Override
    public Subscription getAccount() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetUserInfo.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetUserInfo>() {
                    @Override
                    public void call(RxEvent.GetUserInfo account) {
                        if (account != null) {
                            // 加载数据库数据
                            dbManager = DataBaseUtil.getInstance(account.jfgAccount.getAccount()).dbManager;
                            initMesgData(account.jfgAccount.getAccount());
                            markMesgHasRead();
                        }
                    }
                }, AppLogger::e);
    }

    /**
     * 获取到本地数据库中的所有消息记录
     *
     * @return
     */
    @Override
    public ArrayList<MineMessageBean> findAllFromDb() {
        ArrayList<MineMessageBean> tempList = new ArrayList<>();
        if (dbManager != null) {
            try {
                List<MineMessageBean> allData = dbManager.findAll(MineMessageBean.class);
                if (allData != null) {
                    tempList.addAll(allData);
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
        return sortAddReqList(tempList);
    }

    /**
     * 清空本地消息记录
     */
    @Override
    public void clearRecoard() {
        try {
            dbManager.delete(MineMessageBean.class);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 消息保存到数据库
     *
     * @param bean
     */
    @Override
    public void saveIntoDb(MineMessageBean bean) {
        try {
            dbManager.save(bean);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }


    /**
     * Dp获取到消息记录
     */
    @Override
    public void getMesgDpData(String account) {
        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        try {
                            JFGDPMsg msg1 = new JFGDPMsg(601, System.currentTimeMillis());
                            JFGDPMsg msg4 = new JFGDPMsg(701, System.currentTimeMillis());
                            ArrayList<JFGDPMsg> params = new ArrayList<>();
                            params.add(msg1);
                            params.add(msg4);
                            seq = BaseApplication.getAppComponent().getCmd().robotGetData("", params, 100, false, 0);
                            AppLogger.d("getMesgDpData:" + seq);
                        } catch (Exception e) {
                            AppLogger.e("getMesgDpData:" + e.getLocalizedMessage());
                            e.printStackTrace();
                        }
                    }
                }, throwable -> {
                    AppLogger.e("getMesgDpData" + throwable.getLocalizedMessage());
                });
    }

    /**
     * Dp获取消息记录的回调
     *
     * @return
     */
    @Override
    public Subscription getMesgDpDataCallBack() {
        return RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .subscribeOn(Schedulers.io())
                .filter(rsp -> rsp != null && rsp.seq == seq)
                .first()
                .map(robotoGetDataRsp -> {
                    if (results.size() != 0)
                        results.clear();
                    results.addAll(convertData(robotoGetDataRsp));
//                            markMesgHasRead();
                    return results;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (list.size() != 0) {
                        handlerDataResult(list);
                    } else {
                        getView().showNoMesgView();
                    }
                }, AppLogger::e);
    }


    /**
     * 解析转换数据
     *
     * @param robotoGetDataRsp
     */
    private ArrayList<MineMessageBean> convertData(RobotoGetDataRsp robotoGetDataRsp) {
        MineMessageBean bean;
        clearRecoard();
        ArrayList<MineMessageBean> results = new ArrayList<MineMessageBean>();
        for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : robotoGetDataRsp.map.entrySet()) {
            if (entry.getValue() == null) continue;
            for (JFGDPMsg dp : entry.getValue()) {
                bean = new MineMessageBean();
                bean.type = entry.getKey();
                try {
                    if (bean.type == 701) {
                        DpMsgDefine.DPSystemMesg sysMesg = DpUtils.unpackData(dp.packValue, DpMsgDefine.DPSystemMesg.class);
                        if (sysMesg == null) continue;
                        bean.name = sysMesg.content.trim();
                        bean.content = sysMesg.title.trim();
                        bean.time = dp.version + "";
                        bean.isDone = 0;
                    } else {
                        DpMsgDefine.DPMineMesg mesg = DpUtils.unpackData(dp.packValue, DpMsgDefine.DPMineMesg.class);
                        if (mesg == null) continue;
                        bean.name = mesg.account.trim();
                        bean.isDone = mesg.isDone ? 1 : 0;
                        bean.content = mesg.cid.trim();
                        bean.time = dp.version + "";
                        bean.sn = mesg.sn;
                    }
                    results.add(bean);
                    saveIntoDb(bean);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sortAddReqList(results);
    }

    public ArrayList<MineMessageBean> sortAddReqList(ArrayList<MineMessageBean> list) {
        Comparator<MineMessageBean> comparator = new Comparator<MineMessageBean>() {
            @Override
            public int compare(MineMessageBean lhs, MineMessageBean rhs) {
                long oldTime = Long.parseLong(rhs.time);
                long newTime = Long.parseLong(lhs.time);
                if (oldTime == newTime)
                    return 0;
                if (oldTime < newTime)
                    return -1;
                return 1;
            }
        };
        Collections.sort(list, comparator);
        return list;
    }

    @Override
    public void deleteServiceMsg(long type, long version) {
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        try {
                            ArrayList<JFGDPMsg> list = new ArrayList<JFGDPMsg>();
                            JFGDPMsg msg = new JFGDPMsg(type, version);
                            list.add(msg);
                            long req = BaseApplication.getAppComponent().getCmd().robotDelData("", list, 0);
                            AppLogger.d("deleteServiceMsg:" + req);
                        } catch (JfgException e) {
                            AppLogger.e("deleteServiceMsg:" + e.getLocalizedMessage());
                            e.printStackTrace();
                        }
                    }
                }, throwable -> {
                    AppLogger.e("deleteServiceMsg:" + throwable.getLocalizedMessage());
                });
    }

    @Override
    public Subscription deleteMsgBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeleteDataRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deleteDataRspClass -> {
                    if (getView() != null) getView().deleteMesgReuslt(deleteDataRspClass);
                }, AppLogger::e);
    }

    @Override
    public void deleteOneItem(MineMessageBean bean) {
        Observable.just(null)
                .subscribeOn(Schedulers.io())
                .subscribe(o -> {
                    try {
                        dbManager.delete(MineMessageBean.class, WhereBuilder.b("name", "=", bean.name).and("startTime", "=", bean.time));
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                }, AppLogger::e);
    }

    @Override
    public void markMesgHasRead() {
        Observable.just(null)
                .observeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    try {
                        ArrayList<JFGDPMsg> list = new ArrayList<JFGDPMsg>();
                        JFGDPMsg msg1 = new JFGDPMsg(1101L, 0);
                        JFGDPMsg msg2 = new JFGDPMsg(1103L, 0);
                        JFGDPMsg msg3 = new JFGDPMsg(1104L, 0);
                        msg1.packValue = DpUtils.pack(0);
                        msg2.packValue = DpUtils.pack(0);
                        msg3.packValue = DpUtils.pack(0);
                        list.add(msg1);
                        list.add(msg2);
                        list.add(msg3);
                        long req = BaseApplication.getAppComponent().getCmd().robotSetData("", list);
                        AppLogger.d("mine_markHasRead:" + req);
                    } catch (JfgException e) {
                        AppLogger.e("mine_markHasRead:" + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                }, AppLogger::e);
    }


}
