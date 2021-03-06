package com.cylan.jiafeigou.n.mvp.contract.setting;

import android.support.annotation.ColorInt;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;
import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;

import java.util.List;

/**
 * Created by hunt on 16-5-26.
 */

public interface TimezoneContract {

    interface View extends BaseView<Presenter> {
        void timezoneList(List<TimeZoneBean> list);

        void onNextTheme(@ColorInt int color);
    }

    interface Presenter extends BasePresenter {
        void onSearch(String content);
    }
}
