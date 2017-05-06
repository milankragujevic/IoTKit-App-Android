package com.cylan.jiafeigou.rx;

import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Parcel;
import android.os.Parcelable;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDPMsgRet;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.JFGDoorBellCaller;
import com.cylan.entity.jniCall.JFGFeedbackInfo;
import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.entity.jniCall.JFGResult;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.module.HistoryFile;
import com.cylan.udpMsgPack.JfgUdpMsg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by cylan-hunt on 16-7-6.
 */
public class RxEvent {


    @Deprecated //账号状态不依据这个消息
    public static class OnlineStatusRsp {
        public boolean state;

        public OnlineStatusRsp(boolean state) {
            this.state = state;
        }
    }

    /**
     * 分享账号,列表响应
     */
    public static class GetShareListRsp {
    }

    public static class CloudLiveDelete {

    }

    /**
     * The type Result event.
     */
    public static class ResultEvent {
        /**
         * The constant JFG_RESULT_VERIFY_SMS.
         */
        public static final int JFG_RESULT_VERIFY_SMS = 0;
        /**
         * The constant JFG_RESULT_REGISTER.
         */
        public static final int JFG_RESULT_REGISTER = 1;
        /**
         * The constant JFG_RESULT_LOGIN.
         */
        public static final int JFG_RESULT_LOGIN = 2;
        /**
         * The constant JFG_RESULT_BINDDEV.
         */
        public static final int JFG_RESULT_BINDDEV = 3;

        /**
         * The constant JFG_RESULT_UNBINDDEV.
         */
        public static final int JFG_RESULT_UNBINDDEV = 4;


        /**
         * The constant JFG_RESULT_UPDATE_ACCOUNT.
         */
        public static final int JFG_RESULT_UPDATE_ACCOUNT = 5;

        /**
         * 删除好友的结果
         */
        public static final int JFG_RESULT_DEL_FRIEND = 6;
        /**
         * 同意添加好友的结果
         */
        public static final int JFG_RESULT_CONSENT_ADD_FRIEND = 7;

        /**
         * 设置好友备注名
         */
        public static final int JFG_RESULT_SET_FRIEND_MARKNAME = 8;
    }

    /**
     * The type Sms code result.
     */
    public static class SmsCodeResult {

        /**
         * The Error.
         */
        public int error;
        /**
         * The Token.
         */
        public String token;

        /**
         * Instantiates a new Sms code result.
         *
         * @param error the error
         * @param token the token
         */
        public SmsCodeResult(int error, String token) {
            this.error = error;
            this.token = token;
        }

        @Override
        public String toString() {
            return "SmsCodeResult{" +
                    "error=" + error +
                    ", token='" + token + '\'' +
                    '}';
        }
    }

    /**
     * {@link com.cylan.jfgapp.jni.JfgAppCallBack#OnSendSMSResult(int, String)}
     */
    public static final class ResultVerifyCode {
        public int code;

        public ResultVerifyCode(int code) {
            this.code = code;
        }
    }

    /**
     * 调用 {@link com.cylan.jfgapp.jni.JfgAppCmd#register(String, String, int, String)}
     */
    public static final class ResultRegister {
        public int code;

        public ResultRegister(int code) {
            this.code = code;
        }
    }

    public static final class ResultLogin {
        public int code;

        public ResultLogin(int code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return "ResultLogin{" +
                    "code=" + code +
                    '}';
        }
    }


    public static final class ResultUserLogin {
        public int code;

        public ResultUserLogin(int code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return "ResultUserLogin{" +
                    "code=" + code +
                    '}';
        }
    }

    public static final class ResultUpdateLogin {
        public int code;

        public ResultUpdateLogin(int code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return "ResultUserLogin{" +
                    "code=" + code +
                    '}';
        }
    }

    public static final class ResultBind {
        public int code;

        public ResultBind(int code) {
            this.code = code;
        }
    }


