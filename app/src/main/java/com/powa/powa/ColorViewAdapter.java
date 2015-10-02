package com.powa.powa;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.powa.ui.swipeview.SwipeViewAdapter;
import com.powa.ui.swipeview.ViewHolder;

public class ColorViewAdapter extends SwipeViewAdapter<ColorViewHolder> {
private int mCpt = 0;

public ColorViewAdapter(Context c) {
  super(c);
}

@Override
public View getView(int position, ColorViewHolder c, View convertView, ViewGroup parent) {
  if (convertView == null) {
    convertView = LayoutInflater.from(getContext()).inflate(R.layout.color_view, parent, false);
  }
  ((TextView)convertView.findViewById(R.id.textView)).setText(c.getText());
  convertView.setBackgroundColor(Color.parseColor(c.getColor()));
  return convertView;
}

@Override
public void onStarve(int position) {
  String[] colors = {
    "red", "blue", "yellow", "green", "black", "grey"
  };

  for (int i = 0; i < position; ++i) {
    mData.remove(0);
  }


  for (int i = 0; i < 10; ++i) {
    ColorViewHolder h = new ColorViewHolder(colors[i%colors.length], ""+mCpt++);
    h.setOnEventListener(new ViewHolder.OnEventListener() {
      @Override
      public void onLike(ViewHolder viewHolder) {
        Toast.makeText(getContext(), "Like", Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onDislike(ViewHolder viewHolder) {
        Toast.makeText(getContext(), "Dislike",Toast.LENGTH_SHORT).show();
      }

      @Override
      public void OnClick(ViewHolder viewHolder) {

      }
    });
    mData.add(h);
  }
  notifyDataSetChanged();
}
}
