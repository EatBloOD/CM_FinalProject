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
import android.widget.TextView;

import com.example.jorge.tpamov.DataBase.BudgetDbAdapter;
import com.example.jorge.tpamov.DataBase.WalletDbAdapter;
import com.example.jorge.tpamov.Classes.classBudgetNote;
import com.example.jorge.tpamov.R;
import com.example.jorge.tpamov.Resources.NoteBudget;
import com.example.jorge.tpamov.Resources.Wallet;

import java.util.List;

/**
 * Created by Jorge on 19/12/2016.
 */
public class Budget_Activity extends AppCompatActivity{
    //NOTAS BUDGET
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    private static final int ACTIVITY_WALLET_CREATE=2;
    private static final int ACTIVITY_UPDATE_WALLET=3;

    private TextView tvTOTAL;
    ListView lv_pocket;
    ListView lv_bgnotes;

    private BudgetDbAdapter mDbBudgetHelper;
    private WalletDbAdapter mDbWalletHelper;

    private Cursor mBudgetCursor;
    private Cursor mWalletCursor;

    SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readPreferencesUser();
        setContentView(R.layout.notes_budget_list);
        setTitle(getString(R.string.budgetActivityTitle));

        lv_pocket = (ListView)findViewById(R.id.list_pocket);
        lv_bgnotes = (ListView)findViewById(R.id.list_budgetNotes);
        tvTOTAL = (TextView) findViewById(R.id.tvTotal);

        //abrir a base de dados das notas budget
        mDbBudgetHelper = new BudgetDbAdapter(this);
        mDbBudgetHelper.open();

        //abrir a base de dados das notas budget
        mDbWalletHelper = new WalletDbAdapter(this);
        mDbWalletHelper.open();

