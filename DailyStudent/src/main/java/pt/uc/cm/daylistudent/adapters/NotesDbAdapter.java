package pt.uc.cm.daylistudent.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotesDbAdapter {

    private static final String TAG = NotesDbAdapter.class.getSimpleName();

    public static final String KEY_ROWID = "_id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_DATE = "date";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table notes (_id integer primary key autoincrement, "
                    + "title text not null, "
                    + "body text not null,"
                    + "date text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "notes";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }


    public NotesDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public NotesDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public void createNote(String title, String body) {
        Date date = new Date();
        String displayDate = new SimpleDateFormat("MMM dd, yyyy - h:mm a").format(new Date(date.getTime()));

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_BODY, body);
        initialValues.put(KEY_DATE, displayDate);

        mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    public void deleteNote(long rowId) {
        mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null);
    }

    public Cursor fetchAllNotes() {
        return mDb.query(DATABASE_TABLE, new String[]{KEY_ROWID, KEY_TITLE,
                KEY_BODY, KEY_DATE}, null, null, null, null, null);
    }

    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[]{KEY_ROWID,
                                KEY_TITLE, KEY_BODY, KEY_DATE}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null)
            mCursor.moveToFirst();
        return mCursor;
    }

    public List<String[]> getLatestNotes() {
        List<String[]> data = new ArrayList();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String selectQuery = "SELECT  * FROM " + DATABASE_TABLE;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String titulo = cursor.getString(1);
                String obs = cursor.getString(2);
                String array[] = {titulo, obs};
                data.add(array);
            } while (cursor.moveToNext());
        }
        cursor.close();

        String[] note = {"N/A", "N/A"};
        for (int i = data.size(); i < 3; i++) {
            data.add(note);
        }
        return data;
    }

    public void updateNote(long rowId, String title, String body) {
        ContentValues args = new ContentValues();
        Date date = new Date();
        String displayDate = new SimpleDateFormat("MMM dd, yyyy - h:mm a").format(new Date(date.getTime()));
        args.put(KEY_TITLE, title);
        args.put(KEY_BODY, body);
        args.put(KEY_DATE, displayDate);

        mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null);
    }
}
