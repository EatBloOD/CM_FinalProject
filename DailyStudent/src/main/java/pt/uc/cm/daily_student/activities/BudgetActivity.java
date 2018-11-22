package pt.uc.cm.daily_student.activities;

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
import android.widget.TextView;

import java.util.List;

import pt.uc.cm.daily_student.R;
import pt.uc.cm.daily_student.fragments.NoteBudget;
import pt.uc.cm.daily_student.fragments.Wallet;
import pt.uc.cm.daily_student.adapters.BudgetDbAdapter;
import pt.uc.cm.daily_student.adapters.WalletDbAdapter;
import pt.uc.cm.daily_student.models.BudgetNote;

public class BudgetActivity extends StructureActivity {
    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;
    private static final int ACTIVITY_WALLET_CREATE = 2;
    private static final int ACTIVITY_UPDATE_WALLET = 3;

    private TextView tvTOTAL;
    ListView lv_pocket;
    ListView lv_bgnotes;

    private BudgetDbAdapter mDbBudgetHelper;
    private WalletDbAdapter mDbWalletHelper;

    private Cursor mBudgetCursor;
    private Cursor mWalletCursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readPreferencesUser();
        setContentView(R.layout.notes_budget_list);
        setTitle(getString(R.string.budgetActivityTitle));

        lv_pocket = findViewById(R.id.list_pocket);
        lv_bgnotes = findViewById(R.id.list_budgetNotes);
        tvTOTAL = findViewById(R.id.tvTotal);

        mDbBudgetHelper = new BudgetDbAdapter(this);
        mDbBudgetHelper.open();

        mDbWalletHelper = new WalletDbAdapter(this);
        mDbWalletHelper.open();

