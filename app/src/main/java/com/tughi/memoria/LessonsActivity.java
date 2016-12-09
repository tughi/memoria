package com.tughi.memoria;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

public class LessonsActivity extends AppCompatActivity {

    public static final String FRAGMENT_TAG = "lessons";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(FRAGMENT_TAG) == null) {
            fragmentManager.beginTransaction()
                    .replace(android.R.id.content, new LessonsFragment(), FRAGMENT_TAG)
                    .commit();
        }
    }

}
