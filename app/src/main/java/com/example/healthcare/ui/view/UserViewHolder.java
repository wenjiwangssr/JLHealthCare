package com.example.healthcare.ui.view;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.healthcare.R;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;


/**
 * Created by you on 2017/10/13.
 */

public class UserViewHolder extends RecyclerView.ViewHolder {

    public final QMUIRoundButton tv_name;

    public UserViewHolder(View itemView) {
        super(itemView);
        tv_name = (QMUIRoundButton) itemView.findViewById(R.id.text);
    }
}
