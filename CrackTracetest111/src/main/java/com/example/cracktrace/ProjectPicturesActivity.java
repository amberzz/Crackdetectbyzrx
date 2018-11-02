package com.example.cracktrace;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectPicturesActivity extends AppCompatActivity {
    private GridView gridview;
    private PictureAdapter pictureadapter;
    private Button addnewpicture;
    final static String apppath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/CrackProject";
    private String projectname;//以该项目名创建的文件夹
    private List<String> titles;//该项目名的文件夹下所有图片的名字
    private List<String> imagepaths = new ArrayList<String>(0);//该项目名的文件夹下所有图片的绝对路径
    private String takephoto;//通过系统相机拍摄的照片保存的绝对路径
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        int mscreenheight = display.getHeight();
        int mscreenwight = display.getWidth();
        setContentView(R.layout.activity_projectpicture);
        final Intent intent = getIntent();
        projectname = intent.getStringExtra("projectname");
        if (ActivityCompat.checkSelfPermission(ProjectPicturesActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    ProjectPicturesActivity.this,
                    PERMISSIONS_STORAGE,/**
                     * 针对安卓6.0以上访问storage文件夹的权限动态管理
                     */
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        titles = FileUtil.getImageNames(apppath+ "/" +projectname);
        if (titles!=null) {//防止文件夹内没有图片报空指针异常
            for(int i=0;i< titles.size();i++) {
                imagepaths.add(apppath+ "/" + projectname+ "/" + titles.get(i));

            }
        }
        addnewpicture = (Button) findViewById(R.id.btn_addnewpicture);
        addnewpicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takephotos();
            }
        });

        gridview = (GridView)findViewById(R.id.gridview);
        gridview.setEmptyView(findViewById(R.id.textview_noprojectpicture));
        pictureadapter = new PictureAdapter(titles,imagepaths,ProjectPicturesActivity.this);
        gridview.setAdapter(pictureadapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(ProjectPicturesActivity.this,"你点击了"+imagepaths.get(i),Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ProjectPicturesActivity.this,ShowimagebyzrxActivity.class);
                intent.putExtra("imagepath",imagepaths.get(i));
                intent.putExtra("projectname",projectname);
                startActivity(intent);
            }
        });
        gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProjectPicturesActivity.this);
                builder.setTitle("确定要删除该照片吗");
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String deleteppicture = imagepaths.get(i);
                        deleteFile(new File(imagepaths.get(i)));
                        for(int i=0;i<imagepaths.size();i++) {
                            if(imagepaths.get(i).equals(deleteppicture)){
                                imagepaths.remove(i);
                            }
                        }
                        titles = FileUtil.getImageNames(apppath+ "/" +projectname);
                        imagepaths = new ArrayList<String>();
                        if (titles!=null) {//防止文件夹内没有图片报空指针异常
                            for(int i=0;i< titles.size();i++) {
                                imagepaths.add(apppath+ "/" + projectname+ "/" + titles.get(i));

                            }
                        }
                        pictureadapter = new PictureAdapter(titles,imagepaths,ProjectPicturesActivity.this);
                        gridview.setAdapter(pictureadapter);
                        //imagepaths.remove(i);
                        //pictureadapter.notifyDataSetChanged();



                    }
                });
                builder.create().show();


                return true;
            }
        });


    }



    //删除文件夹及其内部所有文件的方法
    private void deleteFile(File file) {
        if(file.isDirectory()){
            File[] files= file.listFiles();
            for(int i=0;i<files.length;i++) {
                File f = files[i];
                deleteFile(f);
            }
            file.delete();
        } else if (file.exists()) {
            file.delete();
        }
    }

    private void takephotos() {
        String filename = getFileNameBasedOnTime();

        takephoto = apppath+ "/" + projectname+ "/"+ filename + ".jpg";
        File newImage = new File(takephoto);
        if (newImage != null) {
            Uri newImageUri;
			/*
			安卓7.0 调用系统相机拍照不再允许使用Uri方式，应该替换为FileProvider
			 */
            if (Build.VERSION.SDK_INT >= 24) {
                int checkCallPhonePermission = ContextCompat.checkSelfPermission(ProjectPicturesActivity.this, Manifest.permission.CAMERA);
                if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ProjectPicturesActivity.this,new String[]{Manifest.permission.CAMERA},222);
                    return;
                }else {
                    newImageUri = FileProvider.getUriForFile(this, "com.xf.activity.provider.download", newImage);
                }
            } else {
                newImageUri = Uri.fromFile(newImage);
            }
            Intent toCaptureImage = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            toCaptureImage.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, newImageUri);
            startActivityForResult(toCaptureImage, 111);
            //imagepaths.add(takephoto);
            //pictureadapter.notifyDataSetChanged();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode ==111) {//从系统相机拍照返回时更新gridview
            titles = FileUtil.getImageNames(apppath+ "/" +projectname);
            imagepaths = new ArrayList<String>();
            if (titles!=null) {//防止文件夹内没有图片报空指针异常
                for(int i=0;i< titles.size();i++) {
                    imagepaths.add(apppath+ "/" + projectname+ "/" + titles.get(i));

                }
            }
            pictureadapter = new PictureAdapter(titles,imagepaths,ProjectPicturesActivity.this);
            gridview.setAdapter(pictureadapter);


        }
    }
}

