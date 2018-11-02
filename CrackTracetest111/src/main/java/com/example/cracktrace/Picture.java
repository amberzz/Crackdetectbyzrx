package com.example.cracktrace;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *用于操作项目路径下的图片的类
 */
class Picture {
    private String title;
    private String imageid;

    public Picture(String title, String imageid) {
        this.title = title;
        this.imageid = imageid;
    }

    public Picture() {
        super();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageid() {
        return imageid;
    }

    public void setImageid(String imageid) {
        this.imageid = imageid;
    }
}

class FileUtil {
    public static List<String> getImageNames(String foldpath){
        File file = new File(foldpath);
        String[] filename = file.list();
        List<String> filenameList = Arrays.asList(filename);//将数组转化为集合（借用Arrays.asList方法）
        return filenameList;

    }

    private static boolean isImageFile(String filename) {
        String fileend = filename.substring(filename.lastIndexOf(".")+1,filename.length());
        if(fileend.equalsIgnoreCase("jpg")||fileend.equalsIgnoreCase("bmp")
                || fileend.equalsIgnoreCase("png")){
            return true;
        } else {
            return false;
        }
    }
}
