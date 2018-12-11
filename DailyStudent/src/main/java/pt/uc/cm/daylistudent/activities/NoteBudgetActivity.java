package pt.uc.cm.daylistudent.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pt.uc.cm.daylistudent.adapters.BudgetDbAdapter;
import pt.uc.cm.daylistudent.adapters.NotesDbAdapter;
import pt.uc.cm.daylistudent.adapters.WalletDbAdapter;
import pt.uc.cm.daylistudent.R;
import pt.uc.cm.daylistudent.adapters.MySpinnerAdapter;
import pt.uc.cm.daylistudent.utils.SharedPreferencesUtils;

public class NoteBudgetActivity extends AppCompatActivity {
    private final String TAG = NoteBudgetActivity.class.getSimpleName();

    EditText edtxtTitle, edtxtValor, edtxtDesc;
    ImageView myImage;
    Spinner spLucroDespesa, spTipoValor, spContas;

    Long mRowId = null; //Guardar a mRowId que está a ser editada
    String tipo[], categoria[], imageURL;
    private static final int ACTIVITY_PHOTO = 0;
    String photo;

    //ARRAYS DE IMAGENS QUE POSTERIORMENTE NÃO VAO ESTAR AQUI
    Integer[] images_categoria = {0, R.drawable.bar, R.drawable.cantina, R.drawable.material,
            R.drawable.compras, R.drawable.jantares, R.drawable.viagens, R.drawable.propinas,
            R.drawable.apostas, R.drawable.trabalho, R.drawable.house, R.drawable.pawprint,
            R.drawable.tie, R.drawable.groceries, R.drawable.hospital, R.drawable.lodging};
    Integer[] images_tipo = {0, R.drawable.lucro, R.drawable.despesa};

    List<String> objects;

    //BASE DE DADOS
    BudgetDbAdapter mDbHelper;
    WalletDbAdapter mDbWallet;

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
        setContentView(R.layout.budget_note_edit);
        setTitle(getString(R.string.noteBudgetTitle));

        //INICIALIZAÇÃO DAS STRINGS
        tipo = getResources().getStringArray(R.array.tipo);
        categoria = getResources().getStringArray(R.array.categoria);

        //INICIALIZAÇÃO À BASE DE DADOS
        mDbHelper = new BudgetDbAdapter(this);
        mDbWallet = new WalletDbAdapter(this);

        //INICIALIZAÇÃO DOS COMPONENTES
        //TEXTBOX'S
        edtxtTitle = findViewById(R.id.edTituloBg);
        edtxtValor = findViewById(R.id.edtValor);
        edtxtDesc = findViewById(R.id.edtDesc);
        myImage = findViewById(R.id.imageView12);

        //SPINNER'S
        spLucroDespesa = findViewById(R.id.spLucroDespesa);
        spTipoValor = findViewById(R.id.spTipoValor);
        spContas = findViewById(R.id.spContas);

        //ABRIR A BASE DE DADOS
        mDbWallet.open();

        objects = new ArrayList<>();
        objects.add("N/D");
        objects.addAll(mDbWallet.fetchAllWalletsByParameter("title"));

