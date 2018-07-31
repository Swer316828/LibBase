package com.sfh.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * 功能描述: TODO
 *
 * @author SunFeihu 孙飞虎
 * @date 2018/7/28
 */
public class ItemAdapter extends BaseAdapter {

    List<File> files;
    Context context;

    public ItemAdapter(Context context, List<File> files) {
        super();
        this.context = context;
        this.files = files;
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public File getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_adapter, parent, false);
        }
        TextView tvName = convertView.findViewById(R.id.tvName);
        TextView tvPath = convertView.findViewById(R.id.tvPath);

        tvName.setText(getItem(position).getName());
        tvPath.setText(getItem(position).getAbsolutePath());
        return convertView;
    }
}