    public static final class ResultUnBind {
        public int code;
    }

    /**
     * 切换 “登陆” “注册”
     */
    public static final class SwitchBox {
//        public String account;

//        public SwitchBox(String account) {
//            this.account = account;
//        }
    }


    public static final class LoginPopBack {
        public String account;

        public LoginPopBack(String account) {
            this.account = account;
        }

    }

    public static final class ForgetPwdByMail {
        public String account;

        public ForgetPwdByMail(String account) {
            this.account = account;
        }
    }

    public static final class PageScrolled {
        public PageScrolled(int index) {
            this.index = index;
        }

        public int index;

    }

    /**
     * desc:获取好友列表类
     */
    public static final class GetFriendList {

        public int i;
        public ArrayList<JFGFriendAccount> arrayList;

        public GetFriendList(int i, ArrayList<JFGFriendAccount> arrayList) {
            this.i = i;
            this.arrayList = arrayList;
        }
    }

    /**
     * desc：获取添加请求类
     */
    public static final class GetAddReqList {

        public int i;

        public ArrayList<JFGFriendRequest> arrayList;

        public GetAddReqList(int i, ArrayList<JFGFriendRequest> arrayList) {
            this.i = i;
            this.arrayList = arrayList;
        }

    }

    /**
     * desc：获取到分享设备的信息
     */
    public static final class GetShareListCallBack {
        public int i;

        public ArrayList<JFGShareListInfo> arrayList;

        public GetShareListCallBack(int i, ArrayList<JFGShareListInfo> arrayList) {
            this.i = i;
            this.arrayList = arrayList;
        }
    }


    /**
     * 设备列表返回,粗糙数据,任然需要通过查询
     */
    public static final class DeviceRawList {
        public JFGDevice[] devices;

        public DeviceRawList(JFGDevice[] list) {
            this.devices = list;

        }
    }

    /**
     * 获取登录用户的信息
     */
    public static final class GetUserInfo {

        public JFGAccount jfgAccount;

        public GetUserInfo(JFGAccount jfgAccount) {
            this.jfgAccount = jfgAccount;
        }
    }

//    /**
//     * 获取到http请求的结果
//     */
//    public static final class GetHttpDoneResult {
//        public JFGMsgHttpResult jfgMsgHttpResult;
//
//        public GetHttpDoneResult(JFGMsgHttpResult jfgMsgHttpResult) {
//            this.jfgMsgHttpResult = jfgMsgHttpResult;
//        }
//    }

    public static final class LocalUdpMsg {
        //消息的时间,可以用来判断有效性.
        public long time;
        public String ip;
        public short port;
        public byte[] data;

        public LocalUdpMsg() {
        }

        public LocalUdpMsg(long time, String ip, short port, byte[] data) {
            this.time = time;
            this.ip = ip;
            this.port = port;
            this.data = data;
        }

        @Override
        public String toString() {
            return "LocalUdpMsg{" +
                    "ip='" + ip + '\'' +
                    ", port=" + port +
                    ", data=" + Arrays.toString(data) +
                    '}';
        }
    }

    /**
     * 分享设备的回调
     */
    public static final class ShareDeviceCallBack {

        public int requestId;

        public String cid;

        public String account;

        public ShareDeviceCallBack(int requestId, String cid, String account) {
            this.requestId = requestId;
            this.cid = cid;
            this.account = account;
        }
    }

    @Deprecated
    public static final class BindDeviceEvent {
        public int bindResult;
        public String uuid;

        public BindDeviceEvent(int jfgResult) {
            this.bindResult = jfgResult;
        }

        public BindDeviceEvent(int jfgResult, String uuid) {
            this.bindResult = jfgResult;
            this.uuid = uuid;
        }

        @Override
        public String toString() {
            return "BindDeviceEvent{" +
                    "bindResult=" + bindResult +
                    ", uuid='" + uuid + '\'' +
                    '}';
        }
    }

