package com.powa.ui.swipeview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class SwipeView extends AdapterView<SwipeViewAdapter> {

private static final int INVALID_POINTER_ID = -1;

private int mActivePointerId = INVALID_POINTER_ID;
private SwipeViewAdapter mAdapter;
private int mMaxViewPreloaded = 4;
private float mLastTouchX;
private float mLastTouchY;
private ViewHolder mCurrentViewHolder;
private View mCurrentView;
private View mNextView;
private int mTouchSlop;
private int mCurrentAdapterPosition;
private VelocityTracker mVelocityTracker;
private Stack<View> mRecyclerViews = new Stack<>();

private List<View> mAllViews = new ArrayList<>();

private final DataSetObserver mDataSetObserver = new DataSetObserver() {
  @Override
  public void onChanged() {
    super.onChanged();
    int currentItemPosition = mAdapter.getItemPosition(mCurrentViewHolder);
    if (currentItemPosition == -1) {
      clear();
    } else {
      mCurrentAdapterPosition = currentItemPosition;
      int childCount = getChildCount();
      List<View> toRemove = new ArrayList<>();
      for (int i = 0; i < childCount; ++i) {
        View v = getChildAt(childCount - i - 1);
        ViewHolder h = (ViewHolder) mAdapter.getItem(i);
        if (h == null || h.getView() == null || v != h.getView()) {
          toRemove.add(v);
        }
      }

      for (View v : toRemove) {
        mAllViews.remove(v);
        recycleView(v);
      }
    }
    ensureFull();
    initCurrentView();
  }

  @Override
  public void onInvalidated() {
    super.onInvalidated();
    clear();
  }
};
private SwipeListener mSwipeListener = new DefaultSwipeListener();

public SwipeView(Context context) {
  super(context);
  init();

}

public SwipeView(Context context, AttributeSet attrs) {
  super(context, attrs);
  initFromXml(attrs);
  init();
}

public SwipeView(Context context, AttributeSet attrs, int defStyle) {
  super(context, attrs, defStyle);
  initFromXml(attrs);
  init();
}

private void init() {
  ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
  mTouchSlop = viewConfiguration.getScaledTouchSlop();
  mVelocityTracker = null;
}

private void initFromXml(AttributeSet attr) {
  TypedArray a = getContext().obtainStyledAttributes(attr,
          R.styleable.SwipeView);

  setMaxViewPreloaded(a.getInteger(R.styleable.SwipeView_maxViewPreloaded, 4));
  a.recycle();
}

@Override
public SwipeViewAdapter getAdapter() {
  return mAdapter;
}

@Override
public void setAdapter(SwipeViewAdapter adapter) {
  if (mAdapter != null)
    mAdapter.unregisterDataSetObserver(mDataSetObserver);

  clear();
  mCurrentAdapterPosition = 0;
  mAdapter = adapter;

  ensureFull();
  initCurrentView();

  adapter.registerDataSetObserver(mDataSetObserver);
}

public void setSwipeListener(SwipeListener l) {
  mSwipeListener = l;
}

private void ensureFull() {
  int currentAdapterPosition = mCurrentAdapterPosition;

  int i = 0;
  while (mAllViews.size() < mMaxViewPreloaded) {
    View view = null;
    if (!mRecyclerViews.isEmpty()) {
      view = mRecyclerViews.pop();
    }
    view = mAdapter.getView(currentAdapterPosition+mAllViews.size(), view, this);
    if (view == null) break;

    view.setVisibility(GONE);
    view.setLayerType(LAYER_TYPE_SOFTWARE, null);
    addViewInLayout(view, 0, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT), true);
    mAllViews.add(view);
  }

  requestLayout();
}

private void initCurrentView() {
  if (mCurrentAdapterPosition < 0 || mAdapter == null || mCurrentAdapterPosition >= getAdapter().getCount()) {
    mCurrentView = null;
    mCurrentViewHolder = null;
    mNextView = null;
    return;
  }

  mCurrentViewHolder = (ViewHolder) getAdapter().getItem(mCurrentAdapterPosition);
  mCurrentView = mCurrentViewHolder.getView();

  // case where we didn't create yet the view, but adapter isn't empty
  if (mCurrentView == null) {
    mNextView = null;
    return;
  }

  mNextView = getChildAt(getPositionForView(mCurrentView) - 1);

  if (mCurrentView != null) {
    mCurrentView.setLayerType(LAYER_TYPE_HARDWARE, null);
    mCurrentView.setVisibility(VISIBLE);
    mCurrentView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mCurrentViewHolder.getEventListener() != null) {
          mCurrentViewHolder.getEventListener().OnClick(mCurrentViewHolder);
        }
      }
    });
  }

  if (mNextView != null) {
    mNextView.setVisibility(VISIBLE);
    mNextView.setLayerType(LAYER_TYPE_HARDWARE, null);
  }
}

