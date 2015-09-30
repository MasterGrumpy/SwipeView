package com.powa.ui.swipeview;

import android.view.View;

public interface SwipeListener {
void OnSwipeStart(View currentView, View nextView);

void OnSwipe(View currentView, View nextView, float px, float py, float dx, float dy);

SwipeEvent OnSwipeEnd(View currentView, View nextView, float px, float py, float vx, float vy);

enum SwipeEvent {
  NONE,
  LIKE,
  DISLIKE
}
}