        //INICIALIZA OS SPINNERS
        spLucroDespesa.setAdapter(new MySpinnerAdapter(NoteBudgetActivity.this, R.layout.spinner_row,
                tipo, images_tipo, getLayoutInflater()));
        spTipoValor.setAdapter(new MySpinnerAdapter(NoteBudgetActivity.this, R.layout.spinner_row,
                categoria, images_categoria, getLayoutInflater()));

        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, objects);

        spContas.setAdapter(adapter3);

        //OBTEM INFO DE OUTRA ACTIVIDADE
        Bundle extras = getIntent().getExtras();

        //SE TIVER INFO ( EDIT BUDGETNOTE ) PREENCHE OS CAMPOS
        if (extras != null) {
            setTitle(getString(R.string.noteBudgetTitleEdit));
            String title = extras.getString(BudgetDbAdapter.KEY_TITLE);
            String gender = extras.getString(BudgetDbAdapter.KEY_GENDER);
            String type = extras.getString(BudgetDbAdapter.KEY_TYPE);
            String wallet = extras.getString(BudgetDbAdapter.KEY_WALLET);
            String value = extras.getString(BudgetDbAdapter.KEY_VALUE);
            String obs = extras.getString(BudgetDbAdapter.KEY_OBS);
            String image = extras.getString(BudgetDbAdapter.KEY_IMAGE);
            photo = extras.getString(BudgetDbAdapter.KEY_PHOTO);
            mRowId = extras.getLong(NotesDbAdapter.KEY_ROWID);

            if (title != null)
                edtxtTitle.setText(title);
            if (gender != null)
                spLucroDespesa.setSelection(getIndex(spLucroDespesa, gender));
            if (type != null)
                spTipoValor.setSelection(getIndex(spTipoValor, type));
            if (wallet != null)
                spContas.setSelection(getIndex(spContas, wallet));
            if (value != null)
                edtxtValor.setText(value);
            if (obs != null)
                edtxtDesc.setText(obs);
            if (photo != null) {
                createFile(photo);
            }
        }

        //ADICIONAR MAIS OPS DE CATEGORIA
        spContas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mDbWallet.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_budget_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //OBTER O INDEX DA LINHA DO SPINNER
    private int getIndex(Spinner spinner, String myString) {
        int index = 0;

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).equals(myString)) {
                Log.i("TIPO", spinner.getItemAtPosition(i).toString());
                index = i;
            }
        }
        Log.i("SELECIONAR TIPO", spinner.getItemAtPosition(index).toString());
        return index;
    }

    public void saveAction(MenuItem item) {
        Bundle bundle = new Bundle();
        try {
            Double money = Double.parseDouble(edtxtValor.getText().toString());
            if (edtxtTitle.getText().length() > 0
                    && spContas.getSelectedItem().toString().compareTo("N/D") != 0
                    && spLucroDespesa.getSelectedItem().toString().compareTo("N/D") != 0
                    && spTipoValor.getSelectedItem().toString().compareTo("N/D") != 0
                    && money != null) {
                bundle.putString(BudgetDbAdapter.KEY_TITLE, edtxtTitle.getText().toString());
                bundle.putString(BudgetDbAdapter.KEY_GENDER, spLucroDespesa.getSelectedItem().toString());
                bundle.putString(BudgetDbAdapter.KEY_WALLET, spContas.getSelectedItem().toString());
                bundle.putString(BudgetDbAdapter.KEY_TYPE, spTipoValor.getSelectedItem().toString());
                bundle.putString(BudgetDbAdapter.KEY_VALUE, edtxtValor.getText().toString());
                bundle.putString(BudgetDbAdapter.KEY_OBS, edtxtDesc.getText().toString());
                bundle.putString(BudgetDbAdapter.KEY_PHOTO, imageURL);

                if (mRowId != null) //OU SEJA UMA EDIÇÃO
                    bundle.putLong(BudgetDbAdapter.KEY_ROWID, mRowId);

                Intent mIntent = new Intent();

                mIntent.putExtras(bundle);

                setResult(RESULT_OK, mIntent);

                finish();
            } else {
                if (edtxtTitle.getText().length() <= 0) {
                    Snackbar snackbar  = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.noteBudgetEmptyTitle, Snackbar.LENGTH_LONG);
                    TextView snackbarTextView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                    snackbar.getView().setBackgroundColor(Color.RED);
                    snackbarTextView.setTextColor(Color.WHITE);
                    snackbarTextView.setTypeface(snackbarTextView.getTypeface(), Typeface.BOLD);
                    snackbar.show();
                } else if (spContas.getCount() <= 0) { // TEM QUE SER UM PORQUE MESMO QUE APAGUE TODAS TEM LA SEMPRE O N/D
                    Snackbar snackbar  = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.noteBudgetEmptyWallet, Snackbar.LENGTH_LONG);
                    TextView snackbarTextView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                    snackbar.getView().setBackgroundColor(Color.RED);
                    snackbarTextView.setTextColor(Color.WHITE);
                    snackbarTextView.setTypeface(snackbarTextView.getTypeface(), Typeface.BOLD);
                    snackbar.show();
                } else if (spContas.getSelectedItem() == null) {
                    Snackbar snackbar  = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.noteBudgetEmptyWallets, Snackbar.LENGTH_LONG);
                    TextView snackbarTextView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                    snackbar.getView().setBackgroundColor(Color.RED);
                    snackbarTextView.setTextColor(Color.WHITE);
                    snackbarTextView.setTypeface(snackbarTextView.getTypeface(), Typeface.BOLD);
                    snackbar.show();
                } else if (spLucroDespesa.getSelectedItem().toString().compareTo("N/D") == 0) {
                    Snackbar snackbar  = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.noteBudgetEmptyType, Snackbar.LENGTH_LONG);
                    TextView snackbarTextView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                    snackbar.getView().setBackgroundColor(Color.RED);
                    snackbarTextView.setTextColor(Color.WHITE);
                    snackbarTextView.setTypeface(snackbarTextView.getTypeface(), Typeface.BOLD);
                    snackbar.show();
                } else if (spTipoValor.getSelectedItem().toString().compareTo("N/D") == 0) {
                    Snackbar snackbar  = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.noteBudgetEmptyCategorie, Snackbar.LENGTH_LONG);
                    TextView snackbarTextView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                    snackbar.getView().setBackgroundColor(Color.RED);
                    snackbarTextView.setTextColor(Color.WHITE);
                    snackbarTextView.setTypeface(snackbarTextView.getTypeface(), Typeface.BOLD);
                    snackbar.show();
                } else {
                    Snackbar snackbar  = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.noteBudgetEmpty, Snackbar.LENGTH_LONG);
                    TextView snackbarTextView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                    snackbar.getView().setBackgroundColor(Color.RED);
                    snackbarTextView.setTextColor(Color.WHITE);
                    snackbarTextView.setTypeface(snackbarTextView.getTypeface(), Typeface.BOLD);
                    snackbar.show();
                }
            }
        } catch (NumberFormatException e) {
            Snackbar snackbar  = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.noteBudgetNumberException, Snackbar.LENGTH_LONG);
            TextView snackbarTextView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
            snackbar.getView().setBackgroundColor(Color.RED);
            snackbarTextView.setTextColor(Color.WHITE);
            snackbarTextView.setTypeface(snackbarTextView.getTypeface(), Typeface.BOLD);
            snackbar.show();
        }
    }

    public void takePicture(View view) {
        startActivityForResult(new Intent(this, CameraActivity.class), ACTIVITY_PHOTO);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK) {
            Bundle extras = intent.getExtras();
            switch (requestCode) {
                case ACTIVITY_PHOTO:
                    imageURL = extras.getString("TITULO");
                    Log.i(TAG, imageURL);
                    createFile(imageURL);
                    break;
                default:
                    Log.i(TAG, "Back error");
            }
        }
    }

    public void createFile(String imagem) {
        File imgFile = new File(imagem);

        if (imgFile.exists()) {
            myImage.setImageURI(Uri.fromFile(imgFile));
        }
    }

    public void openGallery(View view) {
        if(photo != null) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            File root = new File(photo);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.fromFile(root));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(Uri.fromFile(root), "image/*");
            startActivityForResult(intent, 1);
        }else {
            Log.i(TAG, "Não tens foto");
        }
    }
}
