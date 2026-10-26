package com.jsaul.webflux_thymeleaf;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.jsaul.webflux_thymeleaf.models.dao.ProductDao;
import com.jsaul.webflux_thymeleaf.models.documents.Product;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class WebfluxThymeleafApplication implements CommandLineRunner {

	@Autowired 
	private ProductDao productDao;

	@Autowired 
	private ReactiveMongoTemplate mongoTemplate;

	private static final Logger log = LoggerFactory.getLogger(WebfluxThymeleafApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(WebfluxThymeleafApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		mongoTemplate.dropCollection("products").subscribe();

		Flux.just(
			new Product("Laptop Lenovo IdeaPad", 2499.90),
			new Product("Mouse Logitech inalámbrico", 89.90),
			new Product("Teclado mecánico Redragon", 159.90),
			new Product("Monitor Samsung 27 pulgadas", 899.90),
			new Product("Audífonos Sony Bluetooth", 249.90),
			new Product("Teclado Logitech inalámbrico", 129.90),
			new Product("Disco SSD Kingston 1TB", 299.90),
			new Product("Webcam Logitech Full HD", 199.90)
		)
		.flatMap(p -> {
			p.setCreateAt(new Date());
			return productDao.save(p);
		})
		.subscribe(p -> log.info("Insert: " + p.getId() + " " + p.getName()));

	}

}
