package ru.stickoroch.mineshop.web.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.stickoroch.mineshop.core.model.ProductModel;
import ru.stickoroch.mineshop.core.repository.ProductRepository;
import ru.stickoroch.mineshop.web.dto.Dtos;
import ru.stickoroch.mineshop.web.media.CoverFileStorage;

@Transactional
@AllArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService {

    ProductRepository productRepository;
    CoverFileStorage fileStorage;

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
        fileStorage.deleteCover(id);
        productRepository.deleteById(id);
    }

    public void setActive(long id, boolean active){
        ProductModel productModel = getProduct(id);
        productModel.setIsActive(active);

        productRepository.save(productModel);
    }

    public void setCover(long id, MultipartFile file) {
        ProductModel productModel = getProduct(id);
        if(productModel.getImageCover() != null){
            fileStorage.deleteCover(id);
        }
        String key = fileStorage.load(file, id);
        productModel.setImageCover(key);

        productRepository.save(productModel);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getCover(long id) {
        String key = getProduct(id).getImageCover();
        if(key != null)
            return fileStorage.getCoverAsResource(key);
        return ResponseEntity.notFound().build();
    }

    public void edit(long id, Dtos.ProductRequest r) {
        ProductModel productModel = getProduct(id);
        productModel.setTitle(r.title());
        productModel.setDescription(r.description());
        productModel.setPrice(r.price());
        productModel.setGiveCommand(r.giveCommand());
        productModel.setIsActive(true);

        productRepository.save(productModel);
    }
}
