package com.zhanghang.self.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import java.util.HashMap;

/**
 * 展示浏览器的界面 用于显示广告、推广等
 * 
 * @author zhanghang
 * @date:2015-3-31
 */
public class BaseWebActivity extends Activity {
	
	private String TAG="BaseWebActivity";
	/**
	 * 是否设置额外的视图
	 */
	protected boolean isSetExtraView = false;
	/**
	 * 当前页面所load的URL
	 */
	protected String web_url = "";
	/**
	 * 防止load两次url
	 */
	private String lastUrl = "";
	/**
	 * 业务类型
	 */
	protected int type = 0;
	
	/**********相关view***************************/
	/**
	 * 当前webView
	 */
	protected WebView urlWeb;
	/**
	 * webView的父视图
	 */
	private FrameLayout base_web_framelayout;
	/**
	 * webView的父视图
	 */
	protected View extra_view;

	/**
	 * 获取intent中的数据
	 */
	protected void getIntentData() {

	}

	protected void initTitle() {
		// 初始化方法
		initControl();// 配置WebView
		initEvent();// 设置相关事件
		showWebUrl();// 展示当前URL
	}

	/**
	 * WebSettings中的一些配置
	 */
	protected void initControl() {
		// TODO Auto-generated method stub
		WebSettings websettting = urlWeb.getSettings();
		websettting.setJavaScriptEnabled(true);
		websettting.setJavaScriptCanOpenWindowsAutomatically(true);
		urlWeb.addJavascriptInterface(this, "nagetive");
		websettting.setDefaultTextEncodingName("UTF-8");
		websettting.setBlockNetworkImage(false);
		websettting.setDomStorageEnabled(true);
 		websettting.setDatabaseEnabled(true);
		String dir = getApplicationContext().getDir("database",MODE_PRIVATE).getPath();
		// 启用地理定位
		websettting.setGeolocationEnabled(true);
		websettting.setGeolocationDatabasePath(dir);// 设置定位的数据库路径
		websettting.setDatabasePath(dir);
		//开启localstorage
		websettting.setAllowFileAccess(true);
		websettting.setAppCacheEnabled(true);
		websettting.setAppCacheMaxSize(1024*1024*8);
		String appCachePath = getApplicationContext().getDir("cache",MODE_PRIVATE).getPath();
		websettting.setAppCachePath(appCachePath);
		websettting.setCacheMode(WebSettings.LOAD_DEFAULT);
		websettting.setRenderPriority(RenderPriority.HIGH);

	}

	/**
	 * 设置相关事件（包括WebView相关事件）
	 */
	protected void initEvent() {

	}
	
	/**
	 * 以base_web_framelayout为父视图，设置额外的视图
	 * @param    resoursId    资源ID
	 */
	protected void setExtraView(int resoursId){
		if(!isSetExtraView){
		    if(resoursId>0){
			    LayoutInflater inflater = LayoutInflater.from(this);
			    base_web_framelayout = (FrameLayout)inflater.inflate(resoursId, base_web_framelayout);
			    int count = base_web_framelayout.getChildCount();
			    extra_view = base_web_framelayout.getChildAt(count-1);
			    isSetExtraView = true;
		    }
		}
	}
	/**
	 * 拦截URL具体处理事件方法
	 * @param  view webView
	 * @param  url  所拦截的URL
	 * @return  true  适合进入新页面；flase  就在本页面加载
	 */
	protected boolean dealShouldOverrideUrlLoading(WebView view, String url){
		return true;
	}
	
	/**
	 * 通过startActivity传入的Intent中得到标题和链接地址。
	 */
	protected void showWebUrl() {
		try {

		} catch (Exception e) {
		  e.printStackTrace();	
		}
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		lastUrl = "";
	}

	/**
	 * url上的按钮点击进行的跳转；请使用nagetive.opennewUrl("新页面","http://www.baidu.com");
	 * 
	 * @param title
	 *            新页签的标题
	 * @param url
	 *            新页签的界面url
	 * @edit by liuyi
	 */
	protected void opennewurl(String title, String url,Class<? extends Activity> target,HashMap param) {

	}
	/**
	 * 调用系统浏览器打开网址 请使用nagetive.opennewurlbybrowser("http://www.baidu.com");
	 * 
	 * @param url
	 *            需要跳转的界面
	 */
	private void opennewurlbybrowser(String url) {
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		Uri content_url = Uri.parse(url);
		intent.setData(content_url);
		startActivity(intent);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	

	
	class BaseWebViewClient extends WebViewClient{

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// 结束
			super.onPageFinished(view, url);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			// TODO Auto-generated method stub
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			boolean result = super.shouldOverrideUrlLoading(view, url);
			// 拦截html中href超链接点击事件
			if (!TextUtils.isEmpty(url)) {
				if (url.equals(lastUrl)) {
					return false;
				}
				lastUrl = url;

				return dealShouldOverrideUrlLoading(view, url);
			}
			return result;
		}
	}
	/**
	 * 弹出呼叫号码对话框
	 * @param phone
	 */
	public void assureCallPhone(Context context, String callphonetip, final String phone) {


	}
	/**
	 *设置setWebChromeClient时，继承的基类 
	 *BaseWebActivity中并未调用WebView的
	 *setWebChromeClient方法；继承BaseWeb-
	 *Activity的子类可以酌情决定是否设置WebChr-
	 *omeClient
	 **/
	protected class BaseWebChromeClient extends WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
		}
		/**
		 * 显示位置权限提示
		 * @param  origin    请求定位的url
		 * @param  callback  提示的回调接口
		 **/
		@Override
		public void onGeolocationPermissionsShowPrompt(String origin,GeolocationPermissions.Callback callback) {  
	        Log.i("onGeolocationPermissionsShowPrompt", "onGeolocationPermissionsShowPrompt【"+origin+"】");
	      //第一个参数是指请求定位的url,第二个参数表示是否允许定位，第三个参数表示是否记住选择
	        super.onGeolocationPermissionsShowPrompt(origin, callback);  
	        callback.invoke(origin, true, false);
	    }
		@Override
		public void onGeolocationPermissionsHidePrompt() {
			super.onGeolocationPermissionsHidePrompt();
			Log.i("onGeolocationPermissionsHidePrompt", "onGeolocationPermissionsHidePrompt");
		}
		@Override
		public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota,   
		        long estimatedSize, long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
			quotaUpdater.updateQuota(estimatedSize * 2);
		} 
	}
}
