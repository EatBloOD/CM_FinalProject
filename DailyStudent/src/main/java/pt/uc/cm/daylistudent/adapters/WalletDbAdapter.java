package pt.uc.cm.daylistudent.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WalletDbAdapter {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_TYPE = "type";
    public static final String KEY_VALUE = "value";

    private final Context mCtx;

    private static final String TAG = "WalletDbAdapter";
    private DatabaseHelper mWalletDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table walletPocket (_id integer primary key autoincrement, "
                    + "title text, "
                    + "type text, "
                    + "value double);";

    private static final String DATABASE_NAME = "wallet";
    private static final String DATABASE_TABLE = "walletPocket";
    private static final int DATABASE_VERSION = 2;

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

    public WalletDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public WalletDbAdapter open() throws SQLException {
        mWalletDbHelper = new DatabaseHelper(mCtx);
        mDb = mWalletDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mWalletDbHelper.close();
    }

    public void createWallet(String type, String title, Double value) {
        Log.i("WalletDbAdapter", "CreateWallet");
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_VALUE, value);

        mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    public void deleteWallet(long rowId) {
        mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null);
    }

    public Cursor fetchAllWallets() {
        return mDb.query(DATABASE_TABLE, new String[]{
                KEY_ROWID,
                KEY_TYPE,
                KEY_TITLE,
                KEY_VALUE
        }, null, null, null, null, null);
    }

    public List<String> fetchAllWalletsByParameter(String parameter) {
        List<String> data = new ArrayList();
        SQLiteDatabase db = mWalletDbHelper.getWritableDatabase();
        String selectQuery = "SELECT " + parameter + " FROM walletPocket";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String palavra = cursor.getString(cursor.getColumnIndex(parameter));
                data.add(palavra);
            } while (cursor.moveToNext());
        }
        cursor.close();
        //aqui data terá todos os valores do banco
        //converte o list para array
        return data;
    }

    public List<String[]> getLineTable() {
        List<String[]> dados = new ArrayList();
        SQLiteDatabase db = mWalletDbHelper.getWritableDatabase();
        String selectQuery = "SELECT  * FROM " + DATABASE_TABLE;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(0);
                String nome = cursor.getString(1);
                String tipo = cursor.getString(2);
                Double value = cursor.getDouble(3);
                String array[] = {id.toString(), nome, tipo, value.toString()};
                dados.add(array);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dados;
    }

    public List<String[]> getWalletsMoney() {
        List<String[]> data = new ArrayList();
        SQLiteDatabase db = mWalletDbHelper.getWritableDatabase();
        String selectQuery = "SELECT  * FROM " + DATABASE_TABLE;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(0);
                String nome = cursor.getString(1);
                String tipo = cursor.getString(2);
                Double value = cursor.getDouble(3);
                String array[] = {id.toString(), nome, tipo, value.toString()};
                data.add(array);
            } while (cursor.moveToNext());
        }
        cursor.close();

        String[] walletMoreMoney = {"N/A", "N/A", "N/A", "0.00"};
        String[] walletMoreMoney2 = {"N/A", "N/A", "N/A", "0.00"};
        for (int i = 0; i < data.size(); i++) {
            if (Double.parseDouble(walletMoreMoney[3]) < Double.parseDouble(data.get(i)[3]))
                walletMoreMoney = data.get(i);
            else if (Double.parseDouble(walletMoreMoney2[3]) < Double.parseDouble(data.get(i)[3]))
                walletMoreMoney2 = data.get(i);
        }
        data.clear();
        data.add(walletMoreMoney);
        data.add(walletMoreMoney2);
        return data;
    }

    public String[] getRowNome(String walletName) {
        SQLiteDatabase db = mWalletDbHelper.getWritableDatabase();
        String selectQuery = "SELECT  * FROM " + DATABASE_TABLE;

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Log.i(TAG, cursor.getString(1));
                if (walletName.compareTo(cursor.getString(1)) == 0) {
                    Integer id = cursor.getInt(0);
                    String nome = cursor.getString(1);
                    String tipo = cursor.getString(2);
                    Double value = cursor.getDouble(3);
                    String array[] = {id.toString(), nome, tipo, value.toString()};
                    cursor.close();
                    Log.i(TAG, array[0] + " " + array[1]);
                    return array;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return null;
    }

    public Cursor fetchWallet(long rowId) throws SQLException {
        Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[]{
                        KEY_ROWID,
                        KEY_TYPE,
                        KEY_TITLE,
                        KEY_VALUE
                },
                KEY_ROWID + "=" + rowId, null,
                null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public void updateWallet(long rowId, String type, String title, Double value) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_TYPE, type);
        args.put(KEY_VALUE, value);

        mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null);
    }
}
