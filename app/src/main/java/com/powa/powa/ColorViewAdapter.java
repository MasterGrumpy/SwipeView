package com.powa.powa;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.powa.ui.swipeview.SwipeViewAdapter;

public class ColorViewAdapter extends SwipeViewAdapter<ColorViewHolder> {

public ColorViewAdapter(Context c) {
  super(c);
}

@Override
public View getView(int position, ColorViewHolder c, View convertView, ViewGroup parent) {
  if (convertView == null) {
    convertView = LayoutInflater.from(getContext()).inflate(R.layout.color_view, parent, false);
  }
  convertView.setBackgroundColor(Color.parseColor(c.getColor()));
  return convertView;
}
}
