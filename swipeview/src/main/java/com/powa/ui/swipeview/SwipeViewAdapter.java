package com.powa.ui.swipeview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class SwipeViewAdapter<T extends ViewHolder> extends BaseAdapter {

protected List<T> mData;
private Context mContext;

public SwipeViewAdapter(Context c) {
  mContext = c;
  mData = new ArrayList<>();
}

public Context getContext() {
  return mContext;
}

public void add(T item) {
  mData.add(item);
  notifyDataSetChanged();
}

public void remove(T item) {
  mData.remove(item);
  notifyDataSetChanged();
}

public void setItems(List<T> items) {
  mData = items;
  notifyDataSetChanged();
}

@Override
public Object getItem(int position) {
  if (position < 0 || position >= mData.size()) {
    return null;
  }
  return mData.get(position);
}

@Override
public int getCount() {
  return mData.size();
}

@Override
public long getItemId(int position) {
  return getItem(position).hashCode();
}

@Override
public View getView(int position, View convertView, ViewGroup parent) {
  return getView(position, (T) getItem(position), convertView, parent);
}

public abstract View getView(int position, T viewHolder, View convertView, ViewGroup parent);

}