        //preenche o layout
        fillData();
        registerForContextMenu(lv_pocket);
        registerForContextMenu(lv_bgnotes);
    }

    private boolean readPreferencesUser(){
            int textSize = -1;
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Budget_Activity.this);

            System.out.println("TEMA ESCOLHIDO : " + sharedPreferences.getString("themeKey", "THEMERED"));
            switch(sharedPreferences.getString("themeKey", "THEMEYELLOW")) {
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

    private void fillData() {
        mBudgetCursor = mDbBudgetHelper.fetchAllNotes();
        mWalletCursor = mDbWalletHelper.fetchAllWallets();

        startManagingCursor(mBudgetCursor);
        startManagingCursor(mWalletCursor);

        //Cria um array para especificar os campos que queremos mostrar a partir da BD
        String[] from = new String[]{BudgetDbAdapter.KEY_TITLE, BudgetDbAdapter.KEY_OBS,
                                    BudgetDbAdapter.KEY_VALUE, BudgetDbAdapter.KEY_IMAGE};
        // o array onde os queremos colocar
        int[] to = new int[]{R.id.tvTitulo, R.id.tvBody, R.id.tvDate, R.id.imgGender};

        // criamos um adapter e mostramos o conteudo no sitio certo
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.notesbudget_row, mBudgetCursor, from, to);
        lv_bgnotes.setAdapter(notes);

        String[] from2 = new String[]{WalletDbAdapter.KEY_TITLE, WalletDbAdapter.KEY_TYPE,
                                      WalletDbAdapter.KEY_VALUE};

        int[] to2 = new int[]{R.id.tvTitulo, R.id.tvBody, R.id.tvDate};

        SimpleCursorAdapter wallets =
                new SimpleCursorAdapter(this, R.layout.notesbudget_row, mWalletCursor, from2, to2);
        lv_pocket.setAdapter(wallets);

        lv_pocket.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                updateWallet(position,id);
                fillData();
            }
        });

        lv_bgnotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor c = mBudgetCursor;
                c.moveToPosition(position);
                Intent i = new Intent(getApplicationContext(),NoteBudget.class);
                i.putExtra(BudgetDbAdapter.KEY_ROWID,id);
                i.putExtra(BudgetDbAdapter.KEY_TITLE, c.getString(c.getColumnIndexOrThrow(BudgetDbAdapter.KEY_TITLE)));
                i.putExtra(BudgetDbAdapter.KEY_GENDER, c.getString(c.getColumnIndexOrThrow(BudgetDbAdapter.KEY_GENDER)));
                i.putExtra(BudgetDbAdapter.KEY_WALLET, c.getString(c.getColumnIndexOrThrow(BudgetDbAdapter.KEY_WALLET)));
                i.putExtra(BudgetDbAdapter.KEY_TYPE, c.getString(c.getColumnIndexOrThrow(BudgetDbAdapter.KEY_TYPE)));
                i.putExtra(BudgetDbAdapter.KEY_VALUE, c.getString(c.getColumnIndexOrThrow(BudgetDbAdapter.KEY_VALUE)));
                i.putExtra(BudgetDbAdapter.KEY_OBS, c.getString(c.getColumnIndexOrThrow(BudgetDbAdapter.KEY_OBS)));
                i.putExtra(BudgetDbAdapter.KEY_PHOTO, c.getString(c.getColumnIndexOrThrow(BudgetDbAdapter.KEY_PHOTO)));
                startActivityForResult(i,ACTIVITY_EDIT);
            }
        });
        //Actualiar o total em cima mostrado
        verificaDinheiro(mDbBudgetHelper.fetchNotesByParameter(BudgetDbAdapter.KEY_VALUE),mDbBudgetHelper.fetchNotesByParameter(BudgetDbAdapter.KEY_GENDER));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sup_budget, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void createBudgetAction(MenuItem item){
            Intent i= new Intent(this,NoteBudget.class);
            startActivityForResult(i,ACTIVITY_CREATE);
    }

    public void createWalletAction(MenuItem item){
            Intent i= new Intent(this,Wallet.class);
            startActivityForResult(i,ACTIVITY_WALLET_CREATE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater =getMenuInflater();

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
        switch (item.getItemId()){
            case R.id.delete_note:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mDbBudgetHelper.deleteNote(info.id);
                fillData();
                return true;
            case R.id.delete_pocket:
                AdapterView.AdapterContextMenuInfo info_carteira = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Cursor c = mDbWalletHelper.fetchWallet(info_carteira.id);
                List<classBudgetNote> dados = mDbBudgetHelper.fetchNote(c.getString(2));

                System.out.println(dados.size());

                for(int i = 0; i < dados.size() ; i++) {
                    mDbBudgetHelper.deleteNote(dados.get(i).getId());
                }

                mDbWalletHelper.deleteWallet(info_carteira.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    public void verificaDinheiro(String valores[],String generos[]){
        Double total = 0.00;
        for(int i = 0; i<valores.length; i++){
            if(generos[i].toString().compareTo(getString(R.string.despesa))==0)
                total = total - Double.parseDouble(valores[i].toString());
            else
                total = total + Double.parseDouble(valores[i].toString());
        }
        tvTOTAL.setText(getString(R.string.budgetActivityTotal)+ total + getString(R.string.budgetActivityMoney));

        if(total<0)
            tvTOTAL.setBackgroundColor(0xffff0000);
        else
            tvTOTAL.setBackgroundColor(0xff00ff22);
    }

    private void updateWallet(int position, long id) {
        Intent i= new Intent(this,Wallet.class);
        Cursor c = mWalletCursor;
        c.moveToPosition(position);
        i.putExtra(WalletDbAdapter.KEY_ROWID,id);
        i.putExtra(WalletDbAdapter.KEY_TITLE, c.getString(c.getColumnIndexOrThrow(WalletDbAdapter.KEY_TITLE)));
        i.putExtra(WalletDbAdapter.KEY_TYPE, c.getString(c.getColumnIndexOrThrow(WalletDbAdapter.KEY_TYPE)));
        i.putExtra(WalletDbAdapter.KEY_VALUE, c.getString(c.getColumnIndexOrThrow(WalletDbAdapter.KEY_VALUE)));
        startActivityForResult(i,ACTIVITY_UPDATE_WALLET);
    }

    private void actualizaCarteiras(String nome, String value, String gender){
        Double aux = 0.0;

        String[] data = mDbWalletHelper.getRowNome(nome.trim());

        if(gender.compareTo(getString(R.string.despesa))==0)
            aux = (- Double.parseDouble(value));
        else
            aux = Double.parseDouble(value);
        Double vaux = Double.parseDouble(data[3]) + aux;

        mDbWalletHelper.updateWallet(Long.decode(data[0]),data[2],data[1],vaux);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(resultCode == RESULT_OK) {

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

                    actualizaCarteiras(wallet,value, gender);

                    mDbBudgetHelper.createNote(type, title, gender, wallet, obs,imageURL,Double.parseDouble(value));

                    fillData();

                    break;
                case ACTIVITY_WALLET_CREATE:
                    String titleWallet = extras.getString(WalletDbAdapter.KEY_TITLE);
                    String typeWallet = extras.getString(WalletDbAdapter.KEY_TYPE);
                    String valueWallet = extras.getString(WalletDbAdapter.KEY_VALUE);

                    mDbWalletHelper.createWallet(typeWallet, titleWallet,Double.parseDouble(valueWallet));
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

                        actualizaCarteiras(walletU,valueU, genderU);

                        mDbBudgetHelper.updateNote(mRowId, typeU, titleU,genderU, walletU,obsU,imageURLU,Double.valueOf(valueU));
                    }
                    fillData();
                    break;
                case ACTIVITY_UPDATE_WALLET:
                    Long mWalletRowId = extras.getLong(WalletDbAdapter.KEY_ROWID);
                    if (mWalletRowId != null) {
                        String titleU = extras.getString(WalletDbAdapter.KEY_TITLE);
                        String typeU = extras.getString(WalletDbAdapter.KEY_TYPE);
                        String valueU = extras.getString(WalletDbAdapter.KEY_VALUE);

                        mDbWalletHelper.updateWallet(mWalletRowId,typeU,titleU,Double.valueOf(valueU));
                    }

                    fillData();

                    break;
                default:
            }
        }
    }
}
