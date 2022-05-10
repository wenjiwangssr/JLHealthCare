package com.example.healthcare.ui.view;

import android.graphics.Color;
import android.view.LayoutInflater;

import com.example.healthcare.R;
import com.example.healthcare.bean.ColorBean;

import java.util.List;


/**
 * Created by you on 2017/10/13.
 */

public class TestAdapter extends WheelView.WheelAdapter<UserViewHolder> {

    private final List<ColorBean> colorBeanList;

    public TestAdapter(List<ColorBean> colorBeanList) {
        this.colorBeanList = colorBeanList;
    }

    @Override
    public int getItemCount() {
        return colorBeanList.size();
    }

    @Override
    public UserViewHolder onCreateViewHolder(LayoutInflater inflater, int viewType) {
        return new UserViewHolder(inflater.inflate(R.layout.item_color_picker, null, false));
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        ColorBean item = colorBeanList.get(position);
        holder.tv_name.setText(item.getMesh_name());
        holder.tv_name.setBackgroundColor(Color.argb(item.getA(),item.getR(),item.getG(),item.getB()));
        if (item.getR()*0.299 + item.getG()*0.587 + item.getB()*0.144 <= 0.753 || item.getA() <= 0.4){
            holder.tv_name.setTextColor(Color.WHITE);
        }else{
            holder.tv_name.setTextColor(Color.BLACK);
        }
    }
}
