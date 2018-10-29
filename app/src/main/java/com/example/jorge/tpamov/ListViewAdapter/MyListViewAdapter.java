package com.example.jorge.tpamov.ListViewAdapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jorge.tpamov.DataBase.BudgetDbAdapter;
import com.example.jorge.tpamov.DataBase.WalletDbAdapter;
import com.example.jorge.tpamov.R;

/**
 * Created by Jorge on 23/12/2016.
 */

public class MyListViewAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] title;
    private final String[] desc;
    private final String[] value;
    private final Integer[] imageId;
    private BudgetDbAdapter mDbBudgetHelper;

    public MyListViewAdapter(Activity context,
                             String[] title) {
        super(context, R.layout.notesbudget_row, title);
        this.context = context;

        mDbBudgetHelper = new BudgetDbAdapter(context);

        mDbBudgetHelper.open();

        this.title = title;
        this.imageId = mDbBudgetHelper.fetchAllNotesGenderImage();
        this.desc = mDbBudgetHelper.fetchNotesByParameter("observations");
        this.value = mDbBudgetHelper.fetchNotesByParameter("value");

        mDbBudgetHelper.close();
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.notesbudget_row, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.tvTitulo);
        TextView tvDate = (TextView) rowView.findViewById(R.id.tvDate);
        TextView tvBody = (TextView) rowView.findViewById(R.id.tvBody);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imgGender);
        txtTitle.setText(title[position]);
        tvBody.setText(desc[position]);
        tvDate.setText(value[position]);
        imageView.setImageResource(imageId[position]);
        return rowView;
    }
}
