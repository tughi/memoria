package com.tughi.memoria;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Handler.Callback, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String[] ITEMS_PROJECTION = {
            Items.Columns.ID,
            Items.Columns.PROBLEM,
            Items.Columns.SOLUTION,
            Items.Columns.RATING,
    };
    private static final String ITEMS_SORT_ORDER = Items.Columns.RATING + ", " + Items.Columns.TESTED;
    private static final int ITEM_ID = 0;
    private static final int ITEM_PROBLEM = 1;
    private static final int ITEM_SOLUTION = 2;
    private static final int ITEM_RATING = 3;

    private Cursor itemsCursor;

    private DrawerLayout drawerLayout;
    private View drawerView;

    private Handler practiceHandler;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        practiceHandler = new Handler(this);

        setContentView(R.layout.main_activity);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerView = findViewById(R.id.drawer);
        drawerView.findViewById(R.id.practice).setOnClickListener(this);
        drawerView.findViewById(R.id.knowledge).setOnClickListener(this);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.practice:
                replacePracticeFragment();
                break;
            case R.id.knowledge:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content, new ItemsFragment())
                        .commit();
                break;
        }

        drawerLayout.closeDrawer(drawerView);
    }

    @Override
    public boolean handleMessage(Message message) {
        replacePracticeFragment();

        return true;
    }

    private void replacePracticeFragment() {
        if (itemsCursor != null && itemsCursor.moveToFirst()) {
            PracticeFragment practiceFragment;
            int chance = random.nextInt(10);
            if (chance < 4) {
                practiceFragment = new SolutionPickerFragment();
            } else {
                practiceFragment = new ProblemPickerFragment();
            }

            Bundle args = new Bundle();
            args.putLong(Items.Columns.ID, itemsCursor.getLong(ITEM_ID));
            args.putString(Items.Columns.PROBLEM, itemsCursor.getString(ITEM_PROBLEM));
            args.putString(Items.Columns.SOLUTION, itemsCursor.getString(ITEM_SOLUTION));
            args.putInt(Items.Columns.RATING, itemsCursor.getInt(ITEM_RATING));
            practiceFragment.setArguments(args);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, practiceFragment)
                    .commit();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, Items.CONTENT_URI, ITEMS_PROJECTION, null, null, ITEMS_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        itemsCursor = cursor;

        if (getSupportFragmentManager().findFragmentById(R.id.content) == null) {
            continuePractice(PracticeFragment.NEXT_PROBLEM_IMMEDIATELY);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        itemsCursor = null;
    }

    public void continuePractice(int when) {
        practiceHandler.sendEmptyMessageDelayed(0, when);
    }

}