    public static final class UnBindDeviceEvent {
        public JFGResult jfgResult;

        public UnBindDeviceEvent(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    public static final class DeviceUnBindedEvent {
        public String uuid;

        public DeviceUnBindedEvent(String uuid) {
            this.uuid = uuid;
        }
    }

    /**
     * 检验邮箱是否注册过回调
     */
    public static final class CheckAccountCallback {
        public int i;

        public CheckAccountCallback(int i, String s, String s1, boolean b) {
            this.i = i;
            this.s = s;
            this.s1 = s1;
            this.b = b;
        }

        public String s;
        public String s1;
        public boolean b;
    }

    /**
     * 获取到已经分享的好友的回调
     */
    public static final class GetHasShareFriendCallBack {
        public int i;
        public ArrayList<JFGFriendAccount> arrayList;

        public GetHasShareFriendCallBack(int i, ArrayList<JFGFriendAccount> arrayList) {
            this.i = i;
            this.arrayList = arrayList;
        }
    }

    /**
     * 取消分享的回调
     */
    public static final class UnshareDeviceCallBack {
        public int i;
        public String cid;
        public String account;

        public UnshareDeviceCallBack(int i, String cid, String account) {
            this.i = i;
            this.cid = cid;
            this.account = account;
        }
    }

    /**
     * 获取设备列表
     */
    @Deprecated
    public static final class DeviceListRsp {
    }


    /**
     * 获取好友的信息回调
     */
    public static final class GetFriendInfoCall {
        public int i;

        public GetFriendInfoCall(int i, JFGFriendAccount jfgFriendAccount) {
            this.i = i;
            this.jfgFriendAccount = jfgFriendAccount;
        }

        public JFGFriendAccount jfgFriendAccount;
    }

//    /**
//     * 解绑设备
//     */
//    public static final class UnbindJFGDevice {
//        public String uuid;
//    }

//    /**
//     * 这个消息从{@link DataSourceService#OnRobotCountDataRsp(long, String, ArrayList)}
//     * 传到{@link }
//     */
//    public static final class UnreadCount {
//        public String uuid;
//        public long seq;
//        public ArrayList<JFGDPMsgCount> msgList;
//
//        public UnreadCount() {
//        }
//
//        public UnreadCount(String uuid, long seq, ArrayList<JFGDPMsgCount> counts) {
//            this.uuid = uuid;
//            this.seq = seq;
//            this.msgList = counts;
//        }
//    }


    /**
     * 历史录像数据响应
     */
    public static final class JFGHistoryVideoParseRsp {
        public String uuid;
        public ArrayList<HistoryFile> historyFiles;//可能是当天的数据

        public JFGHistoryVideoParseRsp(String uuid) {
            this.uuid = uuid;
        }

        public JFGHistoryVideoParseRsp setTimeList(ArrayList<Long> dateList) {
            return this;
        }

        public JFGHistoryVideoParseRsp setFileList(ArrayList<HistoryFile> historyFiles) {
            this.historyFiles = historyFiles;
            return this;
        }
    }

    /**
     * 系统反馈回复
     */
    public static final class GetFeedBackRsp {
        public int i;
        public ArrayList<JFGFeedbackInfo> arrayList;

        public GetFeedBackRsp(int i, ArrayList<JFGFeedbackInfo> arrayList) {
            this.i = i;
            this.arrayList = arrayList;
        }
    }


    public static final class BellCallEvent {

        public JFGDoorBellCaller caller;
        public boolean isFromLocal = false;
        public LocalUdpMsg msg;

        public BellCallEvent() {
        }

        public BellCallEvent(JFGDoorBellCaller jfgDoorBellCaller) {
            this.caller = jfgDoorBellCaller;
        }

        public BellCallEvent(JFGDoorBellCaller caller, boolean isFromLocal) {
            this.caller = caller;
            this.isFromLocal = isFromLocal;
        }
    }


    /**
     * 修改密码的返回
     */
    public static final class ChangePwdBack {
        public JFGResult jfgResult;

        public ChangePwdBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 修改密码的返回
     */
    public static final class ResetPwdBack {
        public JFGResult jfgResult;

        public ResetPwdBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 添加亲友的返回
     */
    public static final class AddFriendBack {
        public JFGResult jfgResult;

        public AddFriendBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 删除亲友的返回
     */
    public static final class DelFriendBack {
        public JFGResult jfgResult;

        public DelFriendBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 同意添加亲友的返回
     */
    public static final class ConsentAddFriendBack {
        public JFGResult jfgResult;

        public ConsentAddFriendBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 同意添加亲友的返回
     */
    public static final class SetFriendAliasBack {
        public JFGResult jfgResult;

        public SetFriendAliasBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 发送反馈的返回
     */
    public static final class SendFeekBack {
        public JFGResult jfgResult;

        public SendFeekBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 三方绑定手机设置密码时的返回
     */
    public static final class OpenLogInSetPwdBack {
        public JFGResult jfgResult;

        public OpenLogInSetPwdBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 删除好友添加请求
     */
    public static final class DeleteAddReqBack {
        public JFGResult jfgResult;

        public DeleteAddReqBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 检测是否已注册回调
     */
    public static final class CheckRegsiterBack {
        public JFGResult jfgResult;

        public CheckRegsiterBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    /**
     * 设置设备别名
     */
    public static final class SetAlias {
        public JFGResult result;

        public SetAlias(JFGResult result) {
            this.result = result;
        }
    }

    public static class AppHideEvent {
    }

    @Deprecated
    public static class EFamilyMsgpack {
        public int msgId;
        public byte[] data;
    }

    public static class CallResponse {
        public CallResponse(boolean self) {
            this.self = self;
        }

        public boolean self;
    }

//    public static class GetDataResponse {
//        public long seq;
//        public long msgId;
//        public boolean changed;
//    }

//    @Deprecated
//    public static class ParseResponseCompleted {
//        public long seq;
//        public String uuid;
//    }

    public static class DeviceSyncRsp {

        public DeviceSyncRsp setUuid(String uuid, ArrayList<Long> idList, ArrayList<JFGDPMsg> dpList) {
            this.uuid = uuid;
            this.idList = idList;
            this.dpList = dpList;
            return this;
        }

        public ArrayList<JFGDPMsg> dpList;
        public ArrayList<Long> idList;
        public String uuid;
    }

    public static class DeleteDataRsp {
        public long seq;
        public String peer;
        public int resultCode;

        public DeleteDataRsp(long l, String s, int i) {
            this.seq = l;
            this.peer = s;
            this.resultCode = i;
        }
    }

    public static class ErrorRsp extends RuntimeException {
        public int code;

        public ErrorRsp(int code) {
            this.code = code;
        }
    }

    public static class DeleteWonder {
        public int position;
    }

    public static class DeleteWonderRsp {
        public boolean success;
        public int position;

        public DeleteWonderRsp(boolean b, int position) {
            this.position = position;
            success = b;
        }
    }

//    public static class SdcardClearReqRsp {
//        public long seq;
//
//        public SdcardClearReqRsp(long seq, ArrayList<JFGDPMsgRet> arrayList) {
//            this.seq = seq;
//            this.arrayList = arrayList;
//        }
//
//        public ArrayList<JFGDPMsgRet> arrayList;
//
//    }


    public static class CheckDevVersionRsp implements Parcelable {
        public long seq;
        public boolean hasNew;
        public long fileSize;
        public String url;
        public String version;
        public String tip;
        public String md5;
        public String fileDir;
        public String fileName;

        public CheckDevVersionRsp setSeq(long seq) {
            this.seq = seq;
            return this;
        }

        public CheckDevVersionRsp(boolean hasNew, String url, String version, String tip, String md5) {
            this.hasNew = hasNew;
            this.url = url;
            this.version = version;
            this.tip = tip;
            this.md5 = md5;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.seq);
            dest.writeByte(this.hasNew ? (byte) 1 : (byte) 0);
            dest.writeString(this.url);
            dest.writeString(this.version);
            dest.writeString(this.tip);
            dest.writeString(this.md5);
        }

        protected CheckDevVersionRsp(Parcel in) {
            this.seq = in.readLong();
            this.hasNew = in.readByte() != 0;
            this.url = in.readString();
            this.version = in.readString();
            this.tip = in.readString();
            this.md5 = in.readString();
        }

        public static final Creator<CheckDevVersionRsp> CREATOR = new Creator<CheckDevVersionRsp>() {
            @Override
            public CheckDevVersionRsp createFromParcel(Parcel source) {
                return new CheckDevVersionRsp(source);
            }

            @Override
            public CheckDevVersionRsp[] newArray(int size) {
                return new CheckDevVersionRsp[size];
            }
        };
    }

    public static class LiveResponse {
        public boolean success;
        public Object response;

        public LiveResponse(Object disconnect, boolean success) {
            this.success = false;
            this.response = disconnect;
        }

        public LiveResponse(Object resolution) {
            this.success = true;
            this.response = resolution;
        }
    }

    public static class NetWorkChangeIntent {
        public boolean available;
        public Intent intent;
    }

    public static class NetConnectionEvent {
        public boolean isOnLine = false;
        public boolean available;
        public NetworkInfo mobile;
        public NetworkInfo wifi;

        public NetConnectionEvent(boolean available) {
            this.available = available;
        }
    }


    public static final class SetFriendMarkNameBack {
        public JFGResult jfgResult;

        public SetFriendMarkNameBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    public static final class RessetAccountBack {
        public JFGResult jfgResult;

        public RessetAccountBack(JFGResult jfgResult) {
            this.jfgResult = jfgResult;
        }
    }

    public static class SetDataRsp {
        public long seq;
        public String uuid;
        public ArrayList<JFGDPMsgRet> rets;

        public SetDataRsp(long l, String uuid, ArrayList<JFGDPMsgRet> arrayList) {
            this.seq = l;
            this.uuid = uuid;
            this.rets = arrayList;
        }

        public SetDataRsp setUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }
    }

    public static class ClearDataEvent {
        public int msgId;

        public ClearDataEvent(int msgId) {
            this.msgId = msgId;
        }
    }

    public static class ThirdLoginTab {
        public boolean isThird;

        public ThirdLoginTab(boolean isThird) {
            this.isThird = isThird;
        }
    }

    public static class ShowWonderPageEvent {
    }

    public static final class ShouldCheckPermission {
    }

    public static class DevicesArrived {
        public List<Device> devices;

        public DevicesArrived(List<Device> devices) {
            this.devices = devices;
        }
    }

    public static class AccountArrived {
        public Account account;
        public JFGAccount jfgAccount;

        public AccountArrived(Account account) {
            this.account = account;
        }
    }

    public static final class PwdHasResetEvent {
        public PwdHasResetEvent(int code) {
            this.code = code;
        }

        public int code;
    }

    //DataSouManager 专用,其他场景不要用这个类
    public static final class SerializeCacheAccountEvent {

        public final JFGAccount account;

        public SerializeCacheAccountEvent(JFGAccount jfgAccount) {
            this.account = jfgAccount;
        }
    }

    //DataSouManager 专用,其他场景不要用这个类
    public static final class SerializeCacheDeviceEvent {

        public final JFGDevice[] devices;

        public SerializeCacheDeviceEvent(JFGDevice[] jfgDevices) {
            this.devices = jfgDevices;
        }
    }

    //DataSouManager 专用,其他场景不要用这个类
    public static final class SerializeCacheGetDataEvent {

        public final RobotoGetDataRsp getDataRsp;

        public SerializeCacheGetDataEvent(RobotoGetDataRsp robotoGetDataRsp) {
            this.getDataRsp = robotoGetDataRsp;
        }
    }

    //DataSouManager 专用,其他场景不要用这个类
    public static final class SerializeCacheSyncDataEvent {

        public final boolean b;
        public final String s;
        public final ArrayList<JFGDPMsg> arrayList;

        public SerializeCacheSyncDataEvent(boolean b, String s, ArrayList<JFGDPMsg> arrayList) {
            this.b = b;
            this.s = s;
            this.arrayList = arrayList;
        }
    }

//    /**
//     * {@link com.cylan.jfgapp.interfases.AppCallBack#OnRobotSyncData(boolean, String, ArrayList)}
//     * {@link com.cylan.jiafeigou.dp.DpMsgMap#ID_505_CAMERA_ALARM_MSG}
//     * {@link com.cylan.jiafeigou.dp.DpMsgMap#ID_512_CAMERA_ALARM_MSG_V3}
//     * {@link com.cylan.jiafeigou.dp.DpMsgMap#ID_401_BELL_CALL_STATE}
//     */
//    public static final class ForSystemNotification {
//
//        public final boolean b;
//        public final String s;
//        public final ArrayList<JFGDPMsg> arrayList;
//
//        public ForSystemNotification(boolean b, String s, ArrayList<JFGDPMsg> arrayList) {
//            this.b = b;
//            this.s = s;
//            this.arrayList = arrayList;
//        }
//    }

    /**
     * 从我的界面登录标记
     */
    public static final class LoginMeTab {
        public LoginMeTab(boolean b) {
            this.b = b;
        }

        public boolean b;
    }

    /**
     * 从别的终端密码修改后标记
     */
    public static final class LogOutByResetPwdTab {
        public LogOutByResetPwdTab(boolean b) {
            this.b = b;
        }

        public boolean b;
    }

    public static final class PanoramaConnection {

    }

    public static final class ClientCheckVersion {
        public int ret;
        public String result;
        public int forceUpgrade;//强制升级

        public ClientCheckVersion(int ret, String result, int forceUpgrade) {
            this.ret = ret;
            this.result = result;
            this.forceUpgrade = forceUpgrade;
        }

        @Override
        public String toString() {
            return "ClientCheckVersion{" +
                    "ret=" + ret +
                    ", result='" + result + '\'' +
                    ", forceUpgrade=" + forceUpgrade +
                    '}';
        }
    }

    public static final class ClientUpdateEvent {
        public long currentByte;
        public long totalByte;
        public int state;
        public Throwable throwable;
        public int forceUpdate;

        public ClientUpdateEvent setCurrentByte(long currentByte) {
            this.currentByte = currentByte;
            return this;
        }

        public ClientUpdateEvent setTotalByte(long totalByte) {
            this.totalByte = totalByte;
            return this;
        }

        public ClientUpdateEvent setState(int state) {
            this.state = state;
            return this;
        }

        public ClientUpdateEvent setThrowable(Throwable throwable) {
            this.throwable = throwable;
            return this;
        }

        public ClientUpdateEvent setForceUpdate(int forceUpdate) {
            this.forceUpdate = forceUpdate;
            return this;
        }
    }

    public static final class SetWifiAck {
        public JfgUdpMsg.DoSetWifiAck data;

        public SetWifiAck(JfgUdpMsg.DoSetWifiAck data) {
            this.data = data;
        }
    }

    public static class NeedUpdateGooglePlayService {

    }

    public static class VideoLoadingEvent {
        public final boolean slow;

        public VideoLoadingEvent(boolean slow) {
            this.slow = slow;
        }
    }
//
//    public static final class FirmwareUpdate {
//        public String uuid;
//
//        public FirmwareUpdate(String uuid) {
//            this.uuid = uuid;
//        }
//    }
}
