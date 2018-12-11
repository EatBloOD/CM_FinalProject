package pt.uc.cm.daylistudent.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import pt.uc.cm.daylistudent.R;

public class MySpinnerAdapter extends ArrayAdapter {
    private LayoutInflater inflater;
    private String array[];
    private Integer images[];

    public MySpinnerAdapter(Context context, int textViewResourceId, String[] objects,
                            Integer[] images, LayoutInflater inflater) {
        super(context, textViewResourceId, objects);
        this.inflater = inflater;
        this.array = objects;
        this.images = images;
    }

    private View getCustomView(int position, ViewGroup parent) {
        // Inflating the layout for the custom Spinner
        View layout = inflater.inflate(R.layout.spinner_row, parent, false);

        // Declaring and Typecasting the textview in the inflated layout
        TextView tvLanguage = layout.findViewById(R.id.tvTextSpinner);

        // Setting the text using the array
        tvLanguage.setText(array[position]);

        // Setting the color of the text
        tvLanguage.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));

        // Declaring and Typecasting the imageView in the inflated layout
        ImageView img = layout.findViewById(R.id.imgSpinner);

        // Setting an image using the id's in the array
        img.setImageResource(images[position]);

        // Setting Special atrributes for 1st element
        if (position == 0) {
            // Removing the image view
            img.setVisibility(View.GONE);
            // Setting the size of the text
            //tvLanguage.setTextSize(20f);
            // Setting the text Color
            tvLanguage.setTextColor(Color.BLACK);
        }

        return layout;
    }

    // It gets a View that displays in the drop down popup the data at the specified position
    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, parent);
    }

    // It gets a View that displays the data at the specified position
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, parent);
    }
}
