package com.jsaul.webflux_thymeleaf.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.jsaul.webflux_thymeleaf.models.documents.Category;

public interface CategoryDao extends ReactiveMongoRepository<Category, String> {

}
