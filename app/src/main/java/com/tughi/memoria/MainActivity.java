package com.tughi.memoria;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Handler.Callback {

    private DrawerLayout drawerLayout;
    private View drawerView;

    private Handler practiceHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        practiceHandler = new Handler(this);

        setContentView(R.layout.main_activity);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerView = findViewById(R.id.drawer);
        drawerView.findViewById(R.id.practice).setOnClickListener(this);
        drawerView.findViewById(R.id.knowledge).setOnClickListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentById(R.id.content) == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.content, new SolutionsPracticeFragment())
                    .commit();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.practice:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content, new SolutionsPracticeFragment())
                        .commit();
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
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, new SolutionsPracticeFragment())
                .commit();

        return true;
    }

    public void continuePractice(boolean correct) {
        practiceHandler.sendEmptyMessageDelayed(0, correct ? 500 : 1000);
    }

}
