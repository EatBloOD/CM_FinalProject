package pt.uc.cm.daylistudent.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;


public class SnackBarUtil {

    public static void showSnackBar(View v, Context context, int resID, boolean error) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

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

    public static void showSnackBarStr(View v, Context context, String resID, boolean error) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

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
