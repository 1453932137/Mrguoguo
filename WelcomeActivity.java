package com.css.ydoa.ui.activity.root;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.css.ydoa.R;
import com.css.ydoa.common.base.MD5;
import com.css.ydoa.common.base.dialog.BaseAlertDialog;
import com.css.ydoa.common.base.dialog.BaseLoadingDialog;
import com.css.ydoa.common.base.dialog.SweetAlert.AnimDialogHelper;
import com.css.ydoa.common.model.GlobalVar;
import com.css.ydoa.common.model.UpdateInformation;
import com.css.ydoa.common.utils.BitmapUtils;
import com.css.ydoa.common.utils.PbUtils;
import com.css.ydoa.common.utils.http.SSLClient;
import com.css.ydoa.common.widget.custom.SystemBarTintManager;
import com.css.ydoa.core.config.AppSettings;
import com.css.ydoa.core.remote.RemoteDictTableParams;
import com.css.ydoa.core.remote.RemoteServiceIds;
import com.css.ydoa.core.remote.RemoteServiceInvokeError;
import com.css.ydoa.core.remote.RemoteServiceInvoker;
import com.css.ydoa.core.remote.ServiceResponseHandler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.CoreConnectionPNames;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 进入软件时第一个出现的欢迎界面 根据不同逻辑，可以跳转以下三个界面 1.主界面->MainActivity
 * 2.引导页->IntroduceActivity 3.手势密码验证页->LoginByHandActivity
 *
 * @author zengkai
 */
public class WelcomeActivity extends Activity {
    public static final int EXIT = 1001;
    public static final int CLOSE_WELCOME_AND_OPEN_OTHERS = 1002;
    // 延迟毫秒数
    public static final long DELAY_TIME = 200;
    public static final long IMMEDIATELY = 0;
    /**
     * 登陆接口
     */
    private final String LOGIN = "DZSWJ.ZHGLXT.MHQX.XTKJ.SWRYDL";
    BaseLoadingDialog progress;
    String logintype = "";
    boolean isFirstIn = false, isNeedGesture = false, isFingerprin = false;
    boolean isLogIn = false;//是否已登录过
    // 处理位图压缩以及释放
    BitmapUtils bu;
    Bitmap bm;
    boolean isConstraint = false;//是否为必要升级
    ProgressDialog downloadProgressDialog;
    private Intent intent = new Intent();
    private String[] result = new String[1];
    private String[] flagShare = {"0"};
    // 背景图片
    private ImageView iv_welcome;
    private String LKFLAG;
    private String OAQY = "N";
    private String OABinding = "N";
    /**
     * Handler:跳转到不同界面
     */
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EXIT:
                    break;
                case CLOSE_WELCOME_AND_OPEN_OTHERS:
                    closeWelcomeAndOpenOthers();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private Class mainactivity;

