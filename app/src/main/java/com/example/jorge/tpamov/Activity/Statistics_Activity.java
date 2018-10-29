package com.example.jorge.tpamov.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jorge.tpamov.DataBase.BudgetDbAdapter;
import com.example.jorge.tpamov.Classes.classBudgetNote;
import com.example.jorge.tpamov.R;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Jorge on 19/12/2016.
 */
public class Statistics_Activity extends AppCompatActivity {
    WebView wvGrafico, wvGraficoGanhos;
    String strURL, strURLGanhos;
    List<Integer> diasDaSemana;
    Double totalGastos = 0.00, totalGanhos = 0.00, media = 0.00;

    List<Date> DateDiasDaSemana, diasDaSemanaNotas;
    List<Double> valorDiasDaSemanaGastos, valorDiasDaSemanaGanhos;
    List<classBudgetNote> notas;

    String mes;
    Calendar cal;
    DateFormat df;
    Date date = null;

    TextView tvTotalGastos, tvTotalGanhos, tvMedia;

    private BudgetDbAdapter mDbBudgetHelper;

    SharedPreferences sharedPreferences;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readPreferencesUser();
        setContentView(R.layout.statistics);

        setTitle(getString(R.string.statisticsTitle));

        df = new SimpleDateFormat("dd", Locale.FRENCH);

        //VARIÁVEIS AUXILIARES
        diasDaSemana = new ArrayList<>(); valorDiasDaSemanaGanhos = new ArrayList<>();
        valorDiasDaSemanaGastos = new ArrayList<>(); diasDaSemanaNotas = new ArrayList<>();
        DateDiasDaSemana = new ArrayList<>(); notas = new ArrayList<>();

        //INICIALIZA OS TEXTVIEWS
        tvTotalGanhos = (TextView) findViewById(R.id.tvTotalGanhos);
        tvTotalGastos = (TextView) findViewById(R.id.tvTotalGastos);
        tvMedia = (TextView) findViewById(R.id.tvMedia);

        mDbBudgetHelper = new BudgetDbAdapter(this);
        mDbBudgetHelper.open();

        notas = mDbBudgetHelper.fetchNoteForStatistics();

        cal = Calendar.getInstance();

        mes = new SimpleDateFormat("MMMM").format(cal.getTime()).toUpperCase();

        //COLOCA O CALENDARIO PARA SEGUNDA DESTA SEMANA
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        mDbBudgetHelper.close();
        try {

        //FORMATA OS DIAS DAS NOTAS
        for(int i = 0; i < notas.size(); i++)
            formatDate(notas.get(i).getDate());

        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        //FORMATA OS DIAS DA SEMANA ACTUAL
        formataDatas();

        File dir = getExternalFilesDir(null);
        if (dir == null || !dir.exists()) {
            Toast.makeText(getApplicationContext(), "Não tem SDCARD", Toast.LENGTH_SHORT).show();
            return;
        }

        //VERIFICA SE EXISTE DIAS DA SEMANA ACTUAL PARA A A LISTA DE NOTAS
        comparaDias();

        //try {
/*
        String strFich = dir.getAbsolutePath() + "/userStatistics.txt";
        Toast.makeText(getApplicationContext(), strFich, Toast.LENGTH_SHORT).show();
        FileOutputStream fos = null;
        fos = new FileOutputStream(strFich, true);
        PrintStream ps = new PrintStream(fos);
        String str = userName + " " + password + " " + email;
        ps.println(userName);ps.println(password);ps.println(email);
        fos.close();
        Toast.makeText(getApplicationContext(), "ES Escrevi: " + str, Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
*/

        strURL = "https://chart.googleapis.com/chart?" +
                "cht=lc&" + //define o tipo do gráfico "linha"
                "chxt=x,y&" + //imprime os valores dos eixos X, Y
                "chs=350x350&" + //define o tamanho da imagem
                "chd=t:"+valorDiasDaSemanaGastos.get(0)+","+valorDiasDaSemanaGastos.get(1)+","+valorDiasDaSemanaGastos.get(2)+","+valorDiasDaSemanaGastos.get(3)+","+valorDiasDaSemanaGastos.get(4)+","+valorDiasDaSemanaGastos.get(5)+","+valorDiasDaSemanaGastos.get(6)+"&" + //valor de cada coluna do gráfico
                "chl="+diasDaSemana.get(0)+"|"+diasDaSemana.get(1)+"|"+diasDaSemana.get(2)+"|"+diasDaSemana.get(3)+"|"+diasDaSemana.get(4)+"|"+diasDaSemana.get(5)+"|"+diasDaSemana.get(6)+"&" + //rótulo para cada coluna
                getString(R.string.Statisticsgastos) + //legenda do gráfico
                "chxr=1,0,50&" + //define o valor de início e fim do eixo
                "chds=0,50&" + //define o valor de escala dos dados
                "chg=0,5,0,0&" + //desenha linha horizontal na grade
                "chco=960000&" + //cor da linha do gráfico
                "chtt=" + mes.toUpperCase() +"&" + //cabeçalho do gráfico
                "chm=B,ff7a7a,0,0,0"; //fundo verde

        strURLGanhos = "https://chart.googleapis.com/chart?" +
                "cht=lc&" + //define o tipo do gráfico "linha"
                "chxt=x,y&" + //imprime os valores dos eixos X, Y
                "chs=350x350&" + //define o tamanho da imagem
                "chd=t:"+valorDiasDaSemanaGanhos.get(0)+","+valorDiasDaSemanaGanhos.get(1)+","+valorDiasDaSemanaGanhos.get(2)+","+valorDiasDaSemanaGanhos.get(3)+","+valorDiasDaSemanaGanhos.get(4)+","+valorDiasDaSemanaGanhos.get(5)+","+valorDiasDaSemanaGanhos.get(6)+"&" + //valor de cada coluna do gráfico
                "chl="+diasDaSemana.get(0)+"|"+diasDaSemana.get(1)+"|"+diasDaSemana.get(2)+"|"+diasDaSemana.get(3)+"|"+diasDaSemana.get(4)+"|"+diasDaSemana.get(5)+"|"+diasDaSemana.get(6)+"&" + //rótulo para cada coluna
                getString(R.string.Statisticsganhos) + //legenda do gráfico
                "chxr=1,0,50&" + //define o valor de início e fim do eixo
                "chds=0,50&" + //define o valor de escala dos dados
                "chg=0,5,0,0&" + //desenha linha horizontal na grade
                "chco=24b700&" + //cor da linha do gráfico
                "chtt=" + mes.toUpperCase() +"&" + //cabeçalho do gráfico
                "chm=B,bdffad,0,0,0"; //fundo verde

        wvGrafico = (WebView)findViewById(R.id.wvGrafico);
        wvGrafico.loadUrl(strURL);

        wvGraficoGanhos = (WebView)findViewById(R.id.wvGraficoGanhos);
        wvGraficoGanhos.loadUrl(strURLGanhos);

    }

    private boolean readPreferencesUser(){
        int textSize = -1;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Statistics_Activity.this);

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

    private void comparaDias() {
        int numeroDeDiasUtilizados = 0;
        for(int i = 0; i < DateDiasDaSemana.size(); i++) {
            Double gastos = 0.00;
            Double ganhos = 0.00;
            for (int j = 0; j < diasDaSemanaNotas.size(); j++) {
                if(DateDiasDaSemana.get(i).equals(diasDaSemanaNotas.get(j))){
                    if(notas.get(j).getGender().equals(getString(R.string.despesa)))
                        gastos += notas.get(j).getValue();
                    else
                        ganhos += notas.get(j).getValue();
                    numeroDeDiasUtilizados++;
                }

            }
            valorDiasDaSemanaGastos.add(gastos);
            totalGastos += gastos;
            valorDiasDaSemanaGanhos.add(ganhos);
            totalGanhos += ganhos;


        }
        for(int i = 0; i < DateDiasDaSemana.size(); i++) {
            System.out.println(valorDiasDaSemanaGanhos.get(i));
            System.out.println(valorDiasDaSemanaGastos.get(i));
        }

        media = (totalGanhos-totalGastos)/numeroDeDiasUtilizados;
        tvTotalGastos.setText(totalGastos.toString() + getString(R.string.budgetActivityMoney));
        tvTotalGanhos.setText(totalGanhos.toString() + getString(R.string.budgetActivityMoney));
        tvMedia.setText(media.toString() + getString(R.string.budgetActivityMoney));

    }

    private void formataDatas(){
        try {
            for(int i = 0; i < 7; i++){
                SimpleDateFormat newFormat = new SimpleDateFormat("dd-MM-yyyy");
                String finalString = newFormat.format(cal.getTime());
                DateDiasDaSemana.add((Date) newFormat.parse(finalString));

                diasDaSemana.add(Integer.valueOf(df.format(cal.getTime())));
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

        } catch (ParseException e1) {
            e1.printStackTrace();
        }
    }

    public void formatDate(String dateFromNote) throws ParseException {
        try {
            DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy - h:mm a");
            date = (Date) formatter.parse(dateFromNote);
            SimpleDateFormat newFormat = new SimpleDateFormat("dd-MM-yyyy");
            String finalString = newFormat.format(date);
            date = (Date) newFormat.parse(finalString);
            System.out.println(date.getDate());
            diasDaSemanaNotas.add(date);

        }catch(ParseException ex){
            ex.printStackTrace();
        }
    }
}
