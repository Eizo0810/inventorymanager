package com.example.inventorymanager.dto;

import java.util.List;

public class PageResult<T> {

    private final List<T> items;
    private final int page;
    private final int size;
    private final int totalCount;
    private final int totalPages;

    public PageResult(List<T> items, int page, int size, int totalCount) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.totalCount = totalCount;
        this.totalPages = totalCount == 0
                ? 1
                : (int) Math.ceil((double) totalCount / size);
    }

    public List<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isFirst() {
        return page <= 1;
    }

    public boolean isLast() {
        return page >= totalPages;
    }

    public int getPreviousPage() {
        return Math.max(1, page - 1);
    }

    public int getNextPage() {
        return Math.min(totalPages, page + 1);
    }
}
