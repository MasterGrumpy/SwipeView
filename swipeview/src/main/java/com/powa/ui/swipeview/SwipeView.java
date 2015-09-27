package com.powa.ui.swipeview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;

import java.util.Stack;

public class SwipeView extends AdapterView<SwipeViewAdapter> {

private static final int INVALID_POINTER_ID = -1;
private int mActivePointerId = INVALID_POINTER_ID;
private SwipeViewAdapter mAdapter;
private int mMaxViewPreloaded = 4;

private float mLastTouchX;
private float mLastTouchY;

private View mCurrentView;
private ViewHolder mCurrentViewHolder;

private int mTouchSlop;
private int mCurrentAdapterPosition;
private VelocityTracker mVelocityTracker;

private Stack<View> mRecyclerViews = new Stack<>();

private final DataSetObserver mDataSetObserver = new DataSetObserver() {
  @Override
  public void onChanged() {
    super.onChanged();
    clear();
    ensureFull();
  }

  @Override
  public void onInvalidated() {
    super.onInvalidated();
    clear();
  }
};

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
  setCurrentView(null, null);
  mAdapter = adapter;
  mCurrentAdapterPosition = 0;
  adapter.registerDataSetObserver(mDataSetObserver);

  ensureFull();
}

private void ensureFull() {
  for (int pos = getChildCount(); pos + mCurrentAdapterPosition < mAdapter.getCount() && pos < mMaxViewPreloaded; ++pos) {

    View view = null;
    if (!mRecyclerViews.isEmpty()) {
      view = mRecyclerViews.pop();
    }
    view = mAdapter.getView(pos + mCurrentAdapterPosition, view, this);

    view.setLayerType(LAYER_TYPE_SOFTWARE, null);
    addViewInLayout(view, 0, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT), true);
  }

  if (getChildCount() != 0) {
    setCurrentView(getChildAt(getChildCount() - 1), (ViewHolder) getAdapter().getItem(mCurrentAdapterPosition));
  }
  requestLayout();
}

private void setCurrentView(View view, ViewHolder viewHolder) {
  mCurrentView = view;
  mCurrentViewHolder = viewHolder;

  if (mCurrentView != null) {
    mCurrentView.setLayerType(LAYER_TYPE_HARDWARE, null);
    mCurrentView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mCurrentViewHolder.getEventListener() != null) {
          mCurrentViewHolder.getEventListener().OnClick(mCurrentViewHolder);
        }
      }
    });
  }
}

private void clear() {
  removeAllViewsInLayout();
  mCurrentAdapterPosition = 0;
  setCurrentView(null, null);
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
  if (mCurrentView == null || event == null) {
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

      // filter micro movements
      if (Math.abs(dx) > mTouchSlop && Math.abs(dy) > mTouchSlop) {
        return true;
      }

      mCurrentView.setTranslationX(mCurrentView.getTranslationX() + dx);
      mCurrentView.setTranslationY(mCurrentView.getTranslationY() + dy);
      mCurrentView.setRotation(45 * mCurrentView.getTranslationX() / (getWidth() / 2.f));

      mLastTouchX = x;
      mLastTouchY = y;
      break;
    }
    case MotionEvent.ACTION_UP:
    case MotionEvent.ACTION_CANCEL: {
      mVelocityTracker.computeCurrentVelocity(1);

      float xVelocity = mVelocityTracker.getXVelocity(mActivePointerId);
      boolean gotoNextView = Math.abs(xVelocity) > 0.5;

      if (gotoNextView) {
        dismissCurrentView(xVelocity > 0);
      } else {
        mCurrentView.animate().
            setDuration(250).
            translationX(0).
            translationY(0).
            rotation(0).
            setInterpolator(new AccelerateInterpolator());
      }

      mActivePointerId = INVALID_POINTER_ID;
      mVelocityTracker.recycle();
      mVelocityTracker = null;
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
  }

  return true;
}

@Override
public boolean onInterceptTouchEvent(MotionEvent event) {
  switch (event.getActionMasked()) {
    case MotionEvent.ACTION_MOVE:
      return true;

    case MotionEvent.ACTION_DOWN:
      int pointerIndex = event.getActionIndex();
      float x = event.getX(pointerIndex);
      float y = event.getY(pointerIndex);
      mLastTouchX = x;
      mLastTouchY = y;
      mActivePointerId = event.getPointerId(pointerIndex);
      mVelocityTracker = VelocityTracker.obtain();
      break;
  }

  return false;
}

private void recycleView(View v) {
  if (mRecyclerViews.size() < 10) {
    mRecyclerViews.add(v);
  }
  removeViewInLayout(v);
}

@Override
public View getSelectedView() {
  throw new UnsupportedOperationException();
}

@Override
public void setSelection(int position) {
  throw new UnsupportedOperationException();
}

public void dismissCurrentView(boolean like) {
  ViewHolder viewHolder = (ViewHolder) mAdapter.getItem(mCurrentAdapterPosition);
  if (viewHolder.getEventListener() != null) {
    if (like) viewHolder.getEventListener().onLike(viewHolder);
    else viewHolder.getEventListener().onDislike(viewHolder);
  }

  float progress = (mCurrentView.getWidth() - Math.abs(mCurrentView.getTranslationX())) / (float) mCurrentView.getWidth();
  if (progress < 0) progress = 0;
  if (progress > 1) progress = 1;

  int sign = like ? 1 : -1;

  mCurrentView.animate().
      setDuration((long) (500 * progress)).
      translationXBy(Math.copySign(getWidth() * 2, sign)).
      translationYBy(getHeight() / 4).
      rotation(Math.copySign(45, sign)).
      setInterpolator(new AccelerateInterpolator()).
      setListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationCancel(Animator animation) {
          onAnimationEnd(animation);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
          recycleView(mCurrentView);
          ++mCurrentAdapterPosition;
          ensureFull();
        }
      });
}
}
