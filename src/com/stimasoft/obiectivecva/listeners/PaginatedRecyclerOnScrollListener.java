package com.stimasoft.obiectivecva.listeners;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Pagination for the objectives list
 */
public class PaginatedRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

    public interface RecyclerEndListener {
        // you can define any parameter as per your requirement
        void loadMoreObjectives(int page);
    }

    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean loading = true; // True if we are still waiting for the last set of data to load.
    private int visibleThreshold = 5; // The minimum amount of items to have below your current scroll position before loading more.
    private int firstVisibleItem;
    private int visibleItemCount;
    private int totalItemCount;

    private int current_page = 0;
    private RecyclerEndListener recyclerEndListener;

    private LinearLayoutManager mLinearLayoutManager;

    public PaginatedRecyclerOnScrollListener(Context context, LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
        AppCompatActivity activity = (AppCompatActivity) context;
        recyclerEndListener = (RecyclerEndListener) activity;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLinearLayoutManager.getItemCount();
        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }
        if (!loading && (totalItemCount - visibleItemCount)
                <= (firstVisibleItem + visibleThreshold)) {

            current_page++;

            recyclerEndListener.loadMoreObjectives(current_page);

            loading = true;
        }
    }

}