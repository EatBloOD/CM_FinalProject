package com.example.jorge.tpamov.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.example.jorge.tpamov.DataBase.BudgetDbAdapter;
import com.example.jorge.tpamov.DataBase.NotesDbAdapter;
import com.example.jorge.tpamov.R;
import com.example.jorge.tpamov.Resources.NoteEdit;

/**
 * Created by Jorge on 19/12/2016.
 */
public class DayliStudent_Activity extends AppCompatActivity {
    //NOTAS TEXTUAIS
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int DELETE_ID = Menu.FIRST + 1;


    private NotesDbAdapter mDbHelper;
    private BudgetDbAdapter mDbBudgetHelper;

    private Cursor mNotesCursor;

    SharedPreferences sharedPreferences;

    static ListView lv_notes;

    private boolean readPreferencesUser(){
        int textSize = -1;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(DayliStudent_Activity.this);

        switch(sharedPreferences.getString("themeKey", "YellowTheme")) {
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
        switch(sharedPreferences.getString("fontSizeKey", "normal")) {
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

        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readPreferencesUser();
        setContentView(R.layout.notes_list);
        setTitle(getString(R.string.dayliStudentActivityTitle));

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        mDbBudgetHelper = new BudgetDbAdapter(this);
        mDbBudgetHelper.open();

        fillData();

        lv_notes = (ListView) findViewById(R.id.lv_notes);
        registerForContextMenu(lv_notes);

        lv_notes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor c = mNotesCursor;
                c.moveToPosition(position);
                Intent i = new Intent(getApplicationContext(),NoteEdit.class);
                i.putExtra(NotesDbAdapter.KEY_ROWID,position);
                i.putExtra(NotesDbAdapter.KEY_TITLE, c.getString(c.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
                i.putExtra(NotesDbAdapter.KEY_BODY, c.getString(c.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
                startActivityForResult(i,ACTIVITY_EDIT);
            }
        });
    }

    private void fillData() {
        mNotesCursor = mDbHelper.fetchAllNotes();
        startManagingCursor(mNotesCursor);

        String[] from = new String[]{NotesDbAdapter.KEY_TITLE, NotesDbAdapter.KEY_BODY, NotesDbAdapter.KEY_DATE};

        int[] to = new int[]{R.id.tvTitulo, R.id.tvBody, R.id.tvDate};

        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.notes_row, mNotesCursor, from, to);
        lv_notes = (ListView) findViewById(R.id.lv_notes);
        lv_notes.setAdapter(notes);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sup_notes, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(0,DELETE_ID,0, R.string.DayliStudentActivityDeleteNote);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteNote(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    public void createBudgetAction(MenuItem item){
        Intent i= new Intent(this,NoteEdit.class);
        startActivityForResult(i,ACTIVITY_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(resultCode == RESULT_OK) {

            Bundle extras = intent.getExtras();

            switch (requestCode) {
                case ACTIVITY_CREATE:
                    String title = extras.getString(NotesDbAdapter.KEY_TITLE);
                    String body = extras.getString(NotesDbAdapter.KEY_BODY);
                    mDbHelper.createNote(title, body);
                    fillData();
                    break;
                case ACTIVITY_EDIT:
                    Integer mRowId = extras.getInt(NotesDbAdapter.KEY_ROWID);
                    if (mRowId != null) {
                        String editTitle = extras.getString(NotesDbAdapter.KEY_TITLE);
                        String editBody = extras.getString(NotesDbAdapter.KEY_BODY);
                        mDbHelper.updateNote(mRowId+1, editTitle, editBody);
                    }
                    fillData();
                    break;
                default:
            }
        }
    }
}
