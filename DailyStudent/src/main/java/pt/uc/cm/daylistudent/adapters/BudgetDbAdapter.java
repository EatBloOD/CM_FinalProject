package pt.uc.cm.daylistudent.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pt.uc.cm.daylistudent.R;
import pt.uc.cm.daylistudent.models.BudgetNote;

import static android.R.attr.id;

public class BudgetDbAdapter {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_TYPE = "type";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_WALLET = "wallet";
    public static final String KEY_OBS = "observations";
    public static final String KEY_PHOTO = "photo";
    public static final String KEY_VALUE = "value";
    public static final String KEY_DATE = "date";

    private final Context mCtx;

    private static final String TAG = "BudgetNotesDbAdapter";
    private DatabaseHelper mBudgetDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table budgetNotes (_id integer primary key autoincrement, "
                    + "title text, "
                    + "type text, "
                    + "image text, "
                    + "gender text, "
                    + "wallet text, "
                    + "observations text, "
                    + "photo text, "
                    + "value double,"
                    + "date text);";

    private static final String DATABASE_NAME = "budget";
    private static final String DATABASE_TABLE = "budgetNotes";
    private static final int DATABASE_VERSION = 2;

    public List<Double> fetchTodayExpences() throws ParseException {
        List<String> data = new ArrayList();
        List<String> gender = new ArrayList();
        List<Double> money = new ArrayList();
        List<Double> dados = new ArrayList();
        double despesas = 0.00, lucros = 0.00;

        SQLiteDatabase db = mBudgetDbHelper.getWritableDatabase();
        String selectQuery = "SELECT value,gender, date FROM budgetNotes";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String datai = cursor.getString(cursor.getColumnIndex("date"));
                String genderi = cursor.getString(cursor.getColumnIndex("gender"));
                Double dinheiroi = cursor.getDouble(cursor.getColumnIndex("value"));
                data.add(datai);
                gender.add(genderi);
                money.add(dinheiroi);
            } while (cursor.moveToNext());
        }

        for (int i = 0; i < data.size(); i++)
            System.out.println(data.get(i));

        //OBTEM O DIA DE HOJE
        Date date = new Date();
        String displayDate = new SimpleDateFormat("MMM dd, yyyy - h:mm a").format(new Date(date.getTime()));
        SimpleDateFormat sd = new SimpleDateFormat("MMM dd, yyyy - h:mm a");
        Date currentDate = sd.parse(displayDate);

        for (int i = 0; i < data.size(); i++) {
            String yourDateString = data.get(i);
            SimpleDateFormat sd2 = new SimpleDateFormat("MMM dd, yyyy - h:mm a");
            Date dataBD = sd2.parse(yourDateString);

            if (currentDate.getDate() == dataBD.getDate()
                    && currentDate.getMonth() == dataBD.getMonth()
                    && (dataBD.getYear() + 1900) == (currentDate.getYear() + 1900)
                    ) {
                if (gender.get(i).compareTo("Profits") == 0 || gender.get(i).compareTo("Lucro") == 0 || gender.get(i).compareTo("Beneficio") == 0 || gender.get(i).compareTo("Profit") == 0)
                    lucros += money.get(i);
                else
                    despesas -= money.get(i);
            }

        }
        dados.add(lucros);
        dados.add(despesas);

