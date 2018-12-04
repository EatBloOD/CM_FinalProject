package pt.uc.cm.daylistudent.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class GlobalNotesDbAdapter {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_DATE = "date";

    private static final String TAG = "GlobalNotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table GlobalNotes (_id integer primary key autoincrement, "
                    + "author text not null, "
                    + "title text not null, "
                    + "body text not null,"
                    + "date text not null);";

    private static final String DATABASE_NAME = "GlobalData";
    private static final String DATABASE_TABLE = "GlobalNotes";
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

    public GlobalNotesDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public GlobalNotesDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public List<String[]> getLatestGlobalNotes() {
        List<String[]> data = new ArrayList();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String selectQuery = "SELECT  * FROM " + DATABASE_TABLE;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String author = cursor.getString(1);
                String title = cursor.getString(2);
                String array[] = {author, title};
                Log.i(TAG, "author: " + author + ", title:" + title);
                data.add(array);
            } while (cursor.moveToNext());
        }
        cursor.close();

        String[] note = {"N/A", "N/A"};
        for (int i = data.size(); i < 3; i++)
            data.add(note);
        return data;
    }
}
