package com.powa.powa;

import com.powa.ui.swipeview.ViewHolder;

public class ColorViewHolder extends ViewHolder {
private String mColor;

public ColorViewHolder(String color) {
  mColor = color;
}

public String getColor() {
  return mColor;
}
}
