package com.cylan.jiafeigou.n.view.home;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomePageCoverFragment extends IBaseFragment {

    Unbinder unbinder;
    @BindView(R.id.v_guide)
    ImageView vGuide;

    public HomePageCoverFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_page_cover, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

//    @OnClick(R.id.v_home_cover,R.id.v_guide)
//    public void click2Dismiss() {
//        getActivity().getSupportFragmentManager().popBackStack();
//    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ViewUtils.setViewMarginStatusBar(vGuide);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.tv_pop_content, R.id.v_home_cover})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_pop_content:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.v_home_cover:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
        }
    }
}
