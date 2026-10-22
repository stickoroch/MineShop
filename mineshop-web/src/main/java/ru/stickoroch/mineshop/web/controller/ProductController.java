package ru.stickoroch.mineshop.web.controller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.stickoroch.mineshop.web.dto.Dtos;
import ru.stickoroch.mineshop.web.media.CoverFileStorage;
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
    public Dtos.ProductResponse create(@Valid @RequestBody Dtos.ProductRequest r) {
        return Dtos.ProductResponse.fromModel(productService.create(r));
    }

    @PostMapping("/edit/{id}")
    public void edit(@PathVariable long id, @Valid @RequestBody Dtos.ProductRequest r) {
        productService.edit(id, r);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }

    @PostMapping("/active")
    public void setActive(@Valid @RequestBody Dtos.ProductSetActiveRequest r) {
        productService.setActive(r.id(), r.isActive());
    }

    @PostMapping(
            value = "/cover/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public void cover(
            @PathVariable long id,
            @RequestParam("file") MultipartFile file
    ) {
        productService.setCover(id, file);
    }

    @GetMapping("/cover/{id}")
    public ResponseEntity<Resource> getCover(@PathVariable long id){
       return productService.getCover(id);
    }
}
