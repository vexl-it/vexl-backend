package com.cleevio.vexl.common.dto;

import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Data
public class PaginatedResponse<T> {

    private final String nextLink;
    private final String prevLink;
    private int currentPage;
    private int currentPageSize;
    private int pagesTotal;
    private int itemsCount;
    private Long itemsCountTotal;
    private List<T> items;

    public PaginatedResponse(HttpServletRequest request, Page<T> page) {
        this.currentPage = page.getPageable().isPaged() ? page.getPageable().getPageNumber() : 0;
        this.currentPageSize = page.getPageable().isPaged() ? page.getPageable().getPageSize() : 0;
        this.itemsCount = page.getNumberOfElements();
        this.itemsCountTotal = page.getTotalElements();
        this.pagesTotal = page.getTotalPages();

        final String requestURI = request.getRequestURL().toString();

        this.nextLink = this.currentPage + 2 > this.pagesTotal ? null : UriComponentsBuilder.fromUriString(requestURI)
                .query(request.getQueryString())
                .replaceQueryParam("page", this.currentPage + 1)
                .toUriString();

        this.prevLink = this.currentPage <= 0 ? null : UriComponentsBuilder.fromUriString(requestURI)
                .query(request.getQueryString())
                .replaceQueryParam("page", this.currentPage - 1)
                .toUriString();

        this.items = page.getContent();
    }
}
