package com.example.jorge.tpamov.Resources;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.Toast;

import com.example.jorge.tpamov.Activity.Camera_Activity;
import com.example.jorge.tpamov.DataBase.BudgetDbAdapter;
import com.example.jorge.tpamov.DataBase.NotesDbAdapter;
import com.example.jorge.tpamov.DataBase.WalletDbAdapter;
import com.example.jorge.tpamov.ListViewAdapter.MySpinnerAdapter;
import com.example.jorge.tpamov.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jorge on 27/11/2016.
 */
public class NoteBudget extends AppCompatActivity {

    EditText edtxtTitle, edtxtValor,edtxtDesc;
    ImageView myImage;
    Spinner spLucroDespesa, spTipoValor, spContas;

    Long mRowId = null; //Guardar a mRowId que está a ser editada
    String tipo[],categoria[],imageURL;
    private static final int ACTIVITY_PHOTO=0;
    String photo;

    SharedPreferences sharedPreferences;

    //ARRAYS DE IMAGENS QUE POSTERIORMENTE NÃO VAO ESTAR AQUI
    Integer[] images_categoria = {0, R.drawable.bar, R.drawable.cantina,R.drawable.material,
            R.drawable.compras, R.drawable.jantares, R.drawable.viagens,R.drawable.propinas,
            R.drawable.apostas,R.drawable.trabalho,R.drawable.house, R.drawable.pawprint,
            R.drawable.tie,R.drawable.groceries,R.drawable.hospital,R.drawable.lodging};
    Integer[] images_tipo = {0, R.drawable.lucro, R.drawable.despesa};

    List<String> objects;

    //BASE DE DADOS
    BudgetDbAdapter mDbHelper;
    WalletDbAdapter mDbWallet;

    @Override
    public void onBackPressed() {
        Intent mIntent = new Intent();
        setResult(RESULT_CANCELED,mIntent);
        super.onBackPressed();

    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        readPreferencesUser();
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
        edtxtTitle = (EditText) findViewById(R.id.edTituloBg);
        edtxtValor = (EditText) findViewById(R.id.edtValor);
        edtxtDesc = (EditText) findViewById(R.id.edtDesc);
        myImage = (ImageView) findViewById(R.id.imageView12);

        //SPINNER'S
        spLucroDespesa = (Spinner) findViewById(R.id.spLucroDespesa);
        spTipoValor = (Spinner) findViewById(R.id.spTipoValor);
        spContas = (Spinner) findViewById(R.id.spContas);

        //ABRIR A BASE DE DADOS
        mDbWallet.open();

        objects = new ArrayList<>();
        objects.add("N/D");
        objects.addAll(mDbWallet.fetchAllWalletsByParameter("title"));

        //INICIALIZA OS SPINNERS
        spLucroDespesa.setAdapter(new MySpinnerAdapter(NoteBudget.this, R.layout.spinner_row,
                tipo, images_tipo, getLayoutInflater()));
        spTipoValor.setAdapter(new MySpinnerAdapter(NoteBudget.this, R.layout.spinner_row,
                categoria, images_categoria, getLayoutInflater()));

        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, objects);

        spContas.setAdapter(adapter3);

        //OBTEM INFO DE OUTRA ACTIVIDADE
        Bundle extras = getIntent().getExtras();

        //SE TIVER INFO ( EDIT BUDGETNOTE ) PREENCHE OS CAMPOS
        if(extras != null) {
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

            if(title != null)
                edtxtTitle.setText(title);
            if(gender != null)
                spLucroDespesa.setSelection(getIndex(spLucroDespesa, gender));
            if(type != null)
                spTipoValor.setSelection(getIndex(spTipoValor, type));
            if(wallet != null)
                spContas.setSelection(getIndex(spContas, wallet));
            if(value != null)
                edtxtValor.setText(value);
            if(obs != null)
                edtxtDesc.setText(obs);
            if(photo != null) {
                criaFicheiro(photo);
            }
        }

        //ADICIONAR MAIS OPS DE CATEGORIA
        spContas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {

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

    private boolean readPreferencesUser(){
        int textSize = -1;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(NoteBudget.this);

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

    //OBTER O INDEX DA LINHA DO SPINNER
    private int getIndex(Spinner spinner, String myString){

        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).equals(myString)){
                Log.i("TIPO",spinner.getItemAtPosition(i).toString());
                index = i;
            }
        }
        Log.i("SELECIONAR TIPO",spinner.getItemAtPosition(index).toString());
        return index;
    }

    public void saveAction(MenuItem item) {
        Bundle bundle = new Bundle();
        try {
            Double dinheiro = Double.parseDouble(edtxtValor.getText().toString());
            if (edtxtTitle.getText().length() > 0
                    && spContas.getSelectedItem().toString().compareTo("N/D")!=0
                    && spLucroDespesa.getSelectedItem().toString().compareTo("N/D")!=0
                    && spTipoValor.getSelectedItem().toString().compareTo("N/D")!=0
                    && dinheiro != null) {
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
                if(edtxtTitle.getText().length() <=0){
                    Toast.makeText(getApplicationContext(), R.string.noteBudgetEmptyTitle, Toast.LENGTH_LONG).show();
                }else if (spContas.getCount() <= 0) { // TEM QUE SER UM PORQUE MESMO QUE APAGUE TODAS TEM LA SEMPRE O N/D
                    Toast.makeText(getApplicationContext(), R.string.noteBudgetEmptyWallet, Toast.LENGTH_LONG).show();
                }else if (spContas.getSelectedItem() == null) {
                    Toast.makeText(getApplicationContext(), R.string.noteBudgetEmptyWallets, Toast.LENGTH_LONG).show();
                }else if (spLucroDespesa.getSelectedItem().toString().compareTo("N/D")==0) {
                    Toast.makeText(getApplicationContext(), R.string.noteBudgetEmptyType, Toast.LENGTH_LONG).show();
                }else if (spTipoValor.getSelectedItem().toString().compareTo("N/D") == 0) {
                    Toast.makeText(getApplicationContext(), R.string.noteBudgetEmptyCategorie, Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), R.string.noteBudgetEmpty, Toast.LENGTH_LONG).show();
                }
            }
        }catch (NumberFormatException e){
            Toast.makeText(getApplicationContext(), R.string.noteBudgetNumberException, Toast.LENGTH_LONG).show();
        }
    }

    public void tirarFoto(View view) {
        Intent i= new Intent(this,Camera_Activity.class);
        startActivityForResult(i,ACTIVITY_PHOTO);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(resultCode == RESULT_OK) {
            Bundle extras = intent.getExtras();
            switch (requestCode) {
                case ACTIVITY_PHOTO:

                    imageURL = extras.getString("TITULO");

                    System.out.println(imageURL);

                    criaFicheiro(imageURL);

                    break;
                default:
                    Log.i("DAYLISTUDENT", "ERRO BACK");
            }
        }else{
        }
    }

    public void criaFicheiro(String imagem){
        File imgFile = new  File(imagem);

        if(imgFile.exists())
        {
            myImage.setImageURI(Uri.fromFile(imgFile));
        }
    }

    public void abrirGaleria(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri imgUri = Uri.parse("file://" + photo);
        intent.setDataAndType(imgUri, "image/*");
        startActivity(intent);
    }
}
