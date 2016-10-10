package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseBean;
import com.cylan.jiafeigou.n.view.cloud.LayoutHandler;
import com.cylan.jiafeigou.n.view.cloud.LayoutIdMapCache;
import com.cylan.jiafeigou.n.view.cloud.ViewTypeMapCache;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/10/8
 * 描述：
 */
public class CloudLiveMesgListAdapter extends SuperAdapter<CloudLiveBaseBean> {

    private ViewTypeMapCache viewTypeCache;
    private LayoutIdMapCache layoutIdMapCache;
    private LayoutHandler layoutHandler;

    public CloudLiveMesgListAdapter(Context context, List items, IMulItemViewType<CloudLiveBaseBean> mulItemViewType) {
        super(context, items, mulItemViewType);
        layoutHandler = new LayoutHandler();
    }

    public void setViewTypeCache(ViewTypeMapCache viewTypeCache) {
        this.viewTypeCache = viewTypeCache;
        layoutHandler.setViewTypeCache(viewTypeCache);
    }

    public void setLayoutIdMapCache(LayoutIdMapCache layoutIdMapCache) {
        this.layoutIdMapCache = layoutIdMapCache;
        layoutHandler.setLayoutIdMapCache(layoutIdMapCache);
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, CloudLiveBaseBean item) {
        layoutHandler.handleLayout(holder, viewType, layoutPosition, item);
    }

    @Override
    protected IMulItemViewType<CloudLiveBaseBean> offerMultiItemViewType() {
        return new IMulItemViewType<CloudLiveBaseBean>() {
            @Override
            public int getViewTypeCount() {
                return viewTypeCache.getSize();
            }

            @Override
            public int getItemViewType(int position, CloudLiveBaseBean baseBean) {
                return viewTypeCache.getType(baseBean.data.getClass());
            }

            @Override
            public int getLayoutId(int viewType) {
                return layoutIdMapCache.getLayoutId(viewType);
            }
        };
    }
}