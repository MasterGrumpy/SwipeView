package com.powa.ui.swipeview;

import android.view.View;

public interface SwipeListener {
void OnSwipeStart(ViewHolder currentView, ViewHolder nextView);
void OnSwipe(ViewHolder currentView, ViewHolder nextView, float px, float py, float dx, float dy);
SwipeEvent OnSwipeEnd(ViewHolder currentView, ViewHolder nextView, float px, float py, float vx, float vy);

enum SwipeEvent {
  NONE,
  LIKE,
  DISLIKE
}
}

