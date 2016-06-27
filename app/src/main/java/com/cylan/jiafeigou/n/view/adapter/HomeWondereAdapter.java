package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.MediaBean;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.List;

/**
 * Created by chen on 6/6/16.
 */


public class HomeWondereAdapter extends SuperAdapter<MediaBean> {


    private DeviceItemClickListener deviceItemClickListener;
    private DeviceItemLongClickListener deviceItemLongClickListener;


    public HomeWondereAdapter(Context context, List<MediaBean> items,
                              IMulItemViewType<MediaBean> mulItemViewType) {
        super(context, items, mulItemViewType);

    }

    public void setDeviceItemClickListener(DeviceItemClickListener deviceItemClickListener) {
        this.deviceItemClickListener = deviceItemClickListener;
    }

    public void setDeviceItemLongClickListener(DeviceItemLongClickListener deviceItemLongClickListener) {
        this.deviceItemLongClickListener = deviceItemLongClickListener;
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, MediaBean item) {

        int layoutId;
        if (viewType == 0) {
            layoutId = R.id.rLayout_picture_item_wonder;
        }else {
            layoutId = R.id.rLayout_vedio_item_wonder;
        }
        final View view = holder.getView(layoutId);
        if (view != null) {
            view.setTag(layoutPosition);
        }
        holder.setOnClickListener(layoutId, deviceItemClickListener);
        holder.setOnLongClickListener(layoutId, deviceItemLongClickListener);
        handleState(holder, item);
    }



    private void handleState(SuperViewHolder holder, MediaBean bean) {


        //时间
        holder.setText(R.id.tv_title_item_wonder, bean.curTime);

        //图标
        holder.setBackgroundResource(R.id.iv_src_item_wonder, R.drawable.bg_home_title_daytime);

        //来自摄像头
        holder.setText(R.id.tv_devicename_item_wonder,bean.srcFrom);
    }

    @Override
    protected IMulItemViewType<MediaBean> offerMultiItemViewType() {
        return new IMulItemViewType<MediaBean>() {
            @Override
            public int getViewTypeCount() {
                return 2;
            }

            @Override
            public int getItemViewType(int position, MediaBean mediaBean) {
                return mediaBean.mediaType;//0:image view  1:videoView
            }

            @Override
            public int getLayoutId(int viewType) {
                return viewType==0?R.layout.layout_item_picture_wonderful:R.layout.layout_item_vedio_wonderful;
            }
        };
    }

    public interface DeviceItemClickListener extends View.OnClickListener {

    }

    public interface DeviceItemLongClickListener extends View.OnLongClickListener {

    }
}
