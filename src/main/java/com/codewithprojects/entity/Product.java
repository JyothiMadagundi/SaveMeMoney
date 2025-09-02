package com.codewithprojects.entity;

import lombok.Data;

@Data
public class Product {
    private String name;
    private String price;
    private String imageUrl;
    private String hyperlink;
    private String ratings;
    private String reviews;
    private String source; // Flipkart | Amazon | Croma
}
