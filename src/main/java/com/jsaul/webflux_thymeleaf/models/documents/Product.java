package com.jsaul.webflux_thymeleaf.models.documents;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document (collation = "products")
public class Product {

    @Id 
    private String id;

    private String name;
    private Double price;
    private Date createAt;

    /**
     * Constructors
     */

    public Product() {

    }

    public Product(String name, Double price, Date createAt) {
        this.name = name;
        this.price = price;
        this.createAt = createAt;
    }

    /**
     * Getters an Setters
     */ 

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

}
