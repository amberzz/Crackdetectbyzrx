package com.example.cracktrace;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.util.Log;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;

public class CrackTraceActivity extends ActionBarActivity {
	
	Button m_cache_button;
	Button m_load_button;
	Button m_hist_button;
	
	ButtonClickListener m_btn_listener;
	
	MediaScanner mMediaScanner = null;

//	String appPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/Camera";
	String appPath   = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrackTrace";
	String imagePath = appPath + "/Images";
	//String imagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
	String savePath = appPath + "/Results";
	String mCaptureImage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crack_trace);
		makeSureDirExist(appPath);
//		makeSureDirExist(imagePath);
//		makeSureDirExist(savePath);
		
		m_cache_button = (Button)findViewById(R.id.cache);
		m_load_button = (Button)findViewById(R.id.load);
		m_hist_button = (Button)findViewById(R.id.history);
		
		m_btn_listener = new ButtonClickListener();
		mMediaScanner = new MediaScanner(CrackTraceActivity.this);
		
		m_cache_button.setOnClickListener(m_btn_listener);
		m_load_button.setOnClickListener(m_btn_listener);
		m_hist_button.setOnClickListener(m_btn_listener);

	}
	
	class ButtonClickListener implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			int btn_id = v.getId();
			switch (btn_id) {
			case R.id.cache:
				captureImages();
				break;
			case R.id.load:
				Intent loadImage = new Intent(CrackTraceActivity.this, ShowImageActivity.class);
				startActivity(loadImage);
				break;
			case R.id.history:
				Intent loadHisImage = new Intent(CrackTraceActivity.this, ShowImageActivity.class);
				startActivity(loadHisImage);
				break;

			default:
				break;
			}			
		}
		
	}
	
	public void makeSureDirExist(String dir){
		File directory = new File(dir);
		if (!directory.exists()){
			directory.mkdir();
		}
	}
	
	public void captureImages() {
		String filename = getFileNameBasedOnTime();

		mCaptureImage = imagePath + "/" + filename + ".jpg";
		File newImage = new File(mCaptureImage);
		if (newImage != null) {
			Uri newImageUri;
			/*
			安卓7.0 调用系统相机拍照不再允许使用Uri方式，应该替换为FileProvider
			 */
			if (Build.VERSION.SDK_INT >= 24) {
				int checkCallPhonePermission = ContextCompat.checkSelfPermission(CrackTraceActivity.this, Manifest.permission.CAMERA);
				if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
					ActivityCompat.requestPermissions(CrackTraceActivity.this,new String[]{Manifest.permission.CAMERA},222);
					return;
				}else {
					newImageUri = FileProvider.getUriForFile(this, "com.xf.activity.provider.download", newImage);
				}
			} else {
				newImageUri = Uri.fromFile(newImage);
			}
			Intent toCaptureImage = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			toCaptureImage.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, newImageUri);
			startActivityForResult(toCaptureImage, 0);
		}

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode){
			case 222:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// Permission Granted
					captureImages();
				} else {
					// Permission Denied
					Toast.makeText(CrackTraceActivity.this, "很遗憾你把相机权限禁用了。请务必开启相机权限享受我们提供的服务吧。", Toast.LENGTH_SHORT)
							.show();
				}
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	@Override
	protected void onActivityResult(int requestcode, int resultcode, Intent intent) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestcode, resultcode, intent);
		
		if (RESULT_OK == resultcode) {
			File insertImage = new File(mCaptureImage);
			if (insertImage.exists()) {   
	            try {
					String url = MediaStore.Images.Media.insertImage(getContentResolver(), mCaptureImage, "", "");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
			else{
				System.out.println("insertImage not exist!!!");
			}
//			try {
//				MediaStore.Images.Media.insertImage(getContentResolver(), mCaptureImage, "", "");
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			mMediaScanner.scanFile(mCaptureImage, "jpg");
			//MediaScannerConnection.scanFile(this, new String[]{Environment.getExternalStorageDirectory().getAbsolutePath()}, null, null);
			Intent toShowImage = new Intent(CrackTraceActivity.this, ShowImageActivity.class);
			startActivity(toShowImage);
		}
	}
	
	public String getFileNameBasedOnTime(){
		Time tm = new Time();
		tm.setToNow();
		int year   = tm.year;
		int month  = tm.month;
		int day    = tm.monthDay;
		int hour   = tm.hour;
		int minute = tm.minute;
		int second = tm.second;
		
		String filename = "" + year + month + day + hour + minute + second;
		return filename;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.crack_trace, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
