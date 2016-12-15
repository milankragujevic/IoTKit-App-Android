package com.cylan.jiafeigou.n.view.mine;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineUserInfoLookBigHeadContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineUserInfoLookBigHeadPresenterImpl;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/2
 * 描述：
 */
public class MineUserInfoLookBigHeadFragment extends Fragment implements MineUserInfoLookBigHeadContract.View {

    @BindView(R.id.iv_userinfo_big_image)
    ImageView ivUserinfoBigImage;

    private boolean loadResult = false;

    private MineUserInfoLookBigHeadContract.Presenter presenter;
    private String iamgeUrl;

    public static MineUserInfoLookBigHeadFragment newInstance(Bundle bundle) {
        MineUserInfoLookBigHeadFragment fragment = new MineUserInfoLookBigHeadFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_userinfo_lookbigimagehead, container, false);
        ButterKnife.bind(this, view);
        getArgumentData();
        initPresenter();
        loadBigImage(iamgeUrl);
        return view;
    }

    /**
     * 获取传递过来的参数
     */
    private void getArgumentData() {
        Bundle arguments = getArguments();
        iamgeUrl = arguments.getString("imageUrl");
    }

    private void loadBigImage(String url) {
        Glide.with(getContext())
                .load(url)
                .asBitmap()
                .error(R.drawable.icon_mine_head_normal)
                .centerCrop()
                .into(new BitmapImageViewTarget(ivUserinfoBigImage) {
                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                    }

                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        super.onResourceReady(resource, glideAnimation);
                        loadResult = true;
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        loadResult = false;
                        ToastUtil.showNegativeToast(getString(R.string.Item_LoadFail));
                    }
                });
    }

    private void initPresenter() {
        presenter = new MineUserInfoLookBigHeadPresenterImpl(this);
    }

    @OnClick(R.id.iv_userinfo_big_image)
    public void onClick() {
        if (loadResult) {
            getFragmentManager().popBackStack();
        } else {
            loadBigImage(iamgeUrl);
        }
    }

    @Override
    public void showLoadImageProgress() {
        LoadingDialog.showLoading(getFragmentManager(),getString(R.string.LOADING));
    }

    @Override
    public void hideLoadImageProgress() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    @Override
    public void setPresenter(MineUserInfoLookBigHeadContract.Presenter presenter) {

    }
}
