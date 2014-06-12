package com.zhidian.wifibox.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.AppDetailActivity;
import com.zhidian.wifibox.data.AppDataBean;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.InstallingValidator;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian.wifibox.view.ProgressBitmapDrawable;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * ExtraContainer数据适配器
 * 
 * @author xiedezhi
 * 
 */
public class ExtraAdapter extends BaseAdapter {

	private List<AppDataBean> mList = new ArrayList<AppDataBean>();
	private Context mContext;
	private LayoutInflater mInflater;
	private Bitmap mProgressBitmap;
	/**
	 * 跳转详情点击监听
	 */
	private OnClickListener mItemClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			//long id = (Long) v.getTag();
			AppDataBean bean = (AppDataBean) v.getTag();
			Intent intent = new Intent(mContext, AppDetailActivity.class);
			intent.putExtra("bean", bean);
			intent.putExtra("appId", bean.id);
			mContext.startActivity(intent);
		}
	};
	/**
	 * 打开应用的点击监听
	 */
	private OnClickListener mOpenAppClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String packName = (String) v.getTag();
			try {
				PackageManager packageManager = mContext.getPackageManager();
				Intent intent = packageManager
						.getLaunchIntentForPackage(packName);
				mContext.startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	/**
	 * 打开APK的点击监听
	 */
	private OnClickListener mOpenApkClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			try {
				String fileName = (String) v.getTag();
				File file = new File(fileName);
				Intent intent = new Intent();
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file),
						"application/vnd.android.package-archive");
				mContext.startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	/**
	 * 暂停点击事件
	 */
	private OnClickListener mPauseClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AppDataBean bean = (AppDataBean) v.getTag();
			bean.downloadStatus = DownloadTask.PAUSING;
			Intent intent = new Intent(
					IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
			intent.putExtra("command", IDownloadInterface.REQUEST_COMMAND_PAUSE);
			intent.putExtra("url", bean.downloadUrl);
			mContext.sendBroadcast(intent);
			notifyDataSetChanged();
		}
	};
	/**
	 * 继续点击事件
	 */
	private OnClickListener mContinueClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AppDataBean bean = (AppDataBean) v.getTag();
			bean.downloadStatus = DownloadTask.DOWNLOADING;
			Intent intent = new Intent(
					IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
			intent.putExtra("command",
					IDownloadInterface.REQUEST_COMMAND_CONTINUE);
			intent.putExtra("url", bean.downloadUrl);
			mContext.sendBroadcast(intent);
			notifyDataSetChanged();
		}
	};
	/**
	 * 下载点击监听器
	 */
	private OnClickListener mDownloadClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AppDataBean bean = (AppDataBean) v.getTag();
			bean.downloadStatus = DownloadTask.DOWNLOADING;
			Intent intent = new Intent(
					IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
			intent.putExtra("command", IDownloadInterface.REQUEST_COMMAND_ADD);
			intent.putExtra("url", bean.downloadUrl);
			intent.putExtra("iconUrl", bean.iconUrl);
			intent.putExtra("name", bean.name);
			intent.putExtra("size", bean.size);
			intent.putExtra("packName", bean.packName);
			intent.putExtra("appId", bean.id);
			intent.putExtra("version", bean.version);
			mContext.sendBroadcast(intent);
			notifyDataSetChanged();
		}
	};

	public ExtraAdapter(Context context, TAApplication application) {
		this.mContext = context;
		mInflater = LayoutInflater.from(context);
		mProgressBitmap = ((BitmapDrawable) context.getResources().getDrawable(
				R.drawable.icon_loading1)).getBitmap();
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item_recommend_app, null);
			holder.ivAvatar = (ImageView) convertView
					.findViewById(R.id.item_recommend_image);
			holder.btnDownLoad = (Button) convertView
					.findViewById(R.id.item_recommend_download);
			holder.tvAppDescribe = (TextView) convertView
					.findViewById(R.id.item_recommend_describe);
			holder.tvAppName = (TextView) convertView
					.findViewById(R.id.item_recommend_appname);
			holder.tvAppSize = (TextView) convertView
					.findViewById(R.id.item_recommend_size);
			holder.tvDownloadTime = (TextView) convertView
					.findViewById(R.id.item_recommend_time);
			holder.ratingBar = (RatingBar) convertView
					.findViewById(R.id.item_recommend_rating);
			holder.btnLayout = (LinearLayout) convertView
					.findViewById(R.id.recommend_layout_btn);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// 把bean的内容显示到view上
		AppDataBean bean = mList.get(position);
		holder.tvAppSize.setText(FileUtil.convertFileSize(bean.size));
		holder.tvAppName.setText(bean.name);
		holder.tvDownloadTime.setText(FileUtil
				.convertDownloadTimes(bean.downloads));
		holder.tvAppDescribe.setText(Html.fromHtml(bean.explain));
		holder.ratingBar.setRating(bean.score / 2.0f);

		final ImageView image = holder.ivAvatar;
		image.setTag(bean.iconUrl);
		Bitmap bm = AsyncImageManager.getInstance().loadImage(
				PathConstant.ICON_ROOT_PATH, bean.iconUrl.hashCode() + "",
				bean.iconUrl, true, true, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap == null) {
							return;
						}
						if (image.getTag().equals(imgUrl)) {
							image.setImageBitmap(imageBitmap);
						}
					}
				});
		if (bm != null) {
			image.setImageBitmap(bm);
		} else {
			// 默认
			image.setImageBitmap(DrawUtil.sDefaultIcon);
		}
		holder.btnLayout.setTag(bean);
		holder.btnLayout.setOnClickListener(mItemClickListener);
		// 根据下载状态设置下载按钮的显示
		initDownloadState(holder.btnDownLoad, bean);
		return convertView;
	}

	/**
	 * 根据下载状态初始化下载按钮和点击事件
	 */
	private void initDownloadState(Button btnDownLoad, AppDataBean bean) {
		String packName = bean.packName;
		String apkFileName = DownloadUtil.getCApkFileFromUrl(bean.downloadUrl);
		boolean isInstall = InstallingValidator.getInstance().isAppExist(
				mContext, packName);
		if (isInstall) {
			// 已安装
			btnDownLoad.setCompoundDrawablesWithIntrinsicBounds(null, mContext
					.getResources().getDrawable(R.drawable.icon_open), null,
					null);
			btnDownLoad.setTag(packName);
			btnDownLoad.setOnClickListener(mOpenAppClickListener);
			btnDownLoad.setText(R.string.btn_open);
		} else if (bean.downloadStatus == DownloadTask.INSTALLING) {
			// 正在安装
			btnDownLoad.setCompoundDrawablesWithIntrinsicBounds(null, mContext
					.getResources().getDrawable(R.drawable.icon_install), null,
					null);
			btnDownLoad.setTag(null);
			btnDownLoad.setOnClickListener(null);
			btnDownLoad.setText(R.string.installing);
		} else if (FileUtil.isFileExist(apkFileName)) {
			// 已下载
			btnDownLoad.setCompoundDrawablesWithIntrinsicBounds(null, mContext
					.getResources().getDrawable(R.drawable.icon_install), null,
					null);
			btnDownLoad.setTag(apkFileName);
			btnDownLoad.setOnClickListener(mOpenApkClickListener);
			btnDownLoad.setText(R.string.btn_install);
		} else if (bean.downloadStatus == DownloadTask.DOWNLOADING) {
			// 正在下载
			// 根据具体的下载进度设置按钮图标
			Drawable drawable = new ProgressBitmapDrawable(
					mContext.getResources(), mProgressBitmap,
					bean.alreadyDownloadPercent, 0xFF888888, 0xFF0a78ee);
			btnDownLoad.setCompoundDrawablesWithIntrinsicBounds(null, drawable,
					null, null);
			btnDownLoad.setTag(bean);
			btnDownLoad.setOnClickListener(mPauseClickListener);
			btnDownLoad.setText(bean.alreadyDownloadPercent + "%");
		} else if (bean.downloadStatus == DownloadTask.PAUSING) {
			// 已经暂停
			btnDownLoad.setCompoundDrawablesWithIntrinsicBounds(null, mContext
					.getResources().getDrawable(R.drawable.icon_continue),
					null, null);
			btnDownLoad.setTag(bean);
			btnDownLoad.setOnClickListener(mContinueClickListener);
			btnDownLoad.setText(R.string.btn_goon);
		} else {
			// 下载未开始或下载失败
			btnDownLoad.setCompoundDrawablesWithIntrinsicBounds(null, mContext
					.getResources().getDrawable(R.drawable.icon_download),
					null, null);
			btnDownLoad.setTag(bean);
			btnDownLoad.setOnClickListener(mDownloadClickListener);
			btnDownLoad.setText(R.string.btn_download);
		}
	}

	/**
	 * 更新数据，并调用notifyDataSetChanged
	 */
	public void update(List<AppDataBean> list) {
		mList.clear();
		if (list == null || list.size() <= 0) {
			notifyDataSetChanged();
			return;
		}
		mList.addAll(list);
		notifyDataSetChanged();
	}

	static class ViewHolder {
		ImageView ivAvatar; // 应用头像
		TextView tvAppName; // 应用名称
		TextView tvDownloadTime; // 下载次数
		TextView tvAppSize; // 应用大小
		Button btnDownLoad; // 下载按钮
		TextView tvAppDescribe; // 应用描述
		RatingBar ratingBar; // 星级评分
		LinearLayout btnLayout; // 跳转按钮
	}

}