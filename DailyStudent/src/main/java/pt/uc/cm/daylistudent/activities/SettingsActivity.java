package pt.uc.cm.daylistudent.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import pt.uc.cm.daylistudent.R;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        bindPreferenceSummaryToValue(findPreference("serverKey"));
        bindPreferenceSummaryToValue(findPreference("nameKey"));
        bindPreferenceSummaryToValue(findPreference("emailKey"));
        bindPreferenceSummaryToValue(findPreference("themeKey"));
        bindPreferenceSummaryToValue(findPreference("fontSizeKey"));

    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference("serverKey"));
        bindPreferenceSummaryToValue(findPreference("nameKey"));
        bindPreferenceSummaryToValue(findPreference("emailKey"));
        bindPreferenceSummaryToValue(findPreference("themeKey"));
        bindPreferenceSummaryToValue(findPreference("fontSizeKey"));

    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) -> {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference
                    .setSummary(index >= 0 ? listPreference.getEntries()[index]
                            : null);
        } else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(stringValue);
        }

        return true;
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference
                .setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(
                        preference.getContext()).getString(preference.getKey(),
                        ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        return false;
    }


    @Override
    protected void onPostResume() {
        System.out.println("ONPOSTRESUME");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userTheme = preferences.getString("themeKey", "darkab");
        if (userTheme.equals("RedTheme"))
            setTheme(R.style.RedTheme);
        else if (userTheme.equals("YellowTheme"))
            setTheme(R.style.YellowTheme);
        else if (userTheme.equals("GreenTheme"))
            setTheme(R.style.GreenTheme);
        else
            setTheme(R.style.AppTheme);
        super.onPostResume();
    }
}
