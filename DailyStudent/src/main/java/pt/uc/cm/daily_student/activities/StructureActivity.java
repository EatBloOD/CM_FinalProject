package pt.uc.cm.daily_student.activities;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import pt.uc.cm.daily_student.R;

public class StructureActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    public static final String NOME = "nameKey";
    public static final String EMAIL = "emailKey";
    public static final String PASSWORD = "passwordKey";

    TextView tvUserName, tvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readPreferencesUser();
    }


    public void readInfoUser() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //COLOCA A INFORMAÇÃO NA SIDEBAR
        //tvUserName.setText(sharedPreferences.getString(NOME, "DEFAULT"));
        //tvEmail.setText(sharedPreferences.getString(EMAIL, "DEFAULT"));
    }

    public void readPreferencesUser() {
        int textSize = -1;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        System.out.println("TEMA ESCOLHIDO : " + sharedPreferences.getString("themeKey", "THEMERED"));
        switch (sharedPreferences.getString("themeKey", "THEMEYELLOW")) {
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

        //TODO: RETIRAR CASO NÃO SEJA APLICADO
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
