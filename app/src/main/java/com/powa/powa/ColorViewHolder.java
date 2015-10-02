package com.powa.powa;

import com.powa.ui.swipeview.ViewHolder;

public class ColorViewHolder extends ViewHolder {
private String mColor;
private String mText;

public ColorViewHolder(String color, String text) {
  mColor = color;
  mText = text;
}

public String getColor() {
  return mColor;
}
public String getText() {
  return mText;
}
}
