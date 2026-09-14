package org.example.models;

import java.util.List;

/**
 * Helps frontend render a single page of results
 */
public class ResultsPage<T> {

    private List<T> items;
    private int totalCount;

    /**
     * constructor for the page
     * @param items list of items on the page
     * @param totalCount total count of the items
     */
    public ResultsPage(List<T> items, int totalCount) {
        this.items = items;
        this.totalCount = totalCount;
    }

    /**
     * get the list of items on the page
     * @return the list of items
     */
    public List<T> getItems() {
        return items;
    }

    /**
     * set the list of items on the page
     * @param items the list of items
     */
    public void setItems(List<T> items) {
        this.items = items;
    }

    /**
     * get the total count of items on the page
     * @return the total count of items
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * set the total count of items on the page
     * @param totalCount the total count of items
     */
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
