package com.qa.ui.model;

import java.util.Comparator;

/** Sorting options of the Products page with the order each one must produce. */
public enum SortOption {

    NAME_ASC("az", "Name (A to Z)", Comparator.comparing(Product::name)),
    NAME_DESC("za", "Name (Z to A)", Comparator.comparing(Product::name).reversed()),
    PRICE_ASC("lohi", "Price (low to high)", Comparator.comparing(Product::price)),
    PRICE_DESC("hilo", "Price (high to low)", Comparator.comparing(Product::price).reversed());

    private final String value;
    private final String label;
    private final Comparator<Product> expectedOrder;

    SortOption(String value, String label, Comparator<Product> expectedOrder) {
        this.value = value;
        this.label = label;
        this.expectedOrder = expectedOrder;
    }

    public String value() {
        return value;
    }

    public String label() {
        return label;
    }

    public Comparator<Product> expectedOrder() {
        return expectedOrder;
    }
}
