package org.example.models;

import java.util.List;

/**
 * Helps frontend render a single page of results
 */
public class ResultsPage<T> {

    private List<T> items;
    private int totalCount;

    public ResultsPage(List<T> items, int totalCount) {
        this.items = items;
        this.totalCount = totalCount;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
