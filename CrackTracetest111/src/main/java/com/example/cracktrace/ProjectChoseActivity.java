package com.example.cracktrace;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.cracktrace.R.id.textview_noproject;

public class ProjectChoseActivity extends AppCompatActivity {
    private Button btn_createnewproject;//创建新项目按钮
    final static String apppath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/CrackProject";
    private String projectpath;//创建项目文件夹的完整路径
    private ListView allprojects;
    private String project_item;//打开上下文菜单时所点击的listview中的项
    private ArrayList<String> projectlist = new ArrayList<>();
    private ArrayAdapter arrayadapter;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projectchose);
        /**
         * 针对安卓6.0以上访问storage文件夹的权限动态管理
         */
        if (ActivityCompat.checkSelfPermission(ProjectChoseActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    ProjectChoseActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        btn_createnewproject = (Button) findViewById(R.id.btn_createnewproject);
        btn_createnewproject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateProject();
            }
        });

        initlistview();
        allprojects.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ProjectChoseActivity.this,ProjectPicturesActivity.class);
                intent.putExtra("projectname",projectlist.get(position));
                startActivity(intent);

            }
        });
        //给ListView注册上下文菜单
        registerForContextMenu(allprojects);






    }



    //初始化listview
    private  void initlistview(){
        loadproject();
        allprojects = (ListView) findViewById(R.id.listview_showproject);
        allprojects.setEmptyView(findViewById(R.id.textview_noproject));
        arrayadapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,projectlist);
        allprojects.setAdapter(arrayadapter);
    }


    //设置创建新项目按钮的onclick方法
    private void CreateProject(){
        View view = getLayoutInflater().inflate(R.layout.createnewproject_dialog_view,null);


        final EditText edittext_projectname = (EditText) view.findViewById(R.id.edittext_projectname);
        //final EditText edittext_projectname = new EditText(this);
        edittext_projectname.setHint("请输入项目名");
        final AlertDialog.Builder builder_newproject = new AlertDialog.Builder(this);
        builder_newproject.setTitle("创建新项目");
        builder_newproject.setView(view);
        builder_newproject.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


            }
        });
        builder_newproject.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(edittext_projectname.getText().toString()!=null&&!edittext_projectname.getText().toString().equals(" ")) {
                    //创建项目完整路径
                    projectpath = apppath+ "/" + edittext_projectname.getText().toString();
                    File directory = new File(apppath);
                    if (!directory.exists()) {
                        directory.mkdir();
                    }
                    File file_newproject = new File(projectpath);
                    if (file_newproject.exists()) {
                        Toast.makeText(ProjectChoseActivity.this, "该项目已存在", Toast.LENGTH_LONG).show();


                    } else {
                        file_newproject.mkdir();
                        Toast.makeText(ProjectChoseActivity.this, "项目创建成功", Toast.LENGTH_LONG).show();
                        //通知listview更改数据源；
                        projectlist.add(file_newproject.getName());
                        Collections.sort(projectlist);
                        if(arrayadapter!=null) {
                            arrayadapter.notifyDataSetChanged();
                        } else{
                            arrayadapter = new ArrayAdapter(ProjectChoseActivity.this,android.R.layout.simple_list_item_1,projectlist);

                        }
                    }
                }else {
                    Toast.makeText(ProjectChoseActivity.this, "什么都没发生", Toast.LENGTH_LONG).show();
                }


            }

        });
        builder_newproject.create().show();




    }

    private void loadproject(){
        File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/CrackProject");
        if(!directory.exists()){
            directory.mkdir();
        }
        if(directory!=null){
            String[] allprojects = directory.list();
            if (allprojects!=null) {
                for(String project:allprojects) {
                    projectlist.add(project);
                    Collections.sort(projectlist);

                }
            }
        }
    }

    //覆盖方法，创建上下文菜单。上下文菜单基本上都是长按事件,在一个控件上长按,就会弹出一个菜单
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.projectchose_menu,menu);
    }
    //覆盖方法，点击contextmenu时候所进行的操作
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //在onContextItemSelected方法中，通过AdapterContextMenuInfo的实例可以得到当前所点击item的position、id等信息
        AdapterView.AdapterContextMenuInfo menuinfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        //menuinfo.position就是所点击的listview的item的position;
        //获得长按弹出上下文时所点击的项目名称
        project_item = allprojects.getItemAtPosition(menuinfo.position).toString();
        switch (item.getItemId()){
            case R.id.project_rename:
                //重命名
                break;
            case R.id.project_remove:
                //删除项目
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("确定要删除该项目吗");
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteFile(new File(apppath+"/"+project_item));
                        for(int i=0;i<projectlist.size();i++) {
                            if(projectlist.get(i).equals(project_item)){
                                projectlist.remove(i);
                            }
                        }
                        arrayadapter.notifyDataSetChanged();



                    }
                });
                builder.create().show();

                break;


        }
        return super.onContextItemSelected(item);
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
}
