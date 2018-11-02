package com.example.cracktrace;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

public class MediaScanner {
	private MediaScannerConnection mediaScanConn = null;  
	private ImageSannerClient client = null;  
	private String filePath = null;  
	private String fileType = null;   
	 
	public MediaScanner(Context context) {  
	    //创建MusicSannerClient  
	    if (client == null) {  
	        client = new ImageSannerClient();  
	    }   
        if (mediaScanConn == null) {  
	        mediaScanConn = new MediaScannerConnection(context, client);  
	    }  
	}  
	
	class ImageSannerClient implements MediaScannerConnection.MediaScannerConnectionClient { 
		public void onMediaScannerConnected() { 
		    if(filePath != null){
                mediaScanConn.scanFile(filePath, fileType);  
            }  

		    filePath = null;
		    fileType = null;  
        }  

        public void onScanCompleted(String path, Uri uri) {  
            // TODO Auto-generated method stub  
            mediaScanConn.disconnect();  
        }  

    }
	public void scanFile(String filepath,String fileType) {
		this.filePath = filepath;
		this.fileType = fileType;  
        //连接之后调用MusicSannerClient的onMediaScannerConnected()方法  
        mediaScanConn.connect();  
    }
	
	public String getFilePath() {
		return filePath;  
    }  
  
    public void setFilePath(String filePath) {
    	this.filePath = filePath;  
    }  
  
    public String getFileType() {
    	return fileType;  
    }  
  
    public void setFileType(String fileType) {
    	this.fileType = fileType;  
    }
}
