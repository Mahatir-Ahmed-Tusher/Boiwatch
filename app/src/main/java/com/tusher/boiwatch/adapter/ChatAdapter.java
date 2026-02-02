package com.tusher.boiwatch.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import com.tusher.boiwatch.R;
import com.tusher.boiwatch.activity.MovieDetailActivity;
import com.tusher.boiwatch.models.ChatMessage;
import com.tusher.boiwatch.models.Movie;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<ChatMessage> messages;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ChatMessage.TYPE_USER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_ai, parent, false);
            return new AIViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).tvMessage.setText(message.getText());
        } else {
            AIViewHolder aiHolder = (AIViewHolder) holder;
            aiHolder.tvMessage.setText(message.getText());
            if (message.getSuggestions() != null && !message.getSuggestions().isEmpty()) {
                aiHolder.rvSuggestions.setVisibility(View.VISIBLE);
                aiHolder.rvSuggestions.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                aiHolder.rvSuggestions.setAdapter(new SuggestionAdapter(context, message.getSuggestions()));
            } else {
                aiHolder.rvSuggestions.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        UserViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message_user);
        }
    }

    static class AIViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        RecyclerView rvSuggestions;
        AIViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message_ai);
            rvSuggestions = itemView.findViewById(R.id.rv_movie_suggestions);
        }
    }

    static class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {
        Context context;
        List<Movie> movies;

        SuggestionAdapter(Context context, List<Movie> movies) {
            this.context = context;
            this.movies = movies;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_suggestion_movie, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Movie movie = movies.get(position);
            holder.tvTitle.setText(movie.getTitle());
            holder.tvRating.setText("⭐ " + movie.getVoteAverage());
            Picasso.get().load("https://image.tmdb.org/t/p/w500" + movie.getPosterPath()).into(holder.ivPoster);
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, MovieDetailActivity.class);
                intent.putExtra("movie", movie);
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return movies.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivPoster;
            TextView tvTitle, tvRating;
            ViewHolder(View itemView) {
                super(itemView);
                ivPoster = itemView.findViewById(R.id.iv_suggestion_poster);
                tvTitle = itemView.findViewById(R.id.tv_suggestion_title);
                tvRating = itemView.findViewById(R.id.tv_suggestion_rating);
            }
        }
    }
}
