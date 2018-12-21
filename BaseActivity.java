package com.css.ydoa.ui.activity.base;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.css.ydoa.R;
import com.css.ydoa.common.base.dialog.BaseAlertDialog;
import com.css.ydoa.common.base.dialog.BaseLoadingDialog;
import com.css.ydoa.common.base.dialog.SweetAlert.AnimAlertDialog;
import com.css.ydoa.common.base.dialog.SweetAlert.AnimDialogHelper;
import com.css.ydoa.common.model.GlobalVar;
import com.css.ydoa.common.utils.ExitUtils;
import com.css.ydoa.common.utils.PbUtils;
import com.css.ydoa.common.utils.ToastUtils;
import com.css.ydoa.common.utils.http.ConnectionChangeReceiver;
import com.css.ydoa.common.utils.http.HttpClientUtils;
import com.zhy.autolayout.AutoRelativeLayout;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.event.LoginStateChangeEvent;

/**
 * 基类Activity
 * 封装了统一样式的Tab，以及ActionBar
 */
public class BaseActivity extends BaseYdbgActivity {
    private LinearLayout mActionBar;
    protected RelativeLayout rr_title;
    protected TextView mActionBarTitle;
    public TextView txt_left;
    protected ImageView mBackBtn;
    protected ImageView mRightImage;
    private String mActionBarTitleStr;
    protected Context mContext;
    static RelativeLayout network_title;
    protected LinearLayout title_s;
    protected TextView oATextView;
    //    搜索框的父控件
    private AutoRelativeLayout sou_ll;
    //    搜索输入框
    private EditText sou_et;
    private TextView base_pe_sou_tv;
    private LinearLayout mErrorLayout;
    private LinearLayout mDataNullLayout;
    private TextView mChongShiTxt;

    //  通知公告 title
    private LinearLayout base_tzgg_title;
    private TextView base_tzgg_title_left;
    private TextView base_tzgg_title_right;
    private TextView tv_ti_shi;

    private ConnectionChangeReceiver myReceiver;

    private BaseLoadingDialog lodingDialog, lodingDialogmsg;

    public int color;//换肤，保存的颜色 子类部分设置颜色时使用

    public Typeface mTfRegular;
    protected Typeface mTfLight;

    /**
     * 获取右上角的 TextView
     */
    public TextView getRightTv() {
        oATextView.setVisibility(View.VISIBLE);
        return oATextView;
    }

    /**
     * 返回左上角返回键布局
     */
    public ImageView getmBackBtn() {
        return mBackBtn;
    }

    /*
       *
       * JMessageClient.NOTI_MODE_DEFAULT 显示通知，有声音，有震动。
       * JMessageClient.NOTI_MODE_NO_SOUND 显示通知，无声音，有震动。
       * JMessageClient.NOTI_MODE_NO_VIBRATE 显示通知，有声音，无震动。
       * JMessageClient.NOTI_MODE_SILENCE 显示通知，无声音，无震动。
       * JMessageClient.NOTI_MODE_NO_NOTIFICATION 不显示通知。
       * */


    /**
     * 通知公告的可见性控制
     *
     * @param left 控制右边文本的状况
     */

    public void setTzggZt(String left) {
        mActionBarTitle.setVisibility(View.GONE);
        base_tzgg_title.setVisibility(View.VISIBLE);
        base_tzgg_title_right.setText(left);
    }

    public TextView getTzggLeft() {
        return base_tzgg_title_left;
    }

    public TextView getTzggRight() {
        return base_tzgg_title_right;
    }

    /**
     * 隐藏左上角回退图片，显示左上角 TextView，并返回左上角 TextView
     *
     * @return
     */
    public TextView getLeftTv() {
        mBackBtn.setVisibility(View.GONE);
        txt_left.setVisibility(View.VISIBLE);
        return txt_left;
    }

    /**
     * 获取右上角的 ImageView
     *
     * @return
     */
    public ImageView getRightIv() {
        return mRightImage;
    }

    //    返回文本编辑框布局
    public EditText getEditText() {
        return sou_et;
    }
    //    返回文本框布局
    public TextView getTextView() {
        return base_pe_sou_tv;
    }
    //    返回文本编辑框布局
    public RelativeLayout getSsRelativeLayout() {
        return sou_ll;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExitUtils.activityList.add(this);
        this.setContentView(R.layout.pe_activity_base);
        getWindow().setSoftInputMode(   WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        SharedPreferences sps = getSharedPreferences("Theme", Context.MODE_PRIVATE);
        color = sps.getInt("color", 0);
        if (color == 0) {
            color = 0xFF0070EA;
        }

        rr_title = (RelativeLayout) this.findViewById(R.id.rr_title);
        title_s = (LinearLayout) findViewById(R.id.title_tv_qwe);
        base_tzgg_title = (LinearLayout) findViewById(R.id.base_tzgg_title);
        base_tzgg_title_left = (TextView) findViewById(R.id.base_tzgg_title_left);
        base_tzgg_title_right = (TextView) findViewById(R.id.base_tzgg_title_right);
        sou_ll = (AutoRelativeLayout) findViewById(R.id.pe_base_sou_ll_et);
        base_pe_sou_tv = (TextView) findViewById(R.id.base_pe_sou_tv);
        sou_et = (EditText) findViewById(R.id.base_pe_sou_et);
        tv_ti_shi = (TextView) findViewById(R.id.tv_ti_shi);
        //透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            //获取到状态栏的高度
            int statusHeight = getStatusBarHeight();
            //动态的设置隐藏布局的高度
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusHeight);
            if (title_s != null) {
                title_s.setLayoutParams(lp);
                title_s.setVisibility(View.VISIBLE);
            }
        }

