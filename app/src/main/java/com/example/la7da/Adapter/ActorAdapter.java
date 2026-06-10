package com.example.la7da.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.la7da.Domain.ActorItem;
import com.example.la7da.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class ActorAdapter extends RecyclerView.Adapter<ActorAdapter.ViewHolder> {

    private Context context;
    private List<ActorItem> actors;

    public ActorAdapter(Context context, List<ActorItem> actors) {
        this.context = context;
        this.actors = actors;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewholder_actor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActorItem actor = actors.get(position);
        holder.actorName.setText(actor.getName());
        holder.actorRole.setText(actor.getCharacter());

        if (!actor.getProfilePath().isEmpty()) {
            Glide.with(context)
                    .load("https://image.tmdb.org/t/p/w185" + actor.getProfilePath())
                    .placeholder(R.drawable.ic_person)
                    .into(holder.actorImage);
        } else {
            holder.actorImage.setImageResource(R.drawable.ic_person);
        }
    }

    @Override
    public int getItemCount() {
        return actors != null ? actors.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView actorImage;
        TextView actorName, actorRole;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            actorImage = itemView.findViewById(R.id.actorImage);
            actorName = itemView.findViewById(R.id.actorName);
            actorRole = itemView.findViewById(R.id.actorRole);
        }
    }
}