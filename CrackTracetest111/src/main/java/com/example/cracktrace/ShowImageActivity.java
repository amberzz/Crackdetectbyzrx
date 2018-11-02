package com.example.cracktrace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;



import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PointF;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.FloatMath;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;


public class ShowImageActivity extends AppCompatActivity {

	Boolean m_load  = false;//加载图片
	Boolean m_ruler = false;//标定标记
	Boolean m_chose = false;//划线
	Boolean m_reload = false;

	Boolean m_start = false;
	Boolean m_end = false;

	PointF m_startPoint = new PointF();
	PointF m_endPoint	= new PointF();
	PointF m_choseStart = new PointF();
	PointF m_choseEnd	= new PointF();

	MediaScanner sMediaScanner = null;

	private Bitmap selected_image;
	String appPath   = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrackTrace";
	String savePathDir = appPath + "/Results";
//	String appPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/Camera";
	String savePath;
//	String imagePath = appPath + "/Images";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

	public static final int LOAD_IMAGE   = 0;
	public static final int LOAD_HISTORY = 1;
	public static final int RELOAD_IMAGE = 2;
	public static final int SAVE_IMAGE   = 3;
	public static final int GET_WIDTH    = 4;

	EditText  m_editText;
	ImageView m_imageView;
	ImageViewTouchListener m_imageviewlistener = new ImageViewTouchListener();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_image);

		m_editText  = (EditText)findViewById(R.id.result);
		m_imageView = (ImageView)findViewById(R.id.showImage);
		m_imageView.setOnTouchListener(m_imageviewlistener);

		sMediaScanner = new MediaScanner(ShowImageActivity.this);
	}

	class ImageViewTouchListener implements OnTouchListener{



        @Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
            /**
             * 针对安卓6.0以上访问storage文件夹的权限动态管理
             */
            if (ActivityCompat.checkSelfPermission(ShowImageActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                        ShowImageActivity.this,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            }
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				if(false == m_ruler && false == m_chose && false == m_load){
					Dialog alertDialog = new AlertDialog.Builder(ShowImageActivity.this).setTitle("提示").setMessage("请选择操作类型")
							.setPositiveButton("加载图片", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									m_load = true;
									responseToDialogButton(LOAD_IMAGE);
								}
							})
							.setNegativeButton("加载历史图片", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									m_load = true;
									responseToDialogButton(LOAD_HISTORY);
								}
							}).create();
					alertDialog.show();
				}
				else{
					assert(m_load == true);
					if(false == m_ruler && false == m_chose){
						Dialog alertDialog1 = new AlertDialog.Builder(ShowImageActivity.this).setTitle("提示").setMessage("请选择操作类型")
								.setPositiveButton("重新加载图片", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										//m_load  = true;
										m_ruler = false;
										m_chose = false;
										m_start = false;
										m_end   = false;
										responseToDialogButton(RELOAD_IMAGE);
									}
								})
								.setNegativeButton("标定图片", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										//m_load = true;
										m_ruler = true;
										m_chose = false;
									}
								})
								.setNeutralButton("测量宽度", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										m_ruler = false;
										m_chose = true;
									}
								}).create();
						alertDialog1.show();
					}
					else if (true == m_ruler){
						if (m_start == false)
						{
							m_startPoint.set(event.getX(), event.getY());
							m_start = true;
						}
						else if (m_end == false)
						{
							m_endPoint.set(event.getX(), event.getY());
							m_end = true;
							m_ruler = false;
						}
					}
					else if (true == m_chose){
						m_choseStart.set(event.getX(), event.getY());
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				if(true == m_chose)
				{
					m_choseEnd.set(event.getX(), event.getY());
					float crack_width = Caculate_width();
					m_editText.setText(crack_width + "mm");
					m_chose = false;
				}
			default:
				break;
			}
			return true;
		}

	}

	@SuppressLint("FloatMath")
	float Caculate_width(){
		float crack_width = 0f;

		float dx = m_startPoint.x - m_endPoint.x;
		float dy = m_startPoint.y - m_endPoint.y;
		float lable_len = (float)Math.sqrt(dx *dx + dy *dy );
		float clo_dis = get_clo_dis();

		crack_width = 100f / lable_len * clo_dis;
		return crack_width;
	}

	@SuppressLint("FloatMath")
	float get_clo_dis(){
		float clo_dis = 0f;
		int sx = 0, sy = 0, ex = 0, ey = 0;
		boolean flag = false;

		if (Math.abs(m_choseStart.x - m_choseEnd.x) < 0.0001){
			int choseX = (int)m_choseStart.x;
			int startY = (int)(m_choseStart.y > m_choseEnd.y ? m_choseEnd.y : m_choseStart.y);
			int endY = (int)(m_choseStart.y > m_choseEnd.y ? m_choseStart.y : m_choseEnd.y);

			for(int k = startY; k <= endY; k++){
				int color = selected_image.getPixel(choseX, k);

				if (0xff000000 == color && false == flag){
					sx = choseX;
					sy = k;
					flag = true;

				}
				else if (0xffffffff == color && true == flag){
					ex = choseX;
					ey = k;
					flag = false;

					break;
				}
				else{

					continue;
				}
			}

			clo_dis = (float)(ey - sy);
		}
		else if (Math.abs(m_choseStart.y - m_choseEnd.y) < 0.0001){
			int choseY = (int)m_choseStart.y;
			int startX = (int)(m_choseStart.x > m_choseEnd.x ? m_choseEnd.x : m_choseStart.x);
			int endX = (int)(m_choseStart.x > m_choseEnd.x ? m_choseStart.x : m_choseEnd.x);
			for(int k = startX; k <= endX; k++){
				int color = selected_image.getPixel(choseY, k);

				if (0xff000000 == color && false == flag){
					sx = k;
					sy = choseY;
					flag = true;

				}
				else if (0xffffffff == color && true == flag){
					ex = k;
					ey = choseY;
					flag = false;

					break;
				}
				else{

					continue;
				}
			}
			clo_dis = (float)(ex - sx);
		}
		else{
			float rat = (m_choseStart.y - m_choseEnd.y) / (m_choseStart.x - m_choseEnd.x);
			float b = m_choseStart.y - rat * m_choseStart.x;
			int startX = (int)(m_choseStart.x > m_choseEnd.x ? m_choseEnd.x : m_choseStart.x);
			int endX = (int)(m_choseStart.x > m_choseEnd.x ? m_choseStart.x : m_choseEnd.x);
			for(int k = startX; k <= endX; k++){
				int fx = (int)(rat * k + b);
				int color = selected_image.getPixel(k, fx);

				if (0xff000000 == color && false == flag){
					sx = k;
					sy = fx;
					flag = true;

				}
				else if (0xffffffff == color && true == flag){
					ex = k;
					ey = (int)(rat * ex + b);
					flag = false;

					break;
				}
				else{

					continue;
				}
			}

			clo_dis = (float)Math.sqrt((ey-sy) * (ey-sy) + (ex-sx) * (ex-sx));
		}
		return clo_dis;
	}

	public void responseToDialogButton(int action){
		switch (action) {
		case LOAD_IMAGE:
//			MediaScannerConnection.scanFile(this, new String[]{Environment.getExternalStorageDirectory().getAbsolutePath()}, null, null);
//			getBaseContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath())));
//update image need to Insert image into MediaStore
			Intent action_load = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(action_load, LOAD_IMAGE);
			break;
		case LOAD_HISTORY:
//			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath())));
			Intent action_his = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(action_his, LOAD_HISTORY);
			break;
		case RELOAD_IMAGE:
			Dialog alertDialog2 = new AlertDialog.Builder(ShowImageActivity.this).setTitle("提示").setMessage("选择重新加载图片").
				setPositiveButton("加载图片", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						responseToDialogButton(LOAD_IMAGE);
					}
				}).
				setNegativeButton("加载历史图片", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						responseToDialogButton(LOAD_HISTORY);
					}
				}).setNeutralButton("保存图片", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						responseToDialogButton(SAVE_IMAGE);
					}
				}).create();
			alertDialog2.show();
			m_start = false;
			m_end   = false;
			break;
		case SAVE_IMAGE:
			final EditText et = new EditText(ShowImageActivity.this);
			Dialog alertDialog3 = new AlertDialog.Builder(ShowImageActivity.this).setTitle("请输入保存文件名")
					.setView(et)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							String saveTitle = et.getText().toString();
							savePath = savePathDir + "/" + saveTitle + ".jpg";
							File saveImage = new File(savePath);

							try {
								saveImage.createNewFile();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
				            FileOutputStream fOut = null;
				            try {
				                    fOut = new FileOutputStream(saveImage);
				                    } catch (FileNotFoundException e) {
				                    e.printStackTrace();
				            }
				            selected_image.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
				            try {
				                    fOut.flush();
				            } catch (IOException e) {
				                    e.printStackTrace();
				            }
				            try {
				                    fOut.close();
				            } catch (IOException e) {
				                    e.printStackTrace();
				            }

				            File insertImage = new File(savePath);
							if (insertImage.exists()) {
					            try {
									String url = MediaStore.Images.Media.insertImage(getContentResolver(), savePath, "", "");
								} catch (FileNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
					        }
							else{
								System.out.println("insertImage not exist!!!");
							}
							sMediaScanner.scanFile(savePath, "jpg");
						}
					})
					.setNegativeButton("Cancel", null)
					.create();
			alertDialog3.show();
			break;
		default:
			break;
		}
	}

	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestcode, int resultcode, Intent intent) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestcode, resultcode, intent);

		if(resultcode == RESULT_OK && null != intent){
			Uri selectedImage = intent.getData();
			String[] filepathColumn = {MediaStore.Images.Media.DATA};

			Cursor cursor = getContentResolver().query(selectedImage,
													   filepathColumn,
													   null, null, null);
			cursor.moveToFirst();

			int columnindex = cursor.getColumnIndex(filepathColumn[0]);
			String imagePath = cursor.getString(columnindex);
			cursor.close();

			BitmapFactory.Options options = new BitmapFactory.Options();


			options.inMutable = true;
			options.inJustDecodeBounds = true;

			selected_image = BitmapFactory.decodeFile(imagePath, options);

			LayoutParams param = m_imageView.getLayoutParams();
			param.width = 960;
			param.height = 970;
			m_imageView.setLayoutParams(param);
			if(options.outHeight > param.height || options.outWidth > param.width)
			{
				options.inSampleSize = 4;
			}
			else
			{
				options.inSampleSize = 1;
			}

            options.inJustDecodeBounds = false;
			selected_image = BitmapFactory.decodeFile(imagePath, options);
			if (requestcode == LOAD_IMAGE){
				//turn2bin
				turnToBinaryImage(selected_image);
				if(m_reload == false){
					m_reload = true;
					//save result

				}
			}
			m_imageView.setImageBitmap(selected_image);
		}
	}

	public void turnToBinaryImage(Bitmap source)
	{

		if (null == source)
			System.out.println("[DBG]8");
		int width = source.getWidth();

		int height = source.getHeight();
		int area = width * height;
		int u = 0;// avg gray
		int graysum = 0;
		int graymean = 0;
		int grayfrontmean = 0;
		int graybackmean = 0;
		int color;
		int pixelsA;
		int pixelsR;
		int pixelsG;
		int pixelsB;
		int pixelGray;
		int front = 0;
		int back = 0;

		for (int i = 1; i < width; i++) { // 不算边界行和列，为避免越界
			for (int j = 1; j < height; j++) {
				color = source.getPixel(i, j);
				pixelsA = color&0xFF000000;//img.getRGB(i, j);

				pixelsR = (color >> 16) & 0xFF;// R空间
				pixelsB = (color >> 8) & 0xFF;// G空间
				pixelsG = color & 0xFF;// B空间
				pixelGray = (int) (0.3 * pixelsR + 0.59 * pixelsG + 0.11 * pixelsB);// 计算每个坐标点的灰度
				source.setPixel(i, j, pixelsA + (pixelGray << 16) + (pixelGray << 8) + (pixelGray));
				graysum += pixelGray;
			}
		}

		graymean = (int) (graysum / area);// 整个图的灰度平均值
		u = graymean;
		for (int i = 0; i < width; i++) // 计算整个图的二值化阈值
		{
			for (int j = 0; j < height; j++) {
				if ((source.getPixel(i, j) & (0x0000ff)) < graymean) {
					graybackmean += ((source.getPixel(i, j)) & (0x0000ff));
					back++;
				} else {
					grayfrontmean += ((source.getPixel(i, j)) & (0x0000ff));
					front++;
				}
			}
		}

		int frontvalue = (int) (grayfrontmean / front);// 前景中心
		int backvalue = (int) (graybackmean / back);// 背景中心
		float G[] = new float[frontvalue - backvalue + 1];// 方差数组
		int s = 0;

		for (int i1 = backvalue; i1 < frontvalue + 1; i1++)// 以前景中心和背景中心为区间采用大津法算法
		{
			back = 0;
			front = 0;
			grayfrontmean = 0;
			graybackmean = 0;
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					if (((source.getPixel(i, j)) & (0x0000ff)) < (i1 + 1)) {
						graybackmean += ((source.getPixel(i, j)) & (0x0000ff));
						back++;
					} else {
						grayfrontmean += ((source.getPixel(i, j)) & (0x0000ff));
						front++;
					}
				}
			}
			grayfrontmean = (int) (grayfrontmean / front);
			graybackmean = (int) (graybackmean / back);
			G[s] = (((float) back / area) * (graybackmean - u)
					* (graybackmean - u) + ((float) front / area)
					* (grayfrontmean - u) * (grayfrontmean - u));
			s++;
		}

		float max = G[0];
		int index = 0;
		for (int i = 1; i < frontvalue - backvalue + 1; i++) {
			if (max < G[i]) {
				max = G[i];
				index = i;
			}
		}

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (((source.getPixel(i, j)) & (0x0000ff)) < (index + backvalue)) {
					source.setPixel(i, j, 0xff000000);
				} else {
					source.setPixel(i, j, 0xffffffff);
				}
			}
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_image, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar picturegridview_item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
