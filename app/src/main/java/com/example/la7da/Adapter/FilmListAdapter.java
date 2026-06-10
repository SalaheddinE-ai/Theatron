package com.example.la7da.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.la7da.Activity.DetailActivity;
import com.example.la7da.Domain.FilmItem;
import com.example.la7da.R;
import java.util.ArrayList;
import java.util.List;

public class FilmListAdapter extends RecyclerView.Adapter<FilmListAdapter.ViewHolder> {
    private List<FilmItem> items;
    private Context context;

    public FilmListAdapter(Context context, List<FilmItem> items) {
        this.context = context;
        this.items = items != null ? items : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_film, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FilmItem item = items.get(position);
        holder.titleTxt.setText(item.getTitle());
        holder.scoreTxt.setText(String.valueOf(item.getVoteAverage()));
        Glide.with(context)
                .load("https://image.tmdb.org/t/p/w500" + item.getPosterPath())
                .placeholder(R.drawable.back)
                .into(holder.pic);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("movieId", item.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<FilmItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView pic;
        TextView titleTxt, scoreTxt;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pic = itemView.findViewById(R.id.pic);
            titleTxt = itemView.findViewById(R.id.titleTxt);
            scoreTxt = itemView.findViewById(R.id.scoreTxt);
        }
    }
}