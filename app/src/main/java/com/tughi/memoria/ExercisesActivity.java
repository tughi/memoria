package com.tughi.memoria;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

public class ExercisesActivity extends AppCompatActivity {

    public static final String FRAGMENT_TAG = "exercises";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag("exercises") == null) {
            fragmentManager.beginTransaction()
                    .replace(android.R.id.content, new ExercisesFragment(), FRAGMENT_TAG)
                    .commit();
        }
    }

}
