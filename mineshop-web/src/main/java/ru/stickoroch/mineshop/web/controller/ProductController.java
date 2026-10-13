package ru.stickoroch.mineshop.web.controller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.stickoroch.mineshop.web.dto.Dtos;
import ru.stickoroch.mineshop.web.service.ProductService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {

    ProductService productService;

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    @GetMapping("/{id}")
    public Dtos.ProductResponse getProduct(@PathVariable Long id) {
        return Dtos.ProductResponse.fromModel(productService.getProduct(id));
    }

    @GetMapping("/list/{page}")
    public List<Dtos.ProductResponse> getProducts(@PathVariable int page) {
        return productService.list(page).map(Dtos.ProductResponse::fromModel).stream().toList();
    }

    @PutMapping("/create")
    public void create(@RequestBody Dtos.ProductRequest r) {
        productService.create(r);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }

    @PostMapping("/active")
    public void setActive(@RequestBody Dtos.ProductSetActiveRequest r) {
        productService.setActive(r.id(), r.isActive());
    }


}
