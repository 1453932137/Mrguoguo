package com.css.ydoa.ui.activity.root;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.css.ydoa.R;
import com.css.ydoa.common.common.HomepageListViewPagerAdapter;
import com.css.ydoa.common.utils.PbUtils;
import com.css.ydoa.common.widget.custom.SystemBarTintManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 软件的引导页面
 *
 */
public class ViewPagerActivity extends Activity {

	private ViewPager viewPager;
	private List<View> listViews;
	private View view1;
	private View view2;
	private View view3;
	private ImageView[] dots;
	private LinearLayout welcomeDotaLayout;
	private Button button1;
	private Intent intent;
	private boolean isNeedGesture, isLogIn;
	private ImageView imageView1;
	private ImageView imageView2;
	private ImageView imageView3;

	private  Class mainactivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			setTranslucentStatus(true);
			SystemBarTintManager tintManager = new SystemBarTintManager(this);
			tintManager.setStatusBarTintEnabled(true);
			tintManager.setStatusBarTintResource(R.color.text_whitesmoke);//通知栏所需颜色

		}

		this.setContentView(R.layout.activity_viewpager);
		intent=this.getIntent();

		//设置是否跳转手势密码 false：不跳转 true：跳转
		isNeedGesture = intent.getBooleanExtra("isNeedGesture", false);
		//是否已登录
		isLogIn = intent.getBooleanExtra("isLogIn", false);
		viewPager=(ViewPager)this.findViewById(R.id.newmainviewpager);
		listViews = new ArrayList<View>();
		LayoutInflater mInflater = getLayoutInflater();
		view1=mInflater.inflate(R.layout.viewpager_welcome1, null);
		view2=mInflater.inflate(R.layout.viewpager_welcome2, null);
		view3=mInflater.inflate(R.layout.viewpager_welcome3, null);
		imageView1=(ImageView)view1.findViewById(R.id.viewPager_welcome1_imageView1);
		imageView2=(ImageView)view2.findViewById(R.id.viewPager_welcome2_imageView1);
		imageView3=(ImageView)view3.findViewById(R.id.viewPager_welcome3_imageView1);

		imageView1.setImageResource(R.mipmap.new_welcom1);
		imageView2.setImageResource(R.mipmap.new_welcom2);
		imageView3.setImageResource(R.mipmap.new_welcom3);
		//bu.closeAll();
		button1=(Button)view3.findViewById(R.id.button1);
		listViews.add(view1);
		listViews.add(view2);
		listViews.add(view3);
		viewPager.setAdapter(new HomepageListViewPagerAdapter(listViews,false));
		viewPager.setCurrentItem(0);

		welcomeDotaLayout= (LinearLayout) findViewById(R.id.welcomedot);
		dots = new ImageView[3];
		//取得小点图片
		dots[0] = (ImageView) welcomeDotaLayout.getChildAt(0);
		dots[1] = (ImageView) welcomeDotaLayout.getChildAt(1);
		dots[2] = (ImageView) welcomeDotaLayout.getChildAt(2);
		dots[0].setEnabled(true);// 设为白色
		dots[1].setEnabled(false);// 设为灰色
		dots[2].setEnabled(false);// 设为灰色

		//滑动监听，改变dot外观
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			private void setCurrentDot(int position) {
				for (ImageView dot : dots) {
					dot.setEnabled(false);
				}
				dots[position].setEnabled(true);
			}
			// 当滑动状态改变时调用
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
			// 当当前页面被滑动时调用
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			// 当新的页面被选中时调用
			@Override
			public void onPageSelected(int arg0) {
				// 设置底部小点选中状态
				setCurrentDot(arg0);
			}
		});
		button1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				//setResult(RESULT_OK);
				PbUtils.setGuide(ViewPagerActivity.this, false);
				Intent intent = new Intent();
					if(isLogIn) {
						intent.setClass(ViewPagerActivity.this,mainactivity );
				}
				startActivity(intent);
				finish();
			}
		});
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
	protected void onDestroy() {
		super.onDestroy();
		System.gc();
	}
}
