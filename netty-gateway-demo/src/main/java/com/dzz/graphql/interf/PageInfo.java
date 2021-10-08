package com.dzz.graphql.interf;

public class PageInfo {
    private Integer pageSize;
    private Integer currentPage;
    private Long totalRecords;
    private Integer totalPages;

    public PageInfo() {
    }

    PageInfo(final Integer pageSize, final Integer currentPage, final Long totalRecords, final Integer totalPages) {
        this.pageSize = pageSize;
        this.currentPage = currentPage;
        this.totalRecords = totalRecords;
        this.totalPages = totalPages;
    }

    public static PageInfoBuilder builder() {
        return new PageInfoBuilder();
    }

    public Integer getPageSize() {
        return this.pageSize;
    }

    public Integer getCurrentPage() {
        return this.currentPage;
    }

    public Long getTotalRecords() {
        return this.totalRecords;
    }

    public Integer getTotalPages() {
        return this.totalPages;
    }

    public void setPageSize(final Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setCurrentPage(final Integer currentPage) {
        this.currentPage = currentPage;
    }

    public void setTotalRecords(final Long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public void setTotalPages(final Integer totalPages) {
        this.totalPages = totalPages;
    }

    public static class PageInfoBuilder {
        private Integer pageSize;
        private Integer pageNumber;
        private Long totalRecords;
        private Integer totalPages;

        private PageInfoBuilder() {
        }

        public PageInfoBuilder pageSize(final Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public PageInfoBuilder pageNumber(final Integer pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public PageInfoBuilder totalElements(final Long totalElements) {
            this.totalRecords = totalElements;
            return this;
        }

        public PageInfoBuilder totalPages(final Integer totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public PageInfo build() {
            return new PageInfo(this.pageSize, this.pageNumber, this.totalRecords, this.totalPages);
        }
    }
}
