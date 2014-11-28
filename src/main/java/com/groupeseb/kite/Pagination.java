package com.groupeseb.kite;

import lombok.Data;

@Data
public class Pagination {
    protected String totalPagesField;

    protected String pageParameterName;
    protected String sizeParameterName;

    protected Integer size;
    protected Integer startPage;

    public Pagination(Json specification) {
        totalPagesField = specification.getString("totalPagesField");

        pageParameterName = specification.getStringOrDefault("pageParameterName", "page");
        sizeParameterName = specification.getStringOrDefault("sizeParameterName", "size");

        size = specification.getIntegerOrDefault("size", 20);
        startPage = specification.getIntegerOrDefault("startPage", 1);
    }
}
