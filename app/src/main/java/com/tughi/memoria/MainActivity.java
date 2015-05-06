package com.tughi.memoria;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private ItemsFragment itemsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fragmentManager = getSupportFragmentManager();
        itemsFragment = (ItemsFragment) fragmentManager.findFragmentByTag("items");
        if (itemsFragment == null) {
            itemsFragment = new ItemsFragment();
            fragmentManager.beginTransaction()
                    .add(android.R.id.content, itemsFragment, "items")
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                startActivity(new Intent(this, ItemEditActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
