package com.tusher.boiwatch.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tusher.boiwatch.R;
import com.tusher.boiwatch.activity.AudiobookPlayerActivity;
import com.tusher.boiwatch.adapter.AudiobookListAdapter;
import com.tusher.boiwatch.api.YouTubeAudiobookService;
import com.tusher.boiwatch.models.Audiobook;

import java.util.ArrayList;
import java.util.List;

public class AudiobookSearchBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_QUERY = "search_query";

    private String query;
    private TextView tvQuery, tvNoResults;
    private ImageView ivClose;
    private ProgressBar progressBar;
    private RecyclerView rvResults;
    private View llNoResults;

    private AudiobookListAdapter adapter;
    private List<Audiobook> searchResults = new ArrayList<>();
    private OnAudiobookSelectedListener listener;

    public interface OnAudiobookSelectedListener {
        void onAudiobookSelected(Audiobook audiobook);
    }

    public static AudiobookSearchBottomSheet newInstance(String query) {
        AudiobookSearchBottomSheet fragment = new AudiobookSearchBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_QUERY, query);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnAudiobookSelectedListener(OnAudiobookSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            query = getArguments().getString(ARG_QUERY);
        }
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audiobook_search_bottom_sheet, container, false);
        initViews(view);
        performSearch();
        return view;
    }

    private void initViews(View view) {
        tvQuery = view.findViewById(R.id.tv_search_query);
        ivClose = view.findViewById(R.id.iv_close_sheet);
        progressBar = view.findViewById(R.id.progress_search);
        rvResults = view.findViewById(R.id.rv_search_results);
        llNoResults = view.findViewById(R.id.ll_no_results);
        tvNoResults = view.findViewById(R.id.tv_no_results);

        tvQuery.setText("Results for \"" + query + "\"");

        ivClose.setOnClickListener(v -> dismiss());

        adapter = new AudiobookListAdapter(requireContext(), searchResults, audiobook -> {
            if (listener != null) {
                listener.onAudiobookSelected(audiobook);
            } else {
                // Fallback direct intent launch if listener is not set
                Intent intent = new Intent(requireContext(), AudiobookPlayerActivity.class);
                intent.putExtra("audiobook", audiobook);
                startActivity(intent);
            }
            dismiss();
        });
        rvResults.setAdapter(adapter);
    }

    private void performSearch() {
        progressBar.setVisibility(View.VISIBLE);
        rvResults.setVisibility(View.GONE);
        llNoResults.setVisibility(View.GONE);

        YouTubeAudiobookService.getInstance().searchAudiobooks(query, new YouTubeAudiobookService.SearchCallback() {
            @Override
            public void onSuccess(List<Audiobook> results) {
                if (getActivity() == null || !isAdded()) return;

                progressBar.setVisibility(View.GONE);
                searchResults.clear();
                
                if (results == null || results.isEmpty()) {
                    llNoResults.setVisibility(View.VISIBLE);
                } else {
                    searchResults.addAll(results);
                    rvResults.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String message) {
                if (getActivity() == null || !isAdded()) return;

                progressBar.setVisibility(View.GONE);
                llNoResults.setVisibility(View.VISIBLE);
                tvNoResults.setText("Error: " + message);
            }
        });
    }
}
