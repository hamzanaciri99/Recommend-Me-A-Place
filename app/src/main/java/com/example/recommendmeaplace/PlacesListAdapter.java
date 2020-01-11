package com.example.recommendmeaplace;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PlacesListAdapter extends RecyclerView.Adapter<PlacesListAdapter.PlacesViewHolder> {
    private ArrayList<MyPlace> myPlaces;
    private Activity context;

    public class PlacesViewHolder extends RecyclerView.ViewHolder {
        public TextView name , address, code;
        Drawable selected, unselected;

        public PlacesViewHolder(View v) {
            super(v);
            selected = v.getResources().getDrawable( R.drawable.selected_item);
            unselected = v.getResources().getDrawable( R.drawable.unselected_item);
            name = v.findViewById(R.id.name);
            code = v.findViewById(R.id.code);
            address = v.findViewById(R.id.address);
        }
    }

    public PlacesListAdapter(ArrayList<MyPlace> myPlaces) {
        this.myPlaces = myPlaces;
    }

    public PlacesListAdapter(Activity context, ArrayList<MyPlace> myPlaces) {
        this.myPlaces = myPlaces;
        this.context = context;
    }

    @Override
    public PlacesListAdapter.PlacesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        PlacesViewHolder vh = new PlacesViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final PlacesViewHolder holder, final int position) {
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    v.setBackground(holder.selected);
                }
                else {
                    v.setBackground(holder.unselected);
                }
                return false;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra("Lat", myPlaces.get(position).getLat());
                intent.putExtra("Lng", myPlaces.get(position).getLng());
                intent.putExtra("Title", myPlaces.get(position).getName());
                context.startActivityForResult(intent, 1);
            }
        });

        holder.code.setBackground(holder.itemView.getResources().getDrawable(getBgColor(myPlaces.get(position).getCcode())));
        holder.code.setText(myPlaces.get(position).getCcode());
        holder.name.setText(myPlaces.get(position).getName());
        holder.address.setText(myPlaces.get(position).getAddress());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return myPlaces.size();
    }

    int getBgColor(String ccode) {
        int res = R.drawable.ccode_aa;
        switch (ccode.substring(0, 1)) {
            case "A": res = R.drawable.ccode_aa;break;
            case "B": res = R.drawable.ccode_bb;break;
            case "C": res = R.drawable.ccode_cc;break;
            case "D": res = R.drawable.ccode_dd;break;
            case "E": res = R.drawable.ccode_ee;break;
            case "F": res = R.drawable.ccode_ff;break;
            case "G": res = R.drawable.ccode_gg;break;
            case "H": res = R.drawable.ccode_hh;break;
            case "I": res = R.drawable.ccode_ii;break;
            case "J": res = R.drawable.ccode_jj;break;
            case "K": res = R.drawable.ccode_kk;break;
            case "L": res = R.drawable.ccode_ll;break;
            case "M": res = R.drawable.ccode_mm;break;
            case "N": res = R.drawable.ccode_nn;break;
            case "O": res = R.drawable.ccode_oo;break;
            case "P": res = R.drawable.ccode_pp;break;
            case "Q": res = R.drawable.ccode_qq;break;
            case "R": res = R.drawable.ccode_rr;break;
            case "S": res = R.drawable.ccode_ss;break;
            case "T": res = R.drawable.ccode_tt;break;
            case "U": res = R.drawable.ccode_uu;break;
            case "V": res = R.drawable.ccode_vv;break;
            case "W": res = R.drawable.ccode_ww;break;
            case "X": res = R.drawable.ccode_xx;break;
            case "Y": res = R.drawable.ccode_yy;break;
            case "Z": res = R.drawable.ccode_zz;break;
        }
        return res;
    }


}
