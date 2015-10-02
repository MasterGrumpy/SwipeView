package com.powa.powa;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.powa.ui.swipeview.SwipeView;
import com.powa.ui.swipeview.ViewHolder;

import java.util.ArrayList;

public class SwipeActivity extends AppCompatActivity {

@Override
protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.activity_swipe);

  SwipeView v = (SwipeView) findViewById(R.id.swipeView);
  ColorViewAdapter adapter = new ColorViewAdapter(this);

  String[] colors = {
    "red", "blue", "yellow", "green", "black", "grey"
  };

  for (int i = 0; i < 30; ++i) {
    ColorViewHolder h = new ColorViewHolder(colors[i%colors.length]);
    h.setOnEventListener(new ViewHolder.OnEventListener() {
      @Override
      public void onLike(ViewHolder viewHolder) {
        Toast.makeText(SwipeActivity.this, "Like",Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onDislike(ViewHolder viewHolder) {
        Toast.makeText(SwipeActivity.this, "DisLike",Toast.LENGTH_SHORT).show();
      }

      @Override
      public void OnClick(ViewHolder viewHolder) {

      }
    });
    adapter.add(h);
  }
  v.setAdapter(adapter);
}

@Override
public boolean onCreateOptionsMenu(Menu menu) {
  // Inflate the menu; this adds items to the action bar if it is present.
  getMenuInflater().inflate(R.menu.menu_swipe, menu);
  return true;
}

@Override
public boolean onOptionsItemSelected(MenuItem item) {
  // Handle action bar item clicks here. The action bar will
  // automatically handle clicks on the Home/Up button, so long
  // as you specify a parent activity in AndroidManifest.xml.
  int id = item.getItemId();

  //noinspection SimplifiableIfStatement
  if (id == R.id.action_settings) {
    return true;
  }

  return super.onOptionsItemSelected(item);
}
}
