package pt.uc.cm.daylistudent.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import pt.uc.cm.daylistudent.R;
import pt.uc.cm.daylistudent.adapters.NotesDbAdapter;
import pt.uc.cm.daylistudent.utils.SharedPreferencesUtils;
import pt.uc.cm.daylistudent.utils.SnackBarUtil;

public class ManageNoteActivity extends AppCompatActivity {

    private final String TAG = ManageNoteActivity.class.getSimpleName();

    EditText mTitleText, mBodyText;
    Integer mRowId;
    NotesDbAdapter mDbHelper;

    @Override
    public void onBackPressed() {
        Intent mIntent = new Intent();
        setResult(RESULT_CANCELED, mIntent);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(SharedPreferencesUtils.INSTANCE.readTheme(getApplicationContext()));
        setContentView(R.layout.note_edit);
        setTitle(getString(R.string.noteEditTitle));

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        mTitleText = findViewById(R.id.etTitle);
        mBodyText = findViewById(R.id.etBody);

        mRowId = null;

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String title = extras.getString(NotesDbAdapter.KEY_TITLE);
            String body = extras.getString(NotesDbAdapter.KEY_BODY);
            mRowId = extras.getInt(NotesDbAdapter.KEY_ROWID);

            if (title != null)
                mTitleText.setText(title);
            if (body != null)
                mBodyText.setText(body);
        }

        mDbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sup_editnote, menu);
        return true;
    }

    public void createEmailAction(MenuItem item) {
        Intent email = new Intent(Intent.ACTION_SEND);
        email.setType("message/rfc822");


        email.putExtra(Intent.EXTRA_SUBJECT, mTitleText.getText().toString());
        email.putExtra(Intent.EXTRA_TEXT, mBodyText.getText().toString());

        try {
            startActivity(Intent.createChooser(email, getString(R.string.noteEditSelectEmailApp)));
        } catch (ActivityNotFoundException e) {
            SnackBarUtil.showSnackBar(getWindow().getDecorView().getRootView(), R.string.noteEditNoEmailApp, true);
        }
    }

    public void saveAction(MenuItem item) {
        Bundle bundle = new Bundle();
        if (mTitleText.getText().length() > 0) {
            bundle.putString(NotesDbAdapter.KEY_TITLE, mTitleText.getText().toString());
            bundle.putString(NotesDbAdapter.KEY_BODY, mBodyText.getText().toString());

            if (mRowId != null) //ou seja é uma edição?
                bundle.putInt(NotesDbAdapter.KEY_ROWID, mRowId);

            Intent mIntent = new Intent();
            mIntent.putExtras(bundle);
            setResult(RESULT_OK, mIntent);
            finish();
        } else {
            SnackBarUtil.showSnackBar(getWindow().getDecorView().getRootView(), R.string.noteEditEmptyTitle, true);
        }
    }
}
