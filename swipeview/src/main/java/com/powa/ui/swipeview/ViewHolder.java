package com.powa.ui.swipeview;

import android.view.View;

public class ViewHolder {
private OnEventListener mOnEventListener = null;
private View mView = null;

public View getView() {
  return mView;
}

public void setView(View v) {
  mView = v;
}

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
