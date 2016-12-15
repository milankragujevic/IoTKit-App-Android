package com.cylan.jiafeigou.n.mvp.contract.mine;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.UserInfoBean;

import rx.Subscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/1
 * 描述：
 */
public interface MineInfoContract {

    interface View extends BaseView<Presenter> {

        void initPersonalInformation(JFGAccount bean);          //初始化显示个人信息

        void jump2SetEmailFragment();                           //更改邮箱

        void showLogOutDialog();                                //退出登录提示框

    }


    interface Presenter extends BasePresenter {

        void bindPersonEmail();                 //绑定邮箱
        /**
         * 退出登录
         */
        void logOut();

        /**
         * 检查文件是否存在
         * @param path
         * @return
         */
        String checkFileExit(String path);

        /**
         * 检测是否存在相机
         * @return
         */
        boolean checkHasCamera();

        /**
         * 检测相机是否可用
         * @return
         */
        boolean cameraIsCanUse();

        /**
         * 检测相机的权限
         * @return
         */
        boolean checkCameraPermission();

        /**
         * 检测存储权限
         * @return
         */
        boolean checkExternalStorePermission();
        /**
         * 获取到用户信息
         */
        Subscription getAccount();

        /**
         * 判断是否是三方登录
         * @return
         */
        boolean checkOpenLogin();

    }

}
