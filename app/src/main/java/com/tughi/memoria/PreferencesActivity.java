package com.tughi.memoria;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;

public class PreferencesActivity extends AppCompatActivity {

    public static final String PREFERENCE_SERVER_URL = "server_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(android.R.id.content);
        if (fragment == null) {
            fragmentManager.beginTransaction()
                    .replace(android.R.id.content, Fragment.instantiate(this, PreferencesFragment.class.getName()))
                    .commit();
        }
    }

    public static class PreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private Preference serverUrlPreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            PreferenceScreen preferenceScreen = getPreferenceScreen();
            SharedPreferences preferences = preferenceScreen.getSharedPreferences();

            serverUrlPreference = findPreference(PREFERENCE_SERVER_URL);

            onSharedPreferenceChanged(preferences, PREFERENCE_SERVER_URL);

            preferences.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (PREFERENCE_SERVER_URL.equals(key)) {
                String serverUrl = sharedPreferences.getString(PREFERENCE_SERVER_URL, null);
                if (serverUrl != null) {
                    serverUrlPreference.setSummary(serverUrl);
                } else {
                    serverUrlPreference.setSummary(R.string.none);
                }
            }
        }

    }

}
