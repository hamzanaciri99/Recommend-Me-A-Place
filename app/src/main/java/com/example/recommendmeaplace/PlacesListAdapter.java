package com.example.recommendmeaplace;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class PlacesListAdapter extends RecyclerView.Adapter<PlacesListAdapter.PlacesViewHolder> {
    private ArrayList<MyPlace> myPlaces;
    private Context context;

    public class PlacesViewHolder extends RecyclerView.ViewHolder {
        public TextView name, address;
        public RatingBar ratingBar;
        Drawable selected, unselected;

        public PlacesViewHolder(View v) {
            super(v);
            selected = v.getResources().getDrawable(R.drawable.selected_item);
            unselected = v.getResources().getDrawable(R.drawable.unselected_item);
            name = v.findViewById(R.id.name);
            address = v.findViewById(R.id.address);
            ratingBar = v.findViewById(R.id.ratingBar3);
        }
    }

    public PlacesListAdapter(Context context, ArrayList<MyPlace> myPlaces) {
        this.myPlaces = myPlaces;
        this.context = context;
    }

    @Override
    public PlacesListAdapter.PlacesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v =
                (View)
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.list_item, parent, false);
        PlacesViewHolder vh = new PlacesViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final PlacesViewHolder holder, final int position) {
        holder.itemView.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            v.setBackground(holder.selected);
                        } else {
                            v.setBackground(holder.unselected);
                        }
                        return false;
                    }
                });

        holder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, PlaceItemActivity.class);
                        intent.putExtra("id", myPlaces.get(position).getId());
                        context.startActivity(intent);
                    }
                });

        holder.name.setText(myPlaces.get(position).getName());
        holder.address.setText(myPlaces.get(position).getAddress());
        holder.ratingBar.setRating(Float.parseFloat(myPlaces.get(position).getRating() + ""));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return myPlaces.size();
    }
}
