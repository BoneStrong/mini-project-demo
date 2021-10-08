package com.dzz.graphql.interf;

import java.util.List;

public class PageableData<T> {
    private PageInfo pageInfo;
    private List<T> records;

    public PageableData() {
    }

    public PageableData(PageInfo pageInfo, List<T> records) {
        this.pageInfo = pageInfo;
        this.records = records;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public  static <T> PageableDataBuilder<T> builder() {
        return new PageableDataBuilder<>();
    }

    public static class PageableDataBuilder<T> {
        private PageInfo pageInfo;
        private List<T> records;

        private PageableDataBuilder() {
        }

        public PageableDataBuilder<T> pageInfo(final PageInfo pageInfo) {
            this.pageInfo = pageInfo;
            return this;
        }

        public PageableDataBuilder<T> records(final List<T> records) {
            this.records = records;
            return this;
        }

        public PageableData<T> build() {
            return new PageableData<>(this.pageInfo, this.records);
        }
    }
}