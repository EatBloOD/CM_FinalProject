package com.example.jorge.tpamov.Activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.util.List;


import com.example.jorge.tpamov.*;
import com.example.jorge.tpamov.DataBase.BudgetDbAdapter;
import com.example.jorge.tpamov.DataBase.GlobalNotesDbAdapter;
import com.example.jorge.tpamov.DataBase.NotesDbAdapter;
import com.example.jorge.tpamov.DataBase.WalletDbAdapter;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String NOME = "nameKey";
    public static final String EMAIL = "emailKey";
    public static final String PASSWORD = "passwordKey";

    TextView tvUserName, tvEmail, receitas, despesas,
            wallet1, wallet2, total, wallet1_value,
            wallet2_value, note1, note2, note3,
            note1OBS,note2OBS,note3OBS,
            globalNote1, globalNote2,globalNote3,
            globalNote1Title, globalNote2Title,globalNote3Title;

    ImageView ivNote1,ivNote2,ivNote3,ivGlobalNote1,ivGlobalNote2,ivGlobalNote3, ivWallet1, ivWallet2;


    private BudgetDbAdapter mDbBudgetHelper;
    private WalletDbAdapter mDbWalletHelper;
    private NotesDbAdapter mDbNotesHelper;
    private GlobalNotesDbAdapter mDbGlobalNotesHelper;

    protected DrawerLayout drawer;

    SharedPreferences sharedPreferences;

    @Override
    protected void onStart() {
        super.onStart();
        try {
            preencheCampos();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostResume() {
        readPreferencesUser();
        readInfoUser();
        super.onPostResume();

        System.out.println("ONPOSTRESUME");
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            preencheCampos();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println("ONRESUME");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // Refresh main activity upon close of dialog box
        Intent refresh = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(refresh);
        finish();
        System.out.println("ONRESTART");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readPreferencesUser();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.mainActivityTitle));

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        tvUserName = (TextView) navigationView.findViewById(R.id.tvUserName);
        tvEmail = (TextView) navigationView.findViewById(R.id.tvEmail);

        checkFirstRun();

        try {
            preencheCampos();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void preencheCampos() throws ParseException {
        //1ºLayout
        receitas = (TextView) findViewById(R.id.tvResumoReceitasV_cMain);
        despesas = (TextView) findViewById(R.id.tvResumoDespesasV_cMain);
        total = (TextView) findViewById(R.id.tvResumoTotalV_cMain);

        wallet1 = (TextView) findViewById(R.id.tvWallet1_cMain);
        wallet2 = (TextView) findViewById(R.id.tvWallet2_cMain);
        wallet1_value = (TextView) findViewById(R.id.tvWallet_1_Value_cMain);
        wallet2_value = (TextView) findViewById(R.id.tvWallet_2_Value_cMain);

        note1 = (TextView) findViewById(R.id.tvNote1_cMain);
        note2 = (TextView) findViewById(R.id.tvNote2_cMain);
        note3 = (TextView) findViewById(R.id.tvNote3_cMain);

        note1OBS = (TextView) findViewById(R.id.tvNote1oBS_cMain);
        note2OBS = (TextView) findViewById(R.id.tvNote2OBS_cMain);
        note3OBS = (TextView) findViewById(R.id.tvNote3OBS_cMain);

        globalNote1 = (TextView) findViewById(R.id.tvGlobalNote_author1);
        globalNote2 = (TextView) findViewById(R.id.tvGlobalNote_author2);
        globalNote3 = (TextView) findViewById(R.id.tvGlobalNote_author3);

        globalNote1Title = (TextView) findViewById(R.id.tvGlobalNote_title1);
        globalNote2Title = (TextView) findViewById(R.id.tvGlobalNote_title2);
        globalNote3Title = (TextView) findViewById(R.id.tvGlobalNote_title3);

        ivNote1 = (ImageView) findViewById(R.id.ivNote1);
        ivNote2 = (ImageView) findViewById(R.id.ivNote2);
        ivNote3 = (ImageView) findViewById(R.id.ivNote3);

        ivGlobalNote1 = (ImageView) findViewById(R.id.ivGlobalNote1);
        ivGlobalNote2 = (ImageView) findViewById(R.id.ivGlobalNote2);
        ivGlobalNote3 = (ImageView) findViewById(R.id.ivGlobalNote3);

        ivWallet1 = (ImageView) findViewById(R.id.ivWallet1);
        ivWallet2 = (ImageView) findViewById(R.id.ivWallet2);





        //abrir a base de dados das notas budget
        mDbBudgetHelper = new BudgetDbAdapter(this);
        mDbWalletHelper = new WalletDbAdapter(this);
        mDbNotesHelper = new NotesDbAdapter(this);
        mDbGlobalNotesHelper = new GlobalNotesDbAdapter(this);

        mDbBudgetHelper.open();
        mDbWalletHelper.open();
        mDbNotesHelper.open();
        mDbGlobalNotesHelper.open();

        List<Double> info = mDbBudgetHelper.fetchTodayExpences();
        List<String[]> walletsMoney = mDbWalletHelper.getWalletsMoney();
        List<String[]> notes = mDbNotesHelper.getLatestNotes();
        List<String[]> globalNotes = mDbGlobalNotesHelper.getLatestGlobalNotes();


        mDbBudgetHelper.close();
        mDbWalletHelper.close();
        mDbNotesHelper.close();

        receitas.setText(info.get(0).toString()+getString(R.string.budgetActivityMoney));
        despesas.setText(info.get(1).toString()+getString(R.string.budgetActivityMoney));
        Double aux = info.get(0) + info.get(1);
        total.setText(aux.toString() +getString(R.string.budgetActivityMoney));

        if(walletsMoney.get(0)[0].equals("N/A")) {
            wallet1.setVisibility(View.INVISIBLE);
            wallet1_value.setVisibility(View.INVISIBLE);
            ivWallet1.setVisibility(View.INVISIBLE);
        }else{
            wallet1.setText(walletsMoney.get(0)[1].toString());
            wallet1_value.setText(walletsMoney.get(0)[3].toString() + getString(R.string.budgetActivityMoney));
        }

        if(walletsMoney.get(1)[0].equals("N/A")) {
            wallet2.setVisibility(View.INVISIBLE);
            wallet2_value.setVisibility(View.INVISIBLE);
            ivWallet2.setVisibility(View.INVISIBLE);
        }else {
            wallet2.setText(walletsMoney.get(1)[1].toString());
            wallet2_value.setText(walletsMoney.get(1)[3].toString() + getString(R.string.budgetActivityMoney));
        }
        if(notes.get(0)[0].equals("N/A")) {
            note1.setVisibility(View.INVISIBLE);
            note1OBS.setVisibility(View.INVISIBLE);
            ivNote1.setVisibility(View.INVISIBLE);
        }else{
            note1.setText(notes.get(0)[0]);
            note1OBS.setText(notes.get(0)[1]);
        }

        if(notes.get(1)[0].equals("N/A")) {
            note2.setVisibility(View.INVISIBLE);
            note2OBS.setVisibility(View.INVISIBLE);
            ivNote2.setVisibility(View.INVISIBLE);
        }else{
            note2.setText(notes.get(1)[0]);
            note2OBS.setText(notes.get(1)[1]);
        }

        if(notes.get(2)[0].equals("N/A")){
            note3.setVisibility(View.INVISIBLE);
            note3OBS.setVisibility(View.INVISIBLE);
            ivNote3.setVisibility(View.INVISIBLE);
        }else {
            note3.setText(notes.get(2)[0]);
            note3OBS.setText(notes.get(2)[1]);
        }

        if(globalNotes.get(0)[0].equals("N/A")) {
            globalNote1.setVisibility(View.INVISIBLE);
            globalNote1Title.setVisibility(View.INVISIBLE);
            ivGlobalNote1.setVisibility(View.INVISIBLE);
        }else{
            globalNote1.setText(globalNotes.get(0)[0]);
            globalNote1Title.setText(globalNotes.get(0)[1]);
        }

        if(globalNotes.get(1)[0].equals("N/A")) {
            globalNote2.setVisibility(View.INVISIBLE);
            globalNote2Title.setVisibility(View.INVISIBLE);
            ivGlobalNote2.setVisibility(View.INVISIBLE);
        }else{
            globalNote2.setText(globalNotes.get(1)[0]);
            globalNote2Title.setText(globalNotes.get(1)[1]);
        }

        if(globalNotes.get(2)[0].equals("N/A")){
            globalNote3.setVisibility(View.INVISIBLE);
            globalNote3Title.setVisibility(View.INVISIBLE);
            ivGlobalNote3.setVisibility(View.INVISIBLE);
        }else {
            globalNote3.setText(globalNotes.get(2)[0]);
            globalNote3Title.setText(globalNotes.get(2)[1]);
        }

        mDbWalletHelper.close();

    }

    //VERIFICA SE É A PRIMEIRA UTILIZAÇÃO DA APP
    public void checkFirstRun() {
        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun) {
            // Place your dialog code here to display the dialog
            new AlertDialog.Builder(this).setCancelable(false)
                    .setTitle(R.string.mainActivityTitle)
                    .setMessage(R.string.mainActivityWelcomeDialog)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            registo();
                        }
                    }).setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
                        return false;
                    return true;
                }
            })
                    .setIcon(R.drawable.icon)
                    .show();
            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply();
        }
        readInfoUser();
    }

    public void registo() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.register);
        dialog.setTitle(R.string.mainActivityRegister);
        dialog.setCancelable(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
                    return false;
                return true;
            }
        });
        // APANHA AS REFERÊNCIAS PARA TODOS OS OBJECTOS NECESSÁRIOS
        final EditText editTextUserName=(EditText)dialog.findViewById(R.id.editTextUserName);
        final  EditText editTextPassword=(EditText)dialog.findViewById(R.id.editTextPassword);
        final  EditText editTextConfirmPassword=(EditText)dialog.findViewById(R.id.editTextConfirmPassword);
        final  EditText editTextEmail=(EditText)dialog.findViewById(R.id.editTextEmail);

        final Button btnRegister=(Button)dialog.findViewById(R.id.buttonCreateAccount);

        // REGISTAR LISTENER
        btnRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // VAI BUSCAR O USER NAME E A PASSWORD
                String userName=editTextUserName.getText().toString();
                String email=editTextEmail.getText().toString();
                String password=editTextPassword.getText().toString();
                String confirmPassword=editTextConfirmPassword.getText().toString();
                // VERIFICA SE ESTÁ TUDO BEM FORMATADO
                if(userName.equals("")||password.equals("")||confirmPassword.equals(""))
                {
                    Toast.makeText(getApplicationContext(), R.string.mainActivityRegisterEmptyFields, Toast.LENGTH_LONG).show();
                    return;
                }
                // VERIFICA SE AS PASSWORDS SÃO IGUAIS
                if(!password.equals(confirmPassword))
                {
                    Toast.makeText(getApplicationContext(), R.string.mainActivityRegisterPasNotMatch, Toast.LENGTH_LONG).show();
                    return;
                }
                if(!email.contains("@") || !email.contains("."))
                {
                    Toast.makeText(getApplicationContext(), R.string.mainActivityRegisterEmail, Toast.LENGTH_LONG).show();
                    return;
                }
                else {
                        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        editor.putString(NOME, userName);
                        editor.putString(EMAIL, email);
                        editor.putString(PASSWORD, password);
                        editor.commit();

                    readInfoUser();
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    private boolean readInfoUser(){

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        //COLOCA A INFORMAÇÃO NA SIDEBAR
        tvUserName.setText(sharedPreferences.getString(NOME,"DEFAULT"));
        tvEmail.setText(sharedPreferences.getString(EMAIL,"DEFAULT"));

        return true;
    }

    private boolean readPreferencesUser(){
        int textSize = -1;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_home) {
        } else if (id == R.id.nav_notes) {
            Intent i= new Intent(this,DayliStudent_Activity.class);
            startActivity(i);
        } else if (id == R.id.nav_budget) {
            Intent i= new Intent(this,Budget_Activity.class);
            startActivity(i);
        } else if (id == R.id.nav_statistics) {
            Intent i= new Intent(this,Statistics_Activity.class);
            startActivity(i);
        }else if (id == R.id.nav_global_notes) {
            Intent i= new Intent(this,GlobalNotes.class);
            startActivity(i);
        }else if (id == R.id.nav_credits) {
            Intent i= new Intent(this,Credits_Activity.class);
            startActivity(i);
        }  else if (id == R.id.nav_settings) {
            Intent i= new Intent(this,SettingsActivity.class);
            startActivity(i);
        }else if ( id == R.id.nav_sair){
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void onSettingsAction(MenuItem item) {
        Intent modifySettings  =new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(modifySettings);
    }

    public void goNoteList(View view) {
        Intent i= new Intent(this,DayliStudent_Activity.class);
        startActivity(i);
    }
}
