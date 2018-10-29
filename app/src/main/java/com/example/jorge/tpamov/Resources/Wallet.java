package com.example.jorge.tpamov.Resources;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.jorge.tpamov.DataBase.BudgetDbAdapter;
import com.example.jorge.tpamov.DataBase.WalletDbAdapter;
import com.example.jorge.tpamov.ListViewAdapter.ArrayWalletAdapter;
import com.example.jorge.tpamov.Classes.classBudgetNote;
import com.example.jorge.tpamov.R;

import java.util.ArrayList;

/**
 * Created by Jorge on 27/11/2016.
 */
public class Wallet extends AppCompatActivity {
    EditText edtxtTitle, edtxtValor,edtxtTipo;
    String nome;
    Button confirmWalletButton;
    Long mRowId;
    WalletDbAdapter mDbHelper;
    BudgetDbAdapter mDbBudgetHelper;
    SharedPreferences sharedPreferences;

    @Override
    public void onBackPressed() {
        Intent mIntent = new Intent();
        setResult(RESULT_CANCELED,mIntent);
        super.onBackPressed();

    }

    private boolean readPreferencesUser(){
        int textSize = -1;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Wallet.this);

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
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        readPreferencesUser();
        mDbBudgetHelper = new BudgetDbAdapter(this);

        setContentView(R.layout.wallet);
        setTitle(getString(R.string.criar_carteira));

        mDbHelper = new WalletDbAdapter(this);

        edtxtTitle = (EditText) findViewById(R.id.edtTituloWallet);
        edtxtValor = (EditText) findViewById(R.id.edtValorWallet);
        edtxtTipo = (EditText) findViewById(R.id.edtTipoWallet);
        confirmWalletButton = (Button) findViewById(R.id.btConfirmarWallet);

        mRowId = null;

        Bundle extras = getIntent().getExtras();

        if(extras != null) {
            setTitle(getString(R.string.walletTitle));
            String title = extras.getString(WalletDbAdapter.KEY_TITLE);
            nome = title;
            String type = extras.getString(WalletDbAdapter.KEY_TYPE);
            String value = extras.getString(WalletDbAdapter.KEY_VALUE);
            mRowId = extras.getLong(WalletDbAdapter.KEY_ROWID);

            if(title != null)
                edtxtTitle.setText(title);
            if(value != null)
                edtxtValor.setText(value);
            if(type != null)
                edtxtTipo.setText(type);
            edtxtTitle.setEnabled(false);
            edtxtValor.setEnabled(false);
            edtxtTipo.setEnabled(false);

            fillListView(title);
        }

        confirmWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                if(edtxtTitle.getText().length()>0){
                    bundle.putString(BudgetDbAdapter.KEY_TITLE, edtxtTitle.getText().toString());
                    bundle.putString(BudgetDbAdapter.KEY_TYPE, edtxtTipo.getText().toString());
                    bundle.putString(BudgetDbAdapter.KEY_VALUE, edtxtValor.getText().toString());

                    if (mRowId != null) //ou seja é uma edição?
                        bundle.putLong(BudgetDbAdapter.KEY_ROWID, mRowId);

                    Intent mIntent = new Intent();
                    mIntent.putExtras(bundle);
                    setResult(RESULT_OK, mIntent);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), R.string.walletEmptyTitle, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void fillListView(String title) {
        mDbBudgetHelper.open();

        ArrayList<classBudgetNote> notasDestaCarteira = mDbBudgetHelper.getNotesOfThisWallet(title);

        mDbBudgetHelper.close();

        // Create the adapter to convert the array to views
        ArrayWalletAdapter adapter = new ArrayWalletAdapter(this, notasDestaCarteira);
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.lvWallet);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        return super.onCreateOptionsMenu(menu);
    }

}

