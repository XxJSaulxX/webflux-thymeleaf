package com.jsaul.webflux_thymeleaf.controllers;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;

import com.jsaul.webflux_thymeleaf.models.dao.ProductDao;
import com.jsaul.webflux_thymeleaf.models.documents.Product;

import reactor.core.publisher.Flux;

@Controller
public class ProductController {

    private final ProductDao productDao;

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    public ProductController(ProductDao productDao) {
        this.productDao = productDao;
    }

    @GetMapping({ "/", "/listar" })
    public String listar(Model model) {      

        Flux<Product> products = productDao.findAll()
            .map(product -> {
                product.setName(product.getName().toUpperCase());
                return product;
            });
        
        products.subscribe(product -> log.info(product.getName()));

        model.addAttribute("products", products);
        model.addAttribute("title", "Listado de Productos");
        
        return "listar";
    }

    /**
     * Manejo de contrapersion, Methodo 1
     * Midiendo el buffer en la cantidad de elementos [DataDriver]
     */
    @GetMapping("/listar-datadriver")
    public String listarDataDriver(Model model) {      

        Flux<Product> products = productDao.findAll()
            .map(product -> {
                product.setName(product.getName().toUpperCase());
                return product;
            }).delayElements(Duration.ofSeconds(1));
        
        products.subscribe(product -> log.info(product.getName()));

        model.addAttribute("products", new ReactiveDataDriverContextVariable(products, 1));
        model.addAttribute("title", "Listado de Productos");
        
        return "listar";
    }

    /**
     * Manejo de contrapersion, Methodo 2
     * Midiendo el buffer en bytes
     */
    @GetMapping("/listar-full")
    public String listarFull(Model model) {      

        Flux<Product> products = productDao.findAll()
            .map(product -> {
                product.setName(product.getName().toUpperCase());
                return product;
            }).repeat(5000); // simula repetir 5000 veces el flujo para el ejemplo

        /**
         * # Configurar en el aplication properties
         * spring.thymeleaf.reactive.max-chunk-size=1024
         */

        model.addAttribute("products", products);
        model.addAttribute("title", "Listado de Productos");
        
        return "listar";
    }

    /**
     * Manejo de contrapersion, Methodo 3
     * menejo en algunas vistas
     * Midiendo el buffer en bytes
     */
    @GetMapping("/listar-chunked")
    public String listarChunked(Model model) {      

        Flux<Product> products = productDao.findAll()
            .map(product -> {
                product.setName(product.getName().toUpperCase());
                return product;
            }).repeat(5000); // simula repetir 5000 veces el flujo para el ejemplo

        /**
         * # Configurar en el aplication properties: indicar el byte y las viatas a manejar
         * spring.thymeleaf.reactive.max-chunk-size=1024
         * spring.thymeleaf.reactive.chunked-mode-view-names=listar-chunked
         */

        model.addAttribute("products", products);
        model.addAttribute("title", "Listado de Productos");
        
        return "listar-chunked";
    }



}
