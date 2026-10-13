package ru.stickoroch.mineshop.web.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stickoroch.mineshop.core.model.ProductModel;
import ru.stickoroch.mineshop.core.repository.ProductRepository;
import ru.stickoroch.mineshop.web.dto.Dtos;

@Transactional
@AllArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService {

    ProductRepository productRepository;

    @Transactional(readOnly = true)
    public ProductModel getProduct(long id){
        return productRepository.findById(id).orElseThrow();
    }

    @Transactional(readOnly = true)
    public Page<ProductModel> list(int page){
        return productRepository.findAll(PageRequest.of(page, 10));
    }

    public ProductModel create(Dtos.ProductRequest r){
        ProductModel productModel = new ProductModel();
        productModel.setTitle(r.title());
        productModel.setDescription(r.description());
        productModel.setPrice(r.price());
        productModel.setGiveCommand(r.giveCommand());
        productModel.setIsActive(true);

        return productRepository.save(productModel);
    }

    public void delete(long id){
        productRepository.deleteById(id);
    }

    public void setActive(long id, boolean active){
        ProductModel productModel = getProduct(id);
        productModel.setIsActive(active);

        productRepository.save(productModel);
    }

}