        return dados;
    }

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

    public BudgetDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public BudgetDbAdapter open() throws SQLException {
        mBudgetDbHelper = new BudgetDbAdapter.DatabaseHelper(mCtx);
        mDb = mBudgetDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mBudgetDbHelper.close();
    }

    public void createNote(String type, String title, String gender, String wallet, String obs, String photo, Double value) {
        Date date = new Date();
        String displayDate = new SimpleDateFormat("MMM dd, yyyy - h:mm a").format(new Date(date.getTime()));

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_IMAGE, imageType(type));
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_GENDER, gender);
        initialValues.put(KEY_WALLET, wallet);
        initialValues.put(KEY_OBS, obs);
        initialValues.put(KEY_PHOTO, photo);
        initialValues.put(KEY_VALUE, value);
        initialValues.put(KEY_DATE, displayDate);

        mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    public void deleteNote(long rowId) {
        mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null);
    }

    public Cursor fetchAllNotes() {
        return mDb.query(DATABASE_TABLE, new String[]{
                KEY_ROWID,
                KEY_TYPE,
                KEY_IMAGE,
                KEY_TITLE,
                KEY_GENDER,
                KEY_WALLET,
                KEY_OBS,
                KEY_PHOTO,
                KEY_VALUE,
                KEY_DATE
        }, null, null, null, null, null);
    }

    public String[] fetchAllNotesValue() {
        List<String> dados = new ArrayList();
        SQLiteDatabase db = mBudgetDbHelper.getWritableDatabase();
        ;
        String selectQuery = "SELECT value FROM budgetNotes";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String palavra = cursor.getString(cursor.getColumnIndex("value"));
                dados.add(palavra);
            } while (cursor.moveToNext());
        }
        return dados.toArray(new String[dados.size()]);
    }

    public ArrayList<BudgetNote> getNotesOfThisWallet(String carteira) {
        ArrayList<BudgetNote> dados = new ArrayList();
        SQLiteDatabase db = mBudgetDbHelper.getWritableDatabase();
        ;
        String selectQuery = "SELECT _id,title,gender,value,wallet FROM budgetNotes";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Integer id = cursor.getInt(cursor.getColumnIndex("_id"));
                String palavra = cursor.getString(cursor.getColumnIndex("title"));
                String wallet = cursor.getString(cursor.getColumnIndex("wallet"));
                String value = cursor.getString(cursor.getColumnIndex("value"));
                String gender = cursor.getString(cursor.getColumnIndex("gender"));
                System.out.println("CARTEIRA " + wallet + carteira.toString());

                if (wallet.equals(carteira)) {
                    if (gender.equals("Despesa"))
                        dados.add(new BudgetNote(id, palavra, -Double.valueOf(value)));
                    else
                        dados.add(new BudgetNote(id, palavra, Double.valueOf(value)));
                }

            } while (cursor.moveToNext());
        }
        //aqui dados terá todos os valores do banco
        //converte o list para array
        for (int i = 0; i < dados.size(); i++)
            System.out.println(" Nome: " + dados.get(i).getTitle() + "    Valor: " + dados.get(i).getValue());
        return dados;
    }

    public String[] fetchNotesByParameter(String parameter) {
        List<String> dados = new ArrayList();
        SQLiteDatabase db = mBudgetDbHelper.getWritableDatabase();
        ;
        String selectQuery = "SELECT " + parameter + " FROM budgetNotes";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String palavra = cursor.getString(cursor.getColumnIndex(parameter));
                dados.add(palavra);
            } while (cursor.moveToNext());
        }
        return dados.toArray(new String[dados.size()]);
    }

    public List<BudgetNote> fetchNote(String wallet) {
        List<BudgetNote> dados = new ArrayList();
        SQLiteDatabase db = mBudgetDbHelper.getWritableDatabase();
        String selectQuery = "SELECT wallet, _id,title, value FROM budgetNotes";
        Cursor cursor = db.rawQuery(selectQuery, null);
        System.out.println("BASE DE DADOS DAS NOTAS");
        if (cursor.moveToFirst()) {
            do {

                String palavra = cursor.getString(cursor.getColumnIndex("wallet"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                String valor = cursor.getString(cursor.getColumnIndex("value"));
                Integer id = cursor.getInt(cursor.getColumnIndex("_id"));
                System.out.println(palavra);
                System.out.println(id);
                if (palavra.equals(wallet))
                    dados.add(new BudgetNote(id, title, Double.valueOf(valor)));
            } while (cursor.moveToNext());
        }
        //aqui dados terá todos os valores do banco
        //converte o list para array
        return dados;
    }

    public List<BudgetNote> fetchNoteForStatistics() {
        List<BudgetNote> dados = new ArrayList();
        SQLiteDatabase db = mBudgetDbHelper.getWritableDatabase();
        String selectQuery = "SELECT * FROM budgetNotes";
        Cursor cursor = db.rawQuery(selectQuery, null);
        System.out.println("fetchNoteForStatistics");
        if (cursor.moveToFirst()) {
            do {

                String title = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
                String gender = cursor.getString(cursor.getColumnIndex(KEY_GENDER));
                String value = cursor.getString(cursor.getColumnIndex(KEY_VALUE));
                String date = cursor.getString(cursor.getColumnIndex(KEY_DATE));
                dados.add(new BudgetNote(id, title, gender, Double.valueOf(value), date));
            } while (cursor.moveToNext());
        }
        return dados;
    }

    public Integer[] fetchAllNotesGenderImage() {
        List<Integer> data = new ArrayList();
        SQLiteDatabase db = mBudgetDbHelper.getWritableDatabase();
        String selectQuery = "SELECT gender FROM budgetNotes";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String word = cursor.getString(cursor.getColumnIndex("gender"));
                if (word.equals(mCtx.getString(R.string.bar)))
                    data.add(R.drawable.bar);
                if (word.equals(mCtx.getString(R.string.cantina)))
                    data.add(R.drawable.cantina);
                if (word.equals(mCtx.getString(R.string.material)))
                    data.add(R.drawable.material);
                if (word.equals(mCtx.getString(R.string.compras)))
                    data.add(R.drawable.compras);
                if (word.equals(mCtx.getString(R.string.jantares)))
                    data.add(R.drawable.jantares);
                if (word.equals(mCtx.getString(R.string.viagens)))
                    data.add(R.drawable.viagens);
                if (word.equals(mCtx.getString(R.string.propinas)))
                    data.add(R.drawable.propinas);
                if (word.equals(mCtx.getString(R.string.apostas)))
                    data.add(R.drawable.apostas);
                if (word.equals(mCtx.getString(R.string.bar)))
                    data.add(R.drawable.trabalho);

            } while (cursor.moveToNext());
        }

        return data.toArray(new Integer[data.size()]);
    }

    public Cursor fetchNote(long rowId) throws SQLException {
        Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[]{
                        KEY_ROWID,
                        KEY_TYPE,
                        KEY_IMAGE,
                        KEY_TITLE,
                        KEY_GENDER,
                        KEY_WALLET,
                        KEY_OBS,
                        KEY_PHOTO,
                        KEY_VALUE,
                        KEY_DATE
                },
                KEY_ROWID + "=" + rowId, null,
                null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    public void updateNote(long rowId, String type, String title, String gender, String wallet, String obs, String photo, Double value) {
        ContentValues args = new ContentValues();
        Log.i(TAG, type + " " + title + " " + gender + " " + obs + " " + value + " " + wallet);
        Date date = new Date();
        String displayDate = new SimpleDateFormat("MMM dd, yyyy - h:mm a").format(new Date(date.getTime()));

        args.put(KEY_TITLE, title);
        args.put(KEY_TYPE, type);
        args.put(KEY_IMAGE, imageType(type));
        args.put(KEY_GENDER, gender);
        args.put(KEY_WALLET, wallet);
        args.put(KEY_OBS, obs);
        args.put(KEY_PHOTO, photo);
        args.put(KEY_VALUE, value);
        args.put(KEY_DATE, displayDate);

        mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null);
    }

    private int imageType(String type) {
        if (type.equals(mCtx.getString(R.string.bar)))
            return R.drawable.bar;
        if (type.equals(mCtx.getString(R.string.cantina)))
            return R.drawable.cantina;
        if (type.equals(mCtx.getString(R.string.material)))
            return R.drawable.material;
        if (type.equals(mCtx.getString(R.string.compras)))
            return R.drawable.compras;
        if (type.equals(mCtx.getString(R.string.jantares)))
            return R.drawable.jantares;
        if (type.equals(mCtx.getString(R.string.viagens)))
            return R.drawable.viagens;
        if (type.equals(mCtx.getString(R.string.propinas)))
            return R.drawable.propinas;
        if (type.equals(mCtx.getString(R.string.apostas)))
            return R.drawable.apostas;
        if (type.equals(mCtx.getString(R.string.trabalho)))
            return R.drawable.trabalho;
        if (type.equals(mCtx.getString(R.string.alojamento)))
            return R.drawable.house;
        if (type.equals(mCtx.getString(R.string.animais)))
            return R.drawable.pawprint;
        if (type.equals(mCtx.getString(R.string.roupa)))
            return R.drawable.tie;
        if (type.equals(mCtx.getString(R.string.comida)))
            return R.drawable.groceries;
        if (type.equals(mCtx.getString(R.string.saude)))
            return R.drawable.hospital;
        if (type.equals(mCtx.getString(R.string.other)))
            return R.drawable.lodging;

        return R.drawable.despesa;
    }
}
