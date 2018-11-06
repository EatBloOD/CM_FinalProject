package pt.uc.cm.daily_student.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import pt.uc.cm.daily_student.R;

public class CreditsActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readPreferencesUser();
        setContentView(R.layout.creditos);

        ImageView imgLinkedIn = findViewById(R.id.ivLINKEDIN);
        imgLinkedIn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse("https://www.linkedin.com/in/jorge-silva-b5b194b0?trk=nav_responsive_tab_profile_pic"));
            startActivity(intent);
        });
        ImageView imgGit = findViewById(R.id.ivGITHUB);
        imgGit.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse("https://github.com/EatBloOD"));
            startActivity(intent);
        });
    }

    private void readPreferencesUser() {
        int textSize = -1;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(CreditsActivity.this);

        switch (sharedPreferences.getString("themeKey", "YellowTheme")) {
            case "RedTheme":
                setTheme(R.style.RedTheme);
                break;
            case "YellowTheme":
                setTheme(R.style.YellowTheme);
                break;
            case "GreenTheme":
                setTheme(R.style.GreenTheme);
                break;
        }

        System.out.println("TAMANHO ESCOLHIDO : " + sharedPreferences.getString("fontSizeKey", "darkab"));
        switch (sharedPreferences.getString("fontSizeKey", "normal")) {
            case "smallest":
                textSize = 12;
                break;
            case "small":
                textSize = 14;
                break;
            case "normal":
                textSize = 16;
                break;
            case "large":
                textSize = 18;
                break;
            case "largest":
                textSize = 20;
                break;
        }
    }
}
