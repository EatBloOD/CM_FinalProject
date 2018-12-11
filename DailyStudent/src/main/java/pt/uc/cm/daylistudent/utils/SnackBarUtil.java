package pt.uc.cm.daylistudent.utils;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;


public class SnackBarUtil {
    public SnackBarUtil() {
    }

    public void showSnackBar(View v, int resID, boolean error) {
        Snackbar snackbar  = Snackbar.make(v, resID, Snackbar.LENGTH_LONG);
        TextView snackbarTextView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setTypeface(snackbarTextView.getTypeface(), Typeface.BOLD);

        if(error) {
            snackbar.getView().setBackgroundColor(Color.RED);
            snackbarTextView.setTextColor(Color.WHITE);
            snackbar.show();
        }
        else{
            snackbar.getView().setBackgroundColor(Color.GREEN);
            snackbarTextView.setTextColor(Color.BLACK);
            snackbar.show();
        }
    }

    public void showSnackBarStr(View v, String resID, boolean error) {
        Snackbar snackbar  = Snackbar.make(v, resID, Snackbar.LENGTH_LONG);
        TextView snackbarTextView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setTypeface(snackbarTextView.getTypeface(), Typeface.BOLD);

        if(error) {
            snackbar.getView().setBackgroundColor(Color.RED);
            snackbarTextView.setTextColor(Color.WHITE);
            snackbar.show();
        }
        else{
            snackbar.getView().setBackgroundColor(Color.GREEN);
            snackbarTextView.setTextColor(Color.BLACK);
            snackbar.show();
        }
    }
}
