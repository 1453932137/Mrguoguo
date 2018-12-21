package com.css.ydoa.ui.activity.base;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.RelativeLayout;

import com.css.ydoa.R;


/**
 * Created by Shievy on 2017/4/12.
 */

public class BaseYdbgActivity extends FragmentActivity {
    static RelativeLayout network_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        network_title = (RelativeLayout) this.findViewById(R.id.network_title);
    }
}
