package com.jsaul.webflux_thymeleaf.controllers;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;

import com.jsaul.webflux_thymeleaf.models.documents.Category;
import com.jsaul.webflux_thymeleaf.models.documents.Product;
import com.jsaul.webflux_thymeleaf.models.services.IProductService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SessionAttributes("product") // segunda forma para editar, mantener el prodcto en sesion para tener pesente el ID
@Controller
public class ProductController {

    @Value("${config.uploads.path}")
    private String PATH;

    private final IProductService productService;
    private static final Logger log = LoggerFactory.getLogger(ProductRestController.class);

    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    /**
     * Metodo que sirve para mapear globalmente en las vistas
     * @return
     */
    @ModelAttribute("categories")
    public Flux<Category> allCategories() {
        return productService.findAllCategories();
    }


    @GetMapping({ "/", "/listar" })
    public Mono<String> listar(Model model) {      

        Flux<Product> products = productService.findAllNameUpperCase();

        products.subscribe(product -> log.info(product.getName()));

        model.addAttribute("products", products);
        model.addAttribute("title", "Listado de Productos");
        
        return Mono.just("listar");
    }

    @GetMapping("/form")
    public Mono<String> form(Model model) {

        model.addAttribute("product", new Product());
        model.addAttribute("title", "FORMULARIO PRODUCTO");

        return Mono.just("form");
    }

    @PostMapping ("/form")
    public Mono<String> save(@Valid Product product, BindingResult result, Model model, @RequestPart(name = "file") FilePart file, SessionStatus status) {

        if (result.hasErrors()) {
            
            model.addAttribute("title", "ERRORES EN EL FORMUJLARIO");
            return Mono.just("form");
        } else {
            status.setComplete(); // solo servira para el update v1, ya que en la version2 no estara guarada nada en la session

            Mono<Category> categoryMono = productService.findCategoryById(product.getCategory().getId());

            return categoryMono.flatMap(c -> {

                if (product.getCreateAt() == null) {
                    product.setCreateAt(new Date());
                }

                if (!file.filename().isEmpty()) {
                    product.setPhoto(
                        UUID.randomUUID().toString() + "-" + 
                        file.filename()
                        .replace(" ", "")
                        .replace(":", "")
                        .replace("\\", "")
                    );
                }

                product.setCategory(c);
                return productService.save(product);
            })
            .doOnNext(p -> log.info("PRODUCT:  " + p.getName() + " ID: " + p.getId()))
            .flatMap(p -> {

                if (!file.filename().isEmpty()) {
                    return file.transferTo(new File(PATH + p.getPhoto()));
                }

                return Mono.empty();
            })
            //.then(Mono.just("redirect:/listar")) // alternativa al thenReturn
            .thenReturn("redirect:/listar?success=producto+guardado+con+exito");
        }


    }


    @GetMapping("/ver/{id}")
    public Mono<String> ver(Model model, @PathVariable(name = "id") String productId) {
        
        return productService.findById(productId)
            .doOnNext(p -> {
                model.addAttribute("product", p);
                model.addAttribute("title", "Detalle Produto");
            })
            .switchIfEmpty(Mono.just(new Product()))
            .flatMap(p -> {
                if (p.getId() == null) {
                    return Mono.error(new InterruptedException("Producto no encontrado"));
                }

                return Mono.just(p);
            })
            .then(Mono.just("ver"))
            .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));
    }

    /**
     * Esta es una explrecion regular para colocar la extencion de la foto (:.+)
     * @throws MalformedURLException 
     */
    @GetMapping("/uploads/img/{namePhoto:.+}")
    public Mono<ResponseEntity<Resource>> verFoto(@PathVariable String namePhoto) throws MalformedURLException {

        Path path = Paths.get(PATH).resolve(namePhoto).toAbsolutePath();

        Resource img = new UrlResource(path.toUri());

        return Mono.just(
            ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + img.getFilename() + "\"")
                .body(img)
        );

    } 

    @GetMapping("/form-v2/{id}")
    public Mono<String> updateV2(@PathVariable(name = "id") String productId, Model model) {

        /**¨
         * El defecto de este metodo para actualizar es que no podemosguardar el producto en la sesion
         * para tener pesente e ID, entonces deberiamos tener que usar el id hidden en el input del html
         */

        return productService.findById(productId)
            .doOnNext(p -> {
                model.addAttribute("title", "EDITAR PRODUCTO");
                model.addAttribute("product", p);
            })
            .defaultIfEmpty(new Product())
            .flatMap(p -> {
                if (p.getId() == null) {
                    return Mono.error(new InterruptedException("No existe el producto"));
                }

                return Mono.just(p);
            })
            .then(Mono.just("form"))
            .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));

    }


    @GetMapping("/form/{id}")
    public Mono<String> update(@PathVariable(name = "id") String productId, Model model) {

        Mono<Product> product = productService.findById(productId)
            .defaultIfEmpty(new Product());

        model.addAttribute("title", "EDITAR PRODUCTO");
        model.addAttribute("product", product);

        return Mono.just("form");
    }

    @GetMapping("/delete/{id}")
    public Mono<String> delete(@PathVariable String id) {
        return productService.findById(id)
            .defaultIfEmpty(new Product())
            .flatMap(p -> {
                if (p.getId() == null) {
                    return Mono.error(new InterruptedException("No existe el producto"));
                }

                return Mono.just(p);
            })
            //.flatMap(p -> productService.delete(p))
            .flatMap(productService::delete)
            .then(Mono.just("redirect:/listar?success=producto+eliminado+con+exito"))
            .onErrorResume(ex -> Mono.just("redirect:/listar?error=no+existe+el+producto"));
    }


    /**
     * Manejo de contrapersion, Methodo 1
     * Midiendo el buffer en la cantidad de elementos [DataDriver]
     */
    @GetMapping("/listar-datadriver")
    public String listarDataDriver(Model model) {      

        Flux<Product> products = productService.findAllNameUpperCase()
            .delayElements(Duration.ofSeconds(1));
        
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

        Flux<Product> products = productService.findAllNameUpperCaseRepeat();

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

        Flux<Product> products = productService.findAllNameUpperCaseRepeat();

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