private void checkStarvation() {
  if (mCurrentAdapterPosition + mMaxViewPreloaded >= mAdapter.getCount()) {
    mAdapter.onStarve(mCurrentAdapterPosition);
  }
}

private void clear() {
  removeAllViewsInLayout();
  mAllViews.clear();
  mCurrentAdapterPosition = 0;
  mCurrentView = null;
  mCurrentViewHolder = null;
  mNextView = null;
}

public int getMaxViewPreloaded() {
  return mMaxViewPreloaded;
}

public void setMaxViewPreloaded(int max) {
  mMaxViewPreloaded = Math.max(2, max);
}

@Override
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
  super.onMeasure(widthMeasureSpec, heightMeasureSpec);

  int requestedWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
  int requestedHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();

  int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(requestedWidth, MeasureSpec.AT_MOST);
  int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(requestedHeight, MeasureSpec.AT_MOST);

  for (int i = 0; i < getChildCount(); ++i) {
    View child = getChildAt(i);
    if (child != null) {
      child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }
  }
}

@Override
protected void onLayout(boolean changed, int l, int t, int r, int b) {
  super.onLayout(changed, l, t, r, b);

  Rect boundsRect = new Rect();
  Rect childRect = new Rect();
  for (int i = 0; i < getChildCount(); ++i) {
    boundsRect.set(0, 0, getWidth(), getHeight());

    View view = getChildAt(i);
    Gravity.apply(Gravity.CENTER, view.getMeasuredWidth(), view.getMeasuredHeight(), boundsRect, childRect);
    view.layout(childRect.left, childRect.top, childRect.right, childRect.bottom);
  }
}

@Override
public boolean onTouchEvent(MotionEvent event) {
  if (mCurrentView == null) {
    return false;
  }

  switch (event.getActionMasked()) {
    case MotionEvent.ACTION_DOWN:
      break;

    case MotionEvent.ACTION_MOVE: {
      mVelocityTracker.addMovement(event);

      int pointerIndex = event.findPointerIndex(mActivePointerId);
      float x = event.getX(pointerIndex);
      float y = event.getY(pointerIndex);

      float dx = x - mLastTouchX;
      float dy = y - mLastTouchY;
      float xProgress = Math.max(-1f, Math.min(1.f, mCurrentView.getTranslationX() / (float) mCurrentView.getWidth()));
      float yProgress = Math.max(-1.f, Math.min(1.f, mCurrentView.getTranslationY() / (float) mCurrentView.getHeight()));
      mSwipeListener.OnSwipe(mCurrentView, mNextView, xProgress, yProgress, dx, dy);
      mLastTouchX = x;
      mLastTouchY = y;
      break;
    }

    case MotionEvent.ACTION_POINTER_UP: {
      int pointerIndex = event.getActionIndex();
      int pointerId = event.getPointerId(pointerIndex);

      if (pointerId == mActivePointerId) {
        int newPointerIndex = event.getPointerCount() - 1;
        mLastTouchX = event.getX(newPointerIndex);
        mLastTouchY = event.getY(newPointerIndex);

        mActivePointerId = event.getPointerId(newPointerIndex);
      }
      break;
    }

    case MotionEvent.ACTION_UP:
    case MotionEvent.ACTION_CANCEL: {
      mVelocityTracker.computeCurrentVelocity(1);

      float xVelocity = mVelocityTracker.getXVelocity(mActivePointerId);
      float yVelocity = mVelocityTracker.getYVelocity(mActivePointerId);
      float xProgress = Math.max(-1f, Math.min(1.f, mCurrentView.getTranslationX() / (float) mCurrentView.getWidth()));
      float yProgress = Math.max(-1.f, Math.min(1.f, mCurrentView.getTranslationY() / (float) mCurrentView.getHeight()));

      SwipeListener.SwipeEvent swipeEvent = mSwipeListener.OnSwipeEnd(mCurrentView, mNextView, xProgress, yProgress, xVelocity, yVelocity);

      switch (swipeEvent) {
        case NONE:
          mCurrentView.animate().
                  setDuration(250).
                  translationX(0).
                  translationY(0).
                  rotation(0).
                  setInterpolator(new DecelerateInterpolator());
          break;
        case DISLIKE:
          dismissCurrentView(false);
          break;
        case LIKE:
          dismissCurrentView(true);
          break;
      }

      mActivePointerId = INVALID_POINTER_ID;
      mVelocityTracker.recycle();
      mVelocityTracker = null;
      break;
    }
  }
  return true;
}

