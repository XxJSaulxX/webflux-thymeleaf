package com.jsaul.webflux_thymeleaf.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.jsaul.webflux_thymeleaf.models.documents.Product;

public interface ProductDao extends ReactiveMongoRepository<Product, String> {

}
