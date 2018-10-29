package com.example.jorge.tpamov.ListViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import com.example.jorge.tpamov.Classes.classBudgetNote;
import com.example.jorge.tpamov.R;

import java.util.ArrayList;

/**
 * Created by Jorge on 19/12/2016.
 */
public class ArrayWalletAdapter extends ArrayAdapter<classBudgetNote> {
    public ArrayWalletAdapter(Context context, ArrayList<classBudgetNote> budgNotes) {
        super(context, 0, budgNotes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        classBudgetNote bgNote = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.budget_wallet_row, parent, false);
        }
        // Lookup view for data population
        TextView tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
        TextView tvValue = (TextView) convertView.findViewById(R.id.tvValue);
        // Populate the data into the template view using the data object
        tvTitle.setText(bgNote.getTitle().toString());
        tvValue.setText(bgNote.getValue().toString());
        // Return the completed view to render on screen
        return convertView;
    }
}