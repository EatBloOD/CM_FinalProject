package pt.uc.cm.daylistudent.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import pt.uc.cm.daylistudent.R;
import pt.uc.cm.daylistudent.utils.SharedPreferencesUtils;

public class CreditsActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferencesUtils.INSTANCE.readPreferencesUser(getApplicationContext());
        setContentView(R.layout.creditos);

        ImageView imgUC = findViewById(R.id.ivUC);
        imgUC.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(getString(R.string.uc_url)));
            startActivity(intent);
        });

        ImageView imgGit = findViewById(R.id.ivGithub);
        imgGit.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(getString(R.string.github_url)));
            startActivity(intent);
        });
    }

}