        network_title = (RelativeLayout) this.findViewById(R.id.network_title);
        network_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 1);

            }
        });
        configureActionBar();
//        JMessageClient.registerEventReceiver(this);
    }


    private void configureActionBar() {
        try {
            mContext = this;
            mActionBar = (LinearLayout) this.findViewById(R.id.ActionBar);
            mActionBarTitle = (TextView) this.findViewById(R.id.title);

            network_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 1);

                }
            });
            mErrorLayout = (LinearLayout) findViewById(R.id.ll_wang_luo_jia_zai_shi_bai);
            mDataNullLayout = (LinearLayout) findViewById(R.id.ll_no_data);
            mChongShiTxt = (TextView) this.findViewById(R.id.tv_chong_shi);
            mRightImage = (ImageView) this.findViewById(R.id.msgText);
            oATextView = (TextView) this.findViewById(R.id.tv_oaright_txt);
            txt_left = (TextView) this.findViewById(R.id.txt_left);
            mBackBtn = (ImageView) this.findViewById(R.id.back);
            mBackBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        } catch (Exception e) {
            //这里使用tryCatch处理NewMainActivity中的设置ActionBar为Gone的处理
        }
    }

    /**
     * 填充内容
     */
    protected void inflateContentView(int layoutResource) {
        ViewGroup contentView = (ViewGroup) this.findViewById(R.id.ContentView);
        this.getLayoutInflater().inflate(layoutResource, contentView);
    }

    /**
     * 隐藏导航栏
     */
    protected void hideActionBar() {
        if (mActionBar != null) {
            mActionBar.setVisibility(View.GONE);
            title_s.setVisibility(View.GONE);
        }
    }

    /**
     * 隐藏回退按钮
     */
    public void hideBackBtn() {
        if (mBackBtn != null) {
            mBackBtn.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 隐藏回退按钮
     */
    protected void showBackBtn() {
        if (mBackBtn != null) {
            mBackBtn.setVisibility(View.VISIBLE);
        }
    }


    //没网络
    public static void network() {
        network_title.setVisibility(View.VISIBLE);
    }

    //有网络
    public static void networkgone() {
        if (network_title != null) {
            network_title.setVisibility(View.GONE);
        }
    }

    /**
     * 改变标题
     */
    public void changeTitle(String title) {
        if (mActionBarTitle != null) {
            mActionBarTitleStr = title;
            mActionBarTitle.setText(mActionBarTitleStr);
        }
    }

    /**
     * 获取actionbar上面的textview
     *
     * @return
     */
    public TextView getTitleView() {
        return mActionBarTitle;
    }


    private void onBackBtnClicked() {
    }


    /**
     * 跳转Activity
     */
    public void nextActivity(Class<?> cls) {
        this.nextActivity(cls, false);
    }

    protected void nextActivity(Class<?> cls, boolean finishCurrent) {
        this.nextActivity(cls, finishCurrent, new Bundle());
    }

    protected void nextActivity(Class<?> cls, Bundle bundle) {
        this.nextActivity(cls, false, bundle);
    }

    public void nextActivity(Class<?> cls, boolean finishCurrent, Bundle bundle) {
        Intent intent = new Intent(this, cls);
        intent.putExtras(bundle);
        startActivity(intent);
        if (finishCurrent) {
            finish();
        }
    }

    public void toast(String message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        if (GlobalVar.isNotification) {
            PbUtils.toast(this, message);
        } else {
            //ToastUtils.makeToast(this,message,ToastUtils.LENGTH_LONG).show();
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    public void alert(String message, DialogInterface.OnClickListener... listeners) {
        BaseAlertDialog badialog = BaseAlertDialog.creat(this, message);
        if (listeners == null || listeners.length == 0) {
            badialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        } else if (listeners.length == 1) {
            badialog.setPositiveButton("确定", listeners[0]);
        } else {
            badialog.setPositiveButton("确定", listeners[0]);
            badialog.setNegativeButton("取消", listeners[1]);
        }
        badialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        HttpClientUtils.cancelRequest();
//        JMessageClient.unRegisterEventReceiver(this);
        ToastUtils.reset();
        unregisterReceiver(myReceiver);
        System.gc(); //由于 pdf或图片采用的是bitmap 预览方式，在此手动回收一下空余资源
        try {
            Thread.sleep(100);
            System.gc();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void setActionBarGone() {
        if (mActionBar != null) {
            mActionBar.setVisibility(View.GONE);
        }
    }

    public void setActionBarVisbar() {
        if (mActionBar != null) {
            mActionBar.setVisibility(View.VISIBLE);
        }
    }

    public void setActionBarCorol(int res) {
        if (title_s != null) {
            title_s.setBackgroundColor(res);
        }
    }

    public void setActionBarTop(int visable) {
        if (title_s != null) {
            title_s.setVisibility(visable);
        }
    }

    /**
     * 通过反射的方式获取状态栏高度
     *
     * @return
     */
    public int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    public void showDialog() {
        try {
            if (!isFinishing()) {
                AnimDialogHelper.alertProgressMessage(this, true, "正在获取数据中...");
            }
        } catch (Exception e) {
        }

    }

    public void dismisDialog() {
        try {
            AnimDialogHelper.dismiss();
        } catch (Exception e) {
        }
    }

    //同一个页面显示相同加载数据提示使用
    public void showDialogMsg(String msg) {
        try {
            AnimDialogHelper.alertProgressMessage(this, true, msg);
        } catch (Exception e) {
        }
    }

    //同一个页面需要加载多个数据提示使用
    public void showDialogMsg(String msg, boolean isNew) {
        try {
            AnimDialogHelper.alertProgressMessage(this, true, msg);
        } catch (Exception e) {
        }
    }

    public void dismisDialogMsg() {
        dismisDialog();
    }

    /**
     * 给 Right_ImageView 设置图片
     *
     * @param res
     */
    public void setRightImageShow(int res) {
        if (mRightImage != null) {
            mRightImage.setVisibility(View.VISIBLE);
            mRightImage.setImageResource(res);
        }
    }

    /**
     * 给 Right_ImageView 设置点击后的下一个 Activity
     *
     * @param cls
     */
    public void setRightClick(final Class<?> cls) {
        if (mRightImage != null) {
            mRightImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nextActivity(cls);
                }
            });
        }
    }

    /**
     * 打开软键盘
     */
    protected void openKeyboard() {
        delayToDo(new TimerTask() {

            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(getParent().getCurrentFocus(), 0);
            }
        }, 500);
    }

    /**
     * 关闭软键盘
     */
    protected void closeKeyboard() {

        try {
            View view = getWindow().peekDecorView();
            if (view != null) {
                InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {

        }
    }

    /**
     * 延迟操作
     *
     * @param task
     * @param delay
     */
    protected void delayToDo(TimerTask task, long delay) {
        Timer timer = new Timer();
        timer.schedule(task, delay);
    }


    /**
     * 网络访问出错调用此接口
     */
    public void loadDataError() {
        if (mErrorLayout != null) {
            mErrorLayout.setVisibility(View.VISIBLE);

        }
        if (mDataNullLayout != null) {
            mDataNullLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 没有数据调用此接口
     */
    public void loadDataNull(String msg) {
        if (mErrorLayout != null) {
            mErrorLayout.setVisibility(View.GONE);

        }
        if (mDataNullLayout != null) {
            mDataNullLayout.setVisibility(View.VISIBLE);
            if (tv_ti_shi != null && !TextUtils.isEmpty(msg)) {
                tv_ti_shi.setText(msg);
            }
        }
    }

    /**
     * 网络访问成功调用此接口
     */
    public void loadDataSuccess() {
        if (mErrorLayout != null) {
            mErrorLayout.setVisibility(View.GONE);

        }
        if (mDataNullLayout != null) {
            mDataNullLayout.setVisibility(View.GONE);
        }
    }

    //判断无数据页面是否显示
    public boolean isNotData() {
        if (mDataNullLayout != null) {
            return mDataNullLayout.getVisibility() == View.VISIBLE ? true : false;
        }
        return false;
    }

    /**
     * 调用此接口设置点击重试按钮的事件监听
     */
    public void setOnRetryListener(final onRetryListener listener) {
        mChongShiTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRetry();
                mErrorLayout.setVisibility(View.GONE);
            }
        });
    }

    public interface onRetryListener {
        void onRetry();
    }

    public String getNotEmptyData(Object string) {
        if (string == null) {
            return "";
        }
        if (TextUtils.isEmpty("" + string) || "null".equals("" + string)) {
            return "";
        }
        return "" + string;
    }

    public String getNotNumberData(Object string) {
        if (string == null) {
            return "0";
        }
        if (TextUtils.isEmpty("" + string) || "null".equals("" + string)) {
            return "0";
        }
        return "" + string;
    }


    //显示提示信息
    public void showDialogTs(String msg) {
        try {
            AnimDialogHelper.alertConfirmMessage(this, msg, new AnimAlertDialog.OnAnimDialogClickListener() {
                @Override
                public void onClick(AnimAlertDialog AnimAlertDialog) {
                    dismisDialog();
                }
            });
        } catch (Exception e) {

        }
    }

    //显示提示信息
    public void showDialogTs(String msg,AnimAlertDialog.OnAnimDialogClickListener listener) {
        try {
            AnimDialogHelper.alertConfirmMessage(this, msg,listener);
        } catch (Exception e) {

        }
    }
}
