package pt.uc.cm.daylistudent.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import pt.uc.cm.daylistudent.R;
import pt.uc.cm.daylistudent.adapters.ArrayWalletAdapter;
import pt.uc.cm.daylistudent.adapters.BudgetDbAdapter;
import pt.uc.cm.daylistudent.adapters.WalletDbAdapter;
import pt.uc.cm.daylistudent.models.BudgetNote;
import pt.uc.cm.daylistudent.utils.SharedPreferencesUtils;

// TODO: Change this to a fragment
public class Wallet extends AppCompatActivity {
    private static final String TAG = Wallet.class.getSimpleName();

    EditText edtxtTitle, edtxtValor, edtxtTipo;
    String nome;
    Button confirmWalletButton;
    Long mRowId;
    WalletDbAdapter mDbHelper;
    BudgetDbAdapter mDbBudgetHelper;

    @Override
    public void onBackPressed() {
        Intent mIntent = new Intent();
        setResult(RESULT_CANCELED, mIntent);
        super.onBackPressed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferencesUtils.INSTANCE.readPreferencesUser(getApplicationContext());
        mDbBudgetHelper = new BudgetDbAdapter(this);

        setContentView(R.layout.wallet);
        setTitle(getString(R.string.criar_carteira));

        mDbHelper = new WalletDbAdapter(this);

        edtxtTitle = findViewById(R.id.edtTituloWallet);
        edtxtValor = findViewById(R.id.edtValorWallet);
        edtxtTipo = findViewById(R.id.edtTipoWallet);
        confirmWalletButton = findViewById(R.id.btConfirmarWallet);

        mRowId = null;

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            setTitle(getString(R.string.walletTitle));
            String title = extras.getString(WalletDbAdapter.KEY_TITLE);
            nome = title;
            String type = extras.getString(WalletDbAdapter.KEY_TYPE);
            String value = extras.getString(WalletDbAdapter.KEY_VALUE);
            mRowId = extras.getLong(WalletDbAdapter.KEY_ROWID);

            if (title != null)
                edtxtTitle.setText(title);
            if (value != null)
                edtxtValor.setText(value);
            if (type != null)
                edtxtTipo.setText(type);
            edtxtTitle.setEnabled(false);
            edtxtValor.setEnabled(false);
            edtxtTipo.setEnabled(false);

            fillListView(title);
        }

        confirmWalletButton.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            if (edtxtTitle.getText().length() > 0) {
                bundle.putString(BudgetDbAdapter.KEY_TITLE, edtxtTitle.getText().toString());
                bundle.putString(BudgetDbAdapter.KEY_TYPE, edtxtTipo.getText().toString());
                bundle.putString(BudgetDbAdapter.KEY_VALUE, edtxtValor.getText().toString());

                if (mRowId != null) //ou seja é uma edição?
                    bundle.putLong(BudgetDbAdapter.KEY_ROWID, mRowId);

                Intent mIntent = new Intent();
                mIntent.putExtras(bundle);
                setResult(RESULT_OK, mIntent);
                finish();
            } else
                Toast.makeText(getApplicationContext(), R.string.walletEmptyTitle, Toast.LENGTH_LONG).show();
        });
    }

    private void fillListView(String title) {
        mDbBudgetHelper.open();

        ArrayList<BudgetNote> walletBudgetNotes = mDbBudgetHelper.getNotesOfThisWallet(title);

        mDbBudgetHelper.close();

        // Create the adapter to convert the array to views
        ArrayWalletAdapter adapter = new ArrayWalletAdapter(this, walletBudgetNotes);
        // Attach the adapter to a ListView
        ListView listView = findViewById(R.id.lvWallet);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater();
        return super.onCreateOptionsMenu(menu);
    }
}