@Override
public boolean onInterceptTouchEvent(MotionEvent event) {
  switch (event.getActionMasked()) {
    case MotionEvent.ACTION_MOVE: {
      int pointerIndex = event.findPointerIndex(mActivePointerId);
      float x = event.getX(pointerIndex);
      float y = event.getY(pointerIndex);

      float dx = x - mLastTouchX;
      float dy = y - mLastTouchY;

      // filter micro movements
      if (Math.abs(dx) > mTouchSlop || Math.abs(dy) > mTouchSlop) {
        mSwipeListener.OnSwipeStart(mCurrentView, mNextView);
        return true;
      }
      return false;
    }


    case MotionEvent.ACTION_DOWN: {
      int pointerIndex = event.getActionIndex();
      float x = event.getX(pointerIndex);
      float y = event.getY(pointerIndex);
      mLastTouchX = x;
      mLastTouchY = y;
      mActivePointerId = event.getPointerId(pointerIndex);
      mVelocityTracker = VelocityTracker.obtain();
      break;
    }
  }

  return false;
}

private void recycleView(View v) {
  removeViewInLayout(v);
  //TODO fix recycling
  if (mRecyclerViews.size() < 0) {
    mRecyclerViews.add(v);
    v.setRotation(0);
    v.setTranslationX(0);
    v.setTranslationY(0);
  }
}

@Override
public View getSelectedView() {
  throw new UnsupportedOperationException();
}

@Override
public void setSelection(int position) {
  throw new UnsupportedOperationException();
}

public void dismissCurrentView(final boolean like) {
  final ViewHolder viewHolder = (ViewHolder) mAdapter.getItem(mCurrentAdapterPosition);
  if (viewHolder == null) {
    return;
  }

  int sign = like ? 1 : -1;
  final View view = mCurrentView;

  mAllViews.remove(view);
  ++mCurrentAdapterPosition;
  ensureFull();
  initCurrentView();

  view.animate().
          setDuration(500).
          translationXBy(Math.copySign(getWidth() * 1.5f, sign)).
          translationYBy(getHeight() / 4).
          rotation(Math.copySign(45, sign)).
          setInterpolator(new DecelerateInterpolator()).
          setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
              onAnimationEnd(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
              //TODO fix issue when swipe too fast
              recycleView(view);

              if (viewHolder.getEventListener() != null) {
                if (like) viewHolder.getEventListener().onLike(viewHolder);
                else viewHolder.getEventListener().onDislike(viewHolder);
              }
              checkStarvation();
            }
          });

}

public class DefaultSwipeListener implements SwipeListener {
  @Override
  public void OnSwipeStart(View currentView, View nextView) {
  }

  @Override
  public void OnSwipe(View currentView, View nextView, float px, float py, float dx, float dy) {
    currentView.setTranslationX(currentView.getTranslationX() + dx);
    currentView.setTranslationY(currentView.getTranslationY() + dy);
    currentView.setRotation(45 * px);
  }

  @Override
  public SwipeEvent OnSwipeEnd(View currentView, View nextView, float px, float py, float vx, float vy) {
    // TODO(grumpy-dev): make them parameters
    final float minVelocity = 0.5f;
    final float minProgress = 0.2f;
    final float minStandingProgress = 0.4f;

    boolean likeView = false;
    boolean dislikeView = false;

    // fast to the right, card already on the right side
    likeView |= vx > minVelocity && px > minProgress;
    // no more velocity, but card extremely to the right
    likeView |= px > minStandingProgress;

    // fast to the left, card already on the left side
    dislikeView |= vx < -minVelocity && px < -minProgress;
    // no more velocity, but card extremely to the left
    dislikeView |= px < -minStandingProgress;

    return likeView ? SwipeEvent.LIKE : dislikeView ? SwipeEvent.DISLIKE : SwipeEvent.NONE;
  }
}

}
