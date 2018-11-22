package pt.uc.cm.daily_student.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import pt.uc.cm.daily_student.R;
import pt.uc.cm.daily_student.adapters.BudgetDbAdapter;
import pt.uc.cm.daily_student.adapters.GlobalNotesDbAdapter;
import pt.uc.cm.daily_student.adapters.NotesDbAdapter;
import pt.uc.cm.daily_student.adapters.WalletDbAdapter;

public class MainActivity extends StructureActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = MainActivity.class.getSimpleName();

    public static final String NOME = "nameKey";
    public static final String EMAIL = "emailKey";
    public static final String PASSWORD = "passwordKey";

    TextView tvUserName, tvEmail, receitas, despesas,
            wallet1, wallet2, total, wallet1_value,
            wallet2_value, note1, note2, note3,
            note1OBS, note2OBS, note3OBS,
            globalNote1, globalNote2, globalNote3,
            globalNote1Title, globalNote2Title, globalNote3Title;

    ImageView ivNote1, ivNote2, ivNote3, ivGlobalNote1, ivGlobalNote2, ivGlobalNote3, ivWallet1, ivWallet2;

    private BudgetDbAdapter mDbBudgetHelper;
    private WalletDbAdapter mDbWalletHelper;
    private NotesDbAdapter mDbNotesHelper;
    private GlobalNotesDbAdapter mDbGlobalNotesHelper;

    protected DrawerLayout drawer;

    @Override
    protected void onStart() {
        super.onStart();
        try {
            fillFields();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostResume() {
        readPreferencesUser();
        readInfoUser();
        super.onPostResume();

        Log.i(TAG, "onPostResume");
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            fillFields();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // Refresh main activity upon close of dialog box
        Intent refresh = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(refresh);
        finish();
        Log.i(TAG, "onRestart");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readPreferencesUser();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.mainActivityTitle));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    2);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    3);
        }

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0);
        tvUserName = headerLayout.findViewById(R.id.tvUserName);
        tvEmail = headerLayout.findViewById(R.id.tvEmail);

        checkFirstRun();

        try {
            fillFields();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void fillFields() throws ParseException {
        //1ºLayout
        receitas = findViewById(R.id.tvResumoReceitasV_cMain);
        despesas = findViewById(R.id.tvResumoDespesasV_cMain);
        total = findViewById(R.id.tvResumoTotalV_cMain);

        wallet1 = findViewById(R.id.tvWallet1_cMain);
        wallet2 = findViewById(R.id.tvWallet2_cMain);
        wallet1_value = findViewById(R.id.tvWallet_1_Value_cMain);
        wallet2_value = findViewById(R.id.tvWallet_2_Value_cMain);

        note1 = findViewById(R.id.tvNote1_cMain);
        note2 = findViewById(R.id.tvNote2_cMain);
        note3 = findViewById(R.id.tvNote3_cMain);

        note1OBS = findViewById(R.id.tvNote1oBS_cMain);
        note2OBS = findViewById(R.id.tvNote2OBS_cMain);
        note3OBS = findViewById(R.id.tvNote3OBS_cMain);

        globalNote1 = findViewById(R.id.tvGlobalNote_author1);
        globalNote2 = findViewById(R.id.tvGlobalNote_author2);
        globalNote3 = findViewById(R.id.tvGlobalNote_author3);

        globalNote1Title = findViewById(R.id.tvGlobalNote_title1);
        globalNote2Title = findViewById(R.id.tvGlobalNote_title2);
        globalNote3Title = findViewById(R.id.tvGlobalNote_title3);

        ivNote1 = findViewById(R.id.ivNote1);
        ivNote2 = findViewById(R.id.ivNote2);
        ivNote3 = findViewById(R.id.ivNote3);

        ivGlobalNote1 = findViewById(R.id.ivGlobalNote1);
        ivGlobalNote2 = findViewById(R.id.ivGlobalNote2);
        ivGlobalNote3 = findViewById(R.id.ivGlobalNote3);

        ivWallet1 = findViewById(R.id.ivWallet1);
        ivWallet2 = findViewById(R.id.ivWallet2);

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

        receitas.setText(info.get(0).toString() + getString(R.string.budgetActivityMoney));
        despesas.setText(info.get(1).toString() + getString(R.string.budgetActivityMoney));
        Double aux = info.get(0) + info.get(1);
        total.setText(aux.toString() + getString(R.string.budgetActivityMoney));

        if (walletsMoney.get(0)[0].equals("N/A")) {
            wallet1.setVisibility(View.INVISIBLE);
            wallet1_value.setVisibility(View.INVISIBLE);
            ivWallet1.setVisibility(View.INVISIBLE);
        } else {
            wallet1.setText(walletsMoney.get(0)[1]);
            wallet1_value.setText(walletsMoney.get(0)[3] + getString(R.string.budgetActivityMoney));
        }

        if (walletsMoney.get(1)[0].equals("N/A")) {
            wallet2.setVisibility(View.INVISIBLE);
            wallet2_value.setVisibility(View.INVISIBLE);
            ivWallet2.setVisibility(View.INVISIBLE);
        } else {
            wallet2.setText(walletsMoney.get(1)[1]);
            wallet2_value.setText(walletsMoney.get(1)[3] + getString(R.string.budgetActivityMoney));
        }
        if (notes.get(0)[0].equals("N/A")) {
            note1.setVisibility(View.INVISIBLE);
            note1OBS.setVisibility(View.INVISIBLE);
            ivNote1.setVisibility(View.INVISIBLE);
        } else {
            note1.setText(notes.get(0)[0]);
            note1OBS.setText(notes.get(0)[1]);
        }

        if (notes.get(1)[0].equals("N/A")) {
            note2.setVisibility(View.INVISIBLE);
            note2OBS.setVisibility(View.INVISIBLE);
            ivNote2.setVisibility(View.INVISIBLE);
        } else {
            note2.setText(notes.get(1)[0]);
            note2OBS.setText(notes.get(1)[1]);
        }

        if (notes.get(2)[0].equals("N/A")) {
            note3.setVisibility(View.INVISIBLE);
            note3OBS.setVisibility(View.INVISIBLE);
            ivNote3.setVisibility(View.INVISIBLE);
        } else {
            note3.setText(notes.get(2)[0]);
            note3OBS.setText(notes.get(2)[1]);
        }

        if (globalNotes.get(0)[0].equals("N/A")) {
            globalNote1.setVisibility(View.INVISIBLE);
            globalNote1Title.setVisibility(View.INVISIBLE);
            ivGlobalNote1.setVisibility(View.INVISIBLE);
        } else {
            globalNote1.setText(globalNotes.get(0)[0]);
            globalNote1Title.setText(globalNotes.get(0)[1]);
        }

        if (globalNotes.get(1)[0].equals("N/A")) {
            globalNote2.setVisibility(View.INVISIBLE);
            globalNote2Title.setVisibility(View.INVISIBLE);
            ivGlobalNote2.setVisibility(View.INVISIBLE);
        } else {
            globalNote2.setText(globalNotes.get(1)[0]);
            globalNote2Title.setText(globalNotes.get(1)[1]);
        }

        if (globalNotes.get(2)[0].equals("N/A")) {
            globalNote3.setVisibility(View.INVISIBLE);
            globalNote3Title.setVisibility(View.INVISIBLE);
            ivGlobalNote3.setVisibility(View.INVISIBLE);
        } else {
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
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> register()).setOnKeyListener((dialog, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
                    return false;
                return true;
            })
                    .setIcon(R.mipmap.ic_launcher)
                    .show();
            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply();
        }
        readInfoUser();
    }

    public void register() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.register);
        dialog.setTitle(R.string.mainActivityRegister);
        dialog.setCancelable(false);
        dialog.setOnKeyListener((dialog1, keyCode, event) ->
                keyCode != KeyEvent.KEYCODE_BACK || event.getAction() != KeyEvent.ACTION_UP);
        // APANHA AS REFERÊNCIAS PARA TODOS OS OBJECTOS NECESSÁRIOS
        final EditText editTextUserName = dialog.findViewById(R.id.editTextUserName);
        final EditText editTextPassword = dialog.findViewById(R.id.editTextPassword);
        final EditText editTextConfirmPassword = dialog.findViewById(R.id.editTextConfirmPassword);
        final EditText editTextEmail = dialog.findViewById(R.id.editTextEmail);

        final Button btnRegister = dialog.findViewById(R.id.buttonCreateAccount);

        // REGISTAR LISTENER
        btnRegister.setOnClickListener((v) -> {
            // VAI BUSCAR O USER NAME E A PASSWORD
            String userName = editTextUserName.getText().toString();
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();
            String confirmPassword = editTextConfirmPassword.getText().toString();
            // VERIFICA SE ESTÁ TUDO BEM FORMATADO
            if (userName.equals("") || password.equals("") || confirmPassword.equals("")) {
                Toast.makeText(getApplicationContext(), R.string.mainActivityRegisterEmptyFields, Toast.LENGTH_LONG).show();
                return;
            }
            // VERIFICA SE AS PASSWORDS SÃO IGUAIS
            if (!password.equals(confirmPassword)) {
                Toast.makeText(getApplicationContext(), R.string.mainActivityRegisterPasNotMatch, Toast.LENGTH_LONG).show();
                return;
            }
            if (!email.contains("@") || !email.contains(".")) {
                Toast.makeText(getApplicationContext(), R.string.mainActivityRegisterEmail, Toast.LENGTH_LONG).show();
                return;
            } else {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString(NOME, userName);
                editor.putString(EMAIL, email);
                editor.putString(PASSWORD, password);
                editor.commit();

                readInfoUser();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_home) {
        } else if (id == R.id.nav_notes) {
            Intent i = new Intent(this, DailyStudentActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_budget) {
            Intent i = new Intent(this, BudgetActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_statistics) {
            Intent i = new Intent(this, StatisticsActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_global_notes) {
            Intent i = new Intent(this, GlobalNotes.class);
            startActivity(i);
        } else if (id == R.id.nav_credits) {
            Intent i = new Intent(this, CreditsActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_sair) {
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void onSettingsAction(MenuItem item) {
        Intent modifySettings = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(modifySettings);
    }

    public void goNoteList(View view) {
        Intent i = new Intent(this, DailyStudentActivity.class);
        startActivity(i);
    }
}