    public static boolean isEmulator(Context context) {
        String result = "";
        try {
            String[] args = {"/system/bin/cat", "/proc/cpuinfo"};
            ProcessBuilder cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            StringBuffer sb = new StringBuffer();
            String readLine = "";
            BufferedReader responseReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
            while ((readLine = responseReader.readLine()) != null) {
                sb.append(readLine);
            }
            responseReader.close();
            result = sb.toString().toLowerCase();
        } catch (IOException ex) {

        }
        return (!result.contains("arm")) || (result.contains("intel")) || (result.contains("amd"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.text_whitesmoke);//通知栏所需颜色
        }
        this.setContentView(R.layout.activity_welcome);


        iv_welcome = (ImageView) this.findViewById(R.id.activity_welcome_imageView1);

        bu = BitmapUtils.getInstance(this);
        bm = bu.compressBitmap(R.mipmap.new_login, Bitmap.Config.RGB_565);
        iv_welcome.setImageBitmap(bm);
        bu.closeAll();
            mainactivity = MainActivity.class;
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppSettings.initSettings(); //初始化配置项
        Judgmentofsafety();
        //获取登录信息

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //判斷運行是否安全
    private void Judgmentofsafety() {
        showDialog("正在检查手机安全...");
        String url = (AppSettings.getItemValue("http.targetCode"));
        if (url.equals("100011")) {
            //判断是否root 安全项检测
            /*if (getRootAhth()) {
                new BaseAlertDialog(WelcomeActivity.this)
                        .setContent("检测到您的手机已经Root，为了您的使用环境的安全，请将手机Root权限关闭！")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                endApp();
                            }
                        }).show();
            } else {
                //判断是否在模拟器
                if (isEmulator(this)){
                    // MD5签名验证
                    checkSignMD5();
                }else {
                    new BaseAlertDialog(WelcomeActivity.this)
                            .setContent("应用不支持模拟器运行！")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    endApp();
                                }
                            }).show();
                }
            }*/
            checkSignMD5();
        } else {
            // MD5签名验证
            checkSignMD5();
        }

    }

    private void checkSignMD5() {
        showDialog("正在与服务器通讯...");
        Map<String, Object> param = new HashMap();
        param.put(RemoteDictTableParams.PARAM_NAME, "zj_app_xtcs");
        Map<String, Object> where = new HashMap();
        if (AppSettings.getBgOrBsTag()) {
            where.put("csdm", "'APP_SIGN_MD5'");
        } else {
            where.put("csdm", "'APPSIGNMD5'");
        }
        param.put(RemoteDictTableParams.WHERE_NAME, where);
        RemoteServiceInvoker.invoke(RemoteServiceIds.RSID_DICTTABLE_SERVICE, param,
                new ServiceResponseHandler(this) {
                    @Override
                    public void onSuccess(Object result) {
                        String SignMD5;
                        try {
                            List<Map<String, Object>> list = (List<Map<String, Object>>) ((Map<String, Object>) result).get("data");
                            SignMD5 = list.get(0).get("CSZ").toString();
                            String MD5 = getSign(WelcomeActivity.this.getPackageName()).toUpperCase();
                            if (MD5.equals(SignMD5)) {
                                checkVersion();
                            } else {
                                new BaseAlertDialog(WelcomeActivity.this)
                                        .setContent("电子签名验证失败，请登录电子税务局官网下载最新版")
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                endApp();
                                            }
                                        }).show();
                            }
                        } catch (Exception e) {
                            new BaseAlertDialog(WelcomeActivity.this)
                                    .setContent("电子签名验证失败，请联系管理人员")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            endApp();
                                        }
                                    }).show();
                        }
                    }

                    @Override
                    public void onFailure(RemoteServiceInvokeError error, String rawJsonData) {
                        error.setMessage("服务器暂未配置，不能进行访问");
                        super.onFailure(error, rawJsonData);
                    }
                });
    }

    /**
     * 获取签名指纹
     *
     * @param paramString 包名
     * @return 签名字符串
     */
    private String getSign(String paramString) {
        if ((paramString == null) || (paramString.length() == 0)) {
            PbUtils.toast(this, "package name is null");
            return "";
        }
        PackageManager localPackageManager = this.getPackageManager();
        PackageInfo localPackageInfo;
        try {
            localPackageInfo = localPackageManager.getPackageInfo(paramString,
                    PackageManager.GET_SIGNATURES);
            if (localPackageInfo == null) {
                PbUtils.toast(this, "package info is null");
                return "";
            }
        } catch (PackageManager.NameNotFoundException localNameNotFoundException) {
            PbUtils.toast(this, "package name not found");
            return "";
        }
        Signature[] arrayOfSignature = localPackageInfo.signatures;
        if ((arrayOfSignature == null) || (arrayOfSignature.length == 0)) {
            PbUtils.toast(this, "signature is null");
            return "";
        }
        int i = arrayOfSignature.length;
        StringBuilder builder = new StringBuilder();
        for (int j = 0; j < i; j++) {
            builder.append(MD5.getMessageDigest(arrayOfSignature[j].toByteArray()));
        }
        return builder.toString();
    }

    // 直接退出欢迎界面
    private void endApp() {
        System.exit(0);
        // 释放首页的图片内存
        if (bu != null) {
            bu.releaseNativeBitmap();
        }
    }

    /**
     * 版本验证
     */
    public void checkVersion() {
        showDialog("正在检测版本...");
        Map<String, Object> param = new HashMap<String, Object>();
        if (AppSettings.getBgOrBsTag()) {
            param.put("versionCode", PbUtils.getVersion(this, 1));
        } else {
            param.put("versionCode", PbUtils.getVersion(this, 1));
            param.put("appCode", "3");
        }
        RemoteServiceInvoker.invoke(RemoteServiceIds.RSID_CHECK_VERSION, param,
                new ServiceResponseHandler(this) {
                    @Override
                    public void onSuccess(Object result) {
                        Map<String, Object> map = (Map<String, Object>) ((Map<?, ?>) result).get("data");
                        dismissDialog();
                        if (map != null) {
                            //有新版本
                            UpdateInformation update = new UpdateInformation();
                            update.setVersionName(map.get("versionName").toString());
                            update.setInformation(map.get("description").toString());
                            if (!"null".equals(map.get("size") + "") && !TextUtils.isEmpty(map.get("size") + "")) {
                                String res = (map.get("size") + "");
                                String str = res.substring(res.length() - 1, res.length());
                                if ("M".equals(str) || "m".equals(str) || "B".equals(str) || "b".equals(str)) {
                                    update.setSize(res);
                                } else {
                                    update.setSize(res + "M");
                                }
                            } else {
                                update.setSize("");
                            }

                            update.setIsConstraint((String) map.get("constraints"));
                            update.setDate(map.get("updateTime").toString());
                            UpdateInformationDialog uid = new UpdateInformationDialog(WelcomeActivity.this,
                                    R.style.CustomDialogStyle, update);
                            uid.show();
                        } else {
                            mHandler.sendEmptyMessageDelayed(CLOSE_WELCOME_AND_OPEN_OTHERS, DELAY_TIME);
                        }
                    }

                    @Override
                    public void onFailure(RemoteServiceInvokeError error, String rawJsonData) {
                        mHandler.sendEmptyMessageDelayed(CLOSE_WELCOME_AND_OPEN_OTHERS, DELAY_TIME);
                    }
                });
    }

    public void dismissDialog() {
        AnimDialogHelper.dismiss();
    }

    public void showDialog(String msg) {
        try {
            AnimDialogHelper.alertProgressMessage(this, false, msg);
        } catch (Exception e) {

        }
    }

    public void closeWelcomeAndOpenOthers() {
        //引导页 HRY@2015/10/12 切换至PbUtilss
        isFirstIn = PbUtils.isGuideNeed(this);
        //手势密码
        isNeedGesture = PbUtils.isGestruePWDSet(this);
        //是否已登录
        isLogIn = PbUtils.isLogin(this);
//		是否开启指纹解锁
        SharedPreferences sps = getSharedPreferences("Fingerprin", Context.MODE_PRIVATE);
        boolean pos = sps.getBoolean("fingerprin", false);
//		是否开启图案解锁
        SharedPreferences Lk = getSharedPreferences("Lockpattern", Context.MODE_PRIVATE);
        LKFLAG = Lk.getString("lockpattern", "");
        if (isFirstIn) {
            //引导页
            intent.setClass(this, ViewPagerActivity.class);
            intent.putExtra("isNeedGesture", isNeedGesture);
            intent.putExtra("isLogIn", isLogIn);
            startActivity(intent);
            mHandler.sendEmptyMessageDelayed(EXIT, IMMEDIATELY);
        }else {
            //原版跳转
            intent.setClass(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            mHandler.sendEmptyMessageDelayed(EXIT, IMMEDIATELY);
        }
    }







    /*
     * 下载 APK 更新包
     */
    @SuppressLint("StaticFieldLeak")
    public void downFile() {
        new AsyncTask<Void, Integer, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                FileOutputStream fileOutputStream = null;
                HttpResponse response;
                try {
                    HttpClient client = new SSLClient();
                    client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
                    client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
                    response = client.execute(new HttpGet(AppSettings.getItemValue("http.remoteUpdateUrl")));
                    HttpEntity entity = response.getEntity();
                    InputStream is = entity.getContent();
                    if (is != null) {
                        long contentLength = entity.getContentLength();
                        if (contentLength == 0) return -1;
                        downloadProgressDialog.setMax((int) contentLength / 1024);
                        File file = new File(PbUtils.LOCALAPKPATH);
                        if (!file.exists()) {
                            file.mkdir();
                        }
                        file = new File(PbUtils.LOCALAPKPATH, PbUtils.LOCALAPKNAME);
                        if (file.exists()) {
                            file.delete();
                        }
                        fileOutputStream = new FileOutputStream(file);
                        int currentLength = 0;
                        byte[] buf = new byte[1024];
                        int len = 0;
                        while ((len = is.read(buf)) > -1) {
                            fileOutputStream.write(buf, 0, len);
                            currentLength += len;
                            publishProgress(currentLength / 1024);
                        }
                        if (contentLength == currentLength) {
                            return 1;
                        } else {
                            return -1;
                        }
                    } else {
                        return -1;
                    }
                } catch (Exception e) {
                    return -1;
                } finally {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.flush();
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                downloadProgressDialog = new ProgressDialog(WelcomeActivity.this);
                downloadProgressDialog.setTitle("正在下载, 请稍候...");
                downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                downloadProgressDialog.setCanceledOnTouchOutside(false);
                downloadProgressDialog.setCancelable(false);
                downloadProgressDialog.show();
            }

            @Override
            protected void onPostExecute(Integer result) {
                try {
                    downloadProgressDialog.dismiss();
                    if (result == 1) {
                        installAPK();
                        WelcomeActivity.this.finish();
                    } else {
                        //安装包下载失败
                        BaseAlertDialog badialog = BaseAlertDialog.creat(WelcomeActivity.this, "安装包下载失败,是否重新下载?");
                        badialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                downFile();
                            }
                        });
                        badialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (!isConstraint) {
                                    mHandler.sendEmptyMessageDelayed(CLOSE_WELCOME_AND_OPEN_OTHERS, DELAY_TIME);
                                } else {
                                    WelcomeActivity.this.finish();
                                }
                            }
                        });
                        badialog.show();
                    }
                } catch (Exception e) {

                }

            }

            /**
             * 在doInBackground方法当中，，每次调用publishProgress方法都会触发onProgressUpdate执行
             * @param values
             */
            @Override
            protected void onProgressUpdate(Integer... values) {
                try {
                    downloadProgressDialog.setProgress(values[0]);
                } catch (Exception e) {

                }
            }
        }.execute();
    }

    // 安装APK
    public void installAPK() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(PbUtils.LOCALAPKPATH, PbUtils.LOCALAPKNAME)), "application/vnd.android.package-archive");
        WelcomeActivity.this.startActivity(intent);
    }

    protected void toast(String message) {
        if (!TextUtils.isEmpty(message)) {
            if (GlobalVar.isNotification) {
                PbUtils.toast(this, message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }


    /**
     * 版本更新的提示框
     */
    class UpdateInformationDialog extends Dialog {
        Button update;
        Button cancel;
        TextView information;
        TextView versionName;
        TextView size;
        TextView PS;
        ScrollView scrollView;
        Context context;
        UpdateInformation updateInformation;

        public UpdateInformationDialog(Context context, int theme,
                                       UpdateInformation ui) {
            super(context, theme);
            this.context = context;
            this.updateInformation = ui;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setContentView(R.layout.dialog_update);
            this.setCancelable(false);
            update = (Button) this.findViewById(R.id.update_message_update);
            cancel = (Button) this.findViewById(R.id.update_message_cancel);
            information = (TextView) this.findViewById(R.id.update_message_information);
            versionName = (TextView) this.findViewById(R.id.update_message_versionName);
            size = (TextView) this.findViewById(R.id.update_message_size);
            PS = (TextView) this.findViewById(R.id.update_message_PS);
            scrollView = (ScrollView) this.findViewById(R.id.update_message_scrollView1);
            information.setText(updateInformation.getInformation());
            versionName.setText("版本：" + updateInformation.getVersionName());
            size.setText("大小：" + updateInformation.getSize());
            isConstraint = "1".equals(updateInformation.getIsConstraint()) ? true : false;
            if (isConstraint) PS.setVisibility(View.VISIBLE);
            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    downFile();
                }
            });


            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isConstraint) {
                        dismiss();
                        WelcomeActivity.this.finish();
                    } else {
                        dismiss();
                        mHandler.sendEmptyMessageDelayed(
                                CLOSE_WELCOME_AND_OPEN_OTHERS, DELAY_TIME);
                    }
                }
            });
        }
    }

}