        fillData();
        registerForContextMenu(lv_pocket);
        registerForContextMenu(lv_bgnotes);
    }

    private void fillData() {
        mBudgetCursor = mDbBudgetHelper.fetchAllNotes();
        mWalletCursor = mDbWalletHelper.fetchAllWallets();

        startManagingCursor(mBudgetCursor);
        startManagingCursor(mWalletCursor);

        //1 - Cria um array para especificar os campos que queremos mostrar a partir da BD
        String[] from = new String[]{BudgetDbAdapter.KEY_TITLE, BudgetDbAdapter.KEY_OBS,
                BudgetDbAdapter.KEY_VALUE, BudgetDbAdapter.KEY_IMAGE};
        //2 - Inicializar o array onde os queremos colocar
        int[] to = new int[]{R.id.tvTitulo, R.id.tvBody, R.id.tvDate, R.id.imgGender};

        //3 - Criamos um adapter e mostramos o conteúdo no sítio certo
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.notesbudget_row, mBudgetCursor, from, to);
        lv_bgnotes.setAdapter(notes);

        String[] from2 = new String[]{WalletDbAdapter.KEY_TITLE, WalletDbAdapter.KEY_TYPE,
                WalletDbAdapter.KEY_VALUE};

        int[] to2 = new int[]{R.id.tvTitulo, R.id.tvBody, R.id.tvDate};

        SimpleCursorAdapter wallets =
                new SimpleCursorAdapter(this, R.layout.notesbudget_row, mWalletCursor, from2, to2);
        lv_pocket.setAdapter(wallets);

        lv_pocket.setOnItemClickListener((adapterView, view, position, id) -> {
            updateWallet(position, id);
            fillData();
        });

        lv_bgnotes.setOnItemClickListener((adapterView, view, position, id) -> {
            Cursor c = mBudgetCursor;
            c.moveToPosition(position);
            Intent i = new Intent(getApplicationContext(), NoteBudget.class);
            i.putExtra(BudgetDbAdapter.KEY_ROWID, id);
            i.putExtra(BudgetDbAdapter.KEY_TITLE, c.getString(c.getColumnIndexOrThrow(BudgetDbAdapter.KEY_TITLE)));
            i.putExtra(BudgetDbAdapter.KEY_GENDER, c.getString(c.getColumnIndexOrThrow(BudgetDbAdapter.KEY_GENDER)));
            i.putExtra(BudgetDbAdapter.KEY_WALLET, c.getString(c.getColumnIndexOrThrow(BudgetDbAdapter.KEY_WALLET)));
            i.putExtra(BudgetDbAdapter.KEY_TYPE, c.getString(c.getColumnIndexOrThrow(BudgetDbAdapter.KEY_TYPE)));
            i.putExtra(BudgetDbAdapter.KEY_VALUE, c.getString(c.getColumnIndexOrThrow(BudgetDbAdapter.KEY_VALUE)));
            i.putExtra(BudgetDbAdapter.KEY_OBS, c.getString(c.getColumnIndexOrThrow(BudgetDbAdapter.KEY_OBS)));
            i.putExtra(BudgetDbAdapter.KEY_PHOTO, c.getString(c.getColumnIndexOrThrow(BudgetDbAdapter.KEY_PHOTO)));
            startActivityForResult(i, ACTIVITY_EDIT);
        });
        //4- Actualizar o total em cima mostrado
        checkMoney(mDbBudgetHelper.fetchNotesByParameter(BudgetDbAdapter.KEY_VALUE), mDbBudgetHelper.fetchNotesByParameter(BudgetDbAdapter.KEY_GENDER));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sup_budget, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void createBudgetAction(MenuItem item) {
        Intent i = new Intent(this, NoteBudget.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    public void createWalletAction(MenuItem item) {
        Intent i = new Intent(this, Wallet.class);
        startActivityForResult(i, ACTIVITY_WALLET_CREATE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();

        switch (v.getId()) {
            case R.id.list_pocket:
                inflater.inflate(R.menu.delete_pocket, menu);
                break;
            case R.id.list_budgetNotes:
                inflater.inflate(R.menu.delete_note, menu);
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_note:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mDbBudgetHelper.deleteNote(info.id);
                fillData();
                return true;
            case R.id.delete_pocket:
                AdapterView.AdapterContextMenuInfo info_carteira = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Cursor c = mDbWalletHelper.fetchWallet(info_carteira.id);
                List<BudgetNote> dados = mDbBudgetHelper.fetchNote(c.getString(2));

                System.out.println(dados.size());

                for (int i = 0; i < dados.size(); i++)
                    mDbBudgetHelper.deleteNote(dados.get(i).getId());

                mDbWalletHelper.deleteWallet(info_carteira.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    public void checkMoney(String values[], String genders[]) {
        Double total = 0.00;
        for (int i = 0; i < values.length; i++) {
            if (genders[i].compareTo(getString(R.string.despesa)) == 0)
                total = total - Double.parseDouble(values[i]);
            else
                total = total + Double.parseDouble(values[i]);
        }
        tvTOTAL.setText(getString(R.string.budgetActivityTotal) + total + getString(R.string.budgetActivityMoney));

        if (total < 0)
            tvTOTAL.setBackgroundColor(0xffff0000);
        else
            tvTOTAL.setBackgroundColor(0xff00ff22);
    }

    private void updateWallet(int position, long id) {
        Intent i = new Intent(this, Wallet.class);
        Cursor c = mWalletCursor;
        c.moveToPosition(position);
        i.putExtra(WalletDbAdapter.KEY_ROWID, id);
        i.putExtra(WalletDbAdapter.KEY_TITLE, c.getString(c.getColumnIndexOrThrow(WalletDbAdapter.KEY_TITLE)));
        i.putExtra(WalletDbAdapter.KEY_TYPE, c.getString(c.getColumnIndexOrThrow(WalletDbAdapter.KEY_TYPE)));
        i.putExtra(WalletDbAdapter.KEY_VALUE, c.getString(c.getColumnIndexOrThrow(WalletDbAdapter.KEY_VALUE)));
        startActivityForResult(i, ACTIVITY_UPDATE_WALLET);
    }

    private void updateWallets(String nome, String value, String gender) {
        Double aux = 0.0;

        String[] data = mDbWalletHelper.getRowNome(nome.trim());

        if (gender.compareTo(getString(R.string.despesa)) == 0)
            aux = (-Double.parseDouble(value));
        else
            aux = Double.parseDouble(value);
        Double vaux = Double.parseDouble(data[3]) + aux;

        mDbWalletHelper.updateWallet(Long.decode(data[0]), data[2], data[1], vaux);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            Bundle extras = intent.getExtras();

            switch (requestCode) {
                case ACTIVITY_CREATE:
                    String title = extras.getString(BudgetDbAdapter.KEY_TITLE);
                    String gender = extras.getString(BudgetDbAdapter.KEY_GENDER);
                    String wallet = extras.getString(BudgetDbAdapter.KEY_WALLET);
                    String type = extras.getString(BudgetDbAdapter.KEY_TYPE);
                    String value = extras.getString(BudgetDbAdapter.KEY_VALUE);
                    String obs = extras.getString(BudgetDbAdapter.KEY_OBS);
                    String imageURL = extras.getString(BudgetDbAdapter.KEY_PHOTO);

                    updateWallets(wallet, value, gender);

                    mDbBudgetHelper.createNote(type, title, gender, wallet, obs, imageURL, Double.parseDouble(value));

                    fillData();
                    break;
                case ACTIVITY_WALLET_CREATE:
                    String titleWallet = extras.getString(WalletDbAdapter.KEY_TITLE);
                    String typeWallet = extras.getString(WalletDbAdapter.KEY_TYPE);
                    String valueWallet = extras.getString(WalletDbAdapter.KEY_VALUE);

                    mDbWalletHelper.createWallet(typeWallet, titleWallet, Double.parseDouble(valueWallet));
                    fillData();
                    break;
                case ACTIVITY_EDIT:
                    Long mRowId = extras.getLong(BudgetDbAdapter.KEY_ROWID);
                    if (mRowId != null) {
                        String titleU = extras.getString(BudgetDbAdapter.KEY_TITLE);
                        String genderU = extras.getString(BudgetDbAdapter.KEY_GENDER);
                        String walletU = extras.getString(BudgetDbAdapter.KEY_WALLET);
                        String typeU = extras.getString(BudgetDbAdapter.KEY_TYPE);
                        String valueU = extras.getString(BudgetDbAdapter.KEY_VALUE);
                        String obsU = extras.getString(BudgetDbAdapter.KEY_OBS);
                        String imageURLU = extras.getString(BudgetDbAdapter.KEY_PHOTO);

                        updateWallets(walletU, valueU, genderU);

                        mDbBudgetHelper.updateNote(mRowId, typeU, titleU, genderU, walletU, obsU, imageURLU, Double.valueOf(valueU));
                    }
                    fillData();
                    break;
                case ACTIVITY_UPDATE_WALLET:
                    Long mWalletRowId = extras.getLong(WalletDbAdapter.KEY_ROWID);
                    if (mWalletRowId != null) {
                        String titleU = extras.getString(WalletDbAdapter.KEY_TITLE);
                        String typeU = extras.getString(WalletDbAdapter.KEY_TYPE);
                        String valueU = extras.getString(WalletDbAdapter.KEY_VALUE);

                        mDbWalletHelper.updateWallet(mWalletRowId, typeU, titleU, Double.valueOf(valueU));
                    }

                    fillData();
                    break;
                default:
            }
        }
    }
}
