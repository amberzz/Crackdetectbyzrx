package com.example.cracktrace;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.BitmapFactory;
import android.support.v7.widget.TintContextWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class PictureAdapter extends BaseAdapter {
      private LayoutInflater inflater;
      private List<Picture> pictures;
      public PictureAdapter(List<String> titles, List<String> images, Context context) {
          super();
          pictures = new ArrayList<Picture>();
          inflater = LayoutInflater.from(context);
          if (images!=null) {
              for(int i =0;i<images.size();i++) {
                  Picture picture = new Picture(titles.get(i),images.get(i));
                  pictures.add(picture);
              }
          }

      }

    @Override
    public int getCount() {
        if(null!=pictures) {
            return pictures.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int i) {
        return pictures.get(i);
    }

    @Override
      public long getItemId(int i) {
          return i;
      }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewholder;
        if (view == null) {
            view = inflater.inflate(R.layout.picturegridview_item,null);
            viewholder = new ViewHolder();
            viewholder.image = (ImageView) view.findViewById(R.id.image);
            view.setTag(viewholder);
        } else {
            viewholder = (ViewHolder)view.getTag();
        }

        viewholder.image.setImageBitmap(BitmapFactory.decodeFile(pictures.get(i).getImageid()));
        int width = getrequiredactivity(viewholder.image).getWindowManager().getDefaultDisplay().getWidth();
        ViewGroup.LayoutParams params = viewholder.image.getLayoutParams();
        //设置图片的相对于屏幕的宽高比
        params.width = width/2;
        params.height = width/3 ;
        viewholder.image.setLayoutParams(params);



        return view;
    }
    private Activity getrequiredactivity(View view) {
          Context context = view.getContext();
          while(context instanceof ContextWrapper) {
              if (context instanceof Activity){
                  return (Activity)context;
              }
              context = ((ContextWrapper)context).getBaseContext();
          }
          return null;

    }
}

class ViewHolder {
    public ImageView image;
}