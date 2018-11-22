package pt.uc.cm.daylistudent.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pt.uc.cm.daylistudent.R;
import pt.uc.cm.daylistudent.adapters.BudgetDbAdapter;
import pt.uc.cm.daylistudent.models.BudgetNote;
import pt.uc.cm.daylistudent.utils.SharedPreferencesUtils;

public class StatisticsActivity extends AppCompatActivity {

    private final String TAG = StatisticsActivity.class.getSimpleName();

    List<Integer> diasDaSemana;
    Double totalGastos = 0.00, totalGanhos = 0.00, media = 0.00;

    List<Date> DateDiasDaSemana, diasDaSemanaNotas;
    List<Double> valorDiasDaSemanaGastos, valorDiasDaSemanaGanhos;
    List<BudgetNote> notas;

    String mes;
    Calendar cal;
    DateFormat df;
    Date date = null;

    TextView tvTotalGastos, tvTotalGanhos, tvMedia;

    private BudgetDbAdapter mDbBudgetHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferencesUtils.INSTANCE.readPreferencesUser(getApplicationContext());
        setContentView(R.layout.statistics);

        setTitle(getString(R.string.statisticsTitle));

        df = new SimpleDateFormat("dd", Locale.FRENCH);

        //VARIÁVEIS AUXILIARES
        diasDaSemana = new ArrayList<>();
        valorDiasDaSemanaGanhos = new ArrayList<>();
        valorDiasDaSemanaGastos = new ArrayList<>();
        diasDaSemanaNotas = new ArrayList<>();
        DateDiasDaSemana = new ArrayList<>();
        notas = new ArrayList<>();

        //INICIALIZA OS TEXTVIEWS
        tvTotalGanhos = findViewById(R.id.tvTotalGanhos);
        tvTotalGastos = findViewById(R.id.tvTotalGastos);
        tvMedia = findViewById(R.id.tvMedia);

        mDbBudgetHelper = new BudgetDbAdapter(this);
        mDbBudgetHelper.open();

        notas = mDbBudgetHelper.fetchNoteForStatistics();

        cal = Calendar.getInstance();

        mes = new SimpleDateFormat("MMMM").format(cal.getTime()).toUpperCase();

        //COLOCA O CALENDARIO PARA SEGUNDA DESTA SEMANA
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        mDbBudgetHelper.close();

        //FORMATA OS DIAS DAS NOTAS
        for (int i = 0; i < notas.size(); i++)
            formatDate(notas.get(i).getDate());

        //FORMATA OS DIAS DA SEMANA ACTUAL
        formatDates();

        File dir = getExternalFilesDir(null);
        if (dir == null || !dir.exists()) {
            Toast.makeText(getApplicationContext(), "Não tem SDCARD", Toast.LENGTH_SHORT).show();
            return;
        }

        //VERIFICA SE EXISTE DIAS DA SEMANA ACTUAL PARA A A LISTA DE NOTAS
        compareDays();

        DataPoint[] arrayGains = new DataPoint[7];
        DataPoint[] arrayExpenses = new DataPoint[7];
        for (int i = 0; i < valorDiasDaSemanaGanhos.size(); i++) {
            arrayGains[i] = new DataPoint(Integer.valueOf(diasDaSemana.get(i)), (int) valorDiasDaSemanaGanhos.get(i).doubleValue());
            arrayExpenses[i] = new DataPoint(Integer.valueOf(diasDaSemana.get(i)), (int) valorDiasDaSemanaGastos.get(i).doubleValue());
        }

        //Gains
        GraphView graph = findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(arrayGains);
        // styling series
        series.setColor(Color.GREEN);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
        series.setThickness(8);
        graph.addSeries(series);

        //Expenses
        GraphView graphExpenses = findViewById(R.id.graphExpenses);
        LineGraphSeries<DataPoint> seriesExpenses = new LineGraphSeries<>(arrayExpenses);
        // styling series
        seriesExpenses.setColor(Color.RED);
        seriesExpenses.setDrawDataPoints(true);
        seriesExpenses.setDataPointsRadius(10);
        seriesExpenses.setThickness(8);
        graphExpenses.addSeries(seriesExpenses);

    }

    private void compareDays() {
        int nDaysUsed = 0;
        for (int i = 0; i < DateDiasDaSemana.size(); i++) {
            Double spence = 0.00;
            Double profit = 0.00;
            for (int j = 0; j < diasDaSemanaNotas.size(); j++) {
                if (DateDiasDaSemana.get(i).equals(diasDaSemanaNotas.get(j))) {
                    if (notas.get(j).getGender().equals(getString(R.string.despesa)))
                        spence += notas.get(j).getValue();
                    else
                        profit += notas.get(j).getValue();
                    nDaysUsed++;
                }

            }
            valorDiasDaSemanaGastos.add(spence);
            totalGastos += spence;
            valorDiasDaSemanaGanhos.add(profit);
            totalGanhos += profit;
        }

        for (int i = 0; i < DateDiasDaSemana.size(); i++) {
            System.out.println(valorDiasDaSemanaGanhos.get(i));
            System.out.println(valorDiasDaSemanaGastos.get(i));
        }

        media = (totalGanhos - totalGastos) / nDaysUsed;
        tvTotalGastos.setText(totalGastos.toString() + getString(R.string.budgetActivityMoney));
        tvTotalGanhos.setText(totalGanhos.toString() + getString(R.string.budgetActivityMoney));
        tvMedia.setText(media.toString() + getString(R.string.budgetActivityMoney));
    }

    private void formatDates() {
        try {
            for (int i = 0; i < 7; i++) {
                SimpleDateFormat newFormat = new SimpleDateFormat("dd-MM-yyyy");
                String finalString = newFormat.format(cal.getTime());
                DateDiasDaSemana.add((Date) newFormat.parse(finalString));

                diasDaSemana.add(Integer.valueOf(df.format(cal.getTime())));
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }

    public void formatDate(String dateFromNote) {
        try {
            DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy - h:mm a");
            date = formatter.parse(dateFromNote);
            SimpleDateFormat newFormat = new SimpleDateFormat("dd-MM-yyyy");
            String finalString = newFormat.format(date);
            date = newFormat.parse(finalString);
            System.out.println(date.getDate());
            diasDaSemanaNotas.add(date);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }
}
