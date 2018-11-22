package pt.uc.cm.daylistudent.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import pt.uc.cm.daylistudent.R;
import pt.uc.cm.daylistudent.models.BudgetNote;

public class ArrayWalletAdapter extends ArrayAdapter<BudgetNote> {

    public ArrayWalletAdapter(Context context, ArrayList<BudgetNote> budgetNotes) {
        super(context, 0, budgetNotes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BudgetNote bgNote = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.budget_wallet_row, parent, false);
        }
        // Lookup view for data population
        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        TextView tvValue = convertView.findViewById(R.id.tvValue);
        // Populate the data into the template view using the data object
        tvTitle.setText(bgNote.getTitle().toString());
        tvValue.setText(bgNote.getValue().toString());
        // Return the completed view to render on screen
        return convertView;
    }
}
