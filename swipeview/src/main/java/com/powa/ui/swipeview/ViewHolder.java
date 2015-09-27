package com.powa.ui.swipeview;

public class ViewHolder {
private OnEventListener mOnEventListener = null;

public OnEventListener getEventListener() {
  return mOnEventListener;
}

public void setOnEventListener(OnEventListener listener) {
  mOnEventListener = listener;
}

public interface OnEventListener {
  void onLike(ViewHolder viewHolder);
  void onDislike(ViewHolder viewHolder);
  void OnClick(ViewHolder viewHolder);
}

}
