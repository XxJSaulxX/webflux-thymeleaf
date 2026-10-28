package com.jsaul.webflux_thymeleaf.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.jsaul.webflux_thymeleaf.models.documents.Product;
import com.jsaul.webflux_thymeleaf.models.services.IProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

    private final IProductService productService;
    private static final Logger log = LoggerFactory.getLogger(ProductRestController.class);

    public ProductRestController(IProductService productService) {
        this.productService = productService;
    }

    @GetMapping()
    public Flux<Product> index() {

        Flux<Product> products = productService.findAll()
            .map(product -> {
                product.setName(product.getName().toUpperCase());
                return product;
            }).doOnNext(product -> log.info(product.getName()));

        return products;
    }

    @GetMapping("/{id}")
    public Mono<Product> show(@PathVariable(name = "id") String productId) {

        //Mono<Product> product = productDao.findById(productId);

        Flux<Product> products = productService.findAll();

        Mono<Product> product = products.filter(p -> p.getId().equals(productId))
            .next() // convierte un Flux a on Mono
            .doOnNext(p -> log.info(p.getName()));

        return product;
    }

}
