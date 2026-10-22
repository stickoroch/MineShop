package ru.stickoroch.mineshop.web.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.stickoroch.mineshop.core.model.ProductModel;
import ru.stickoroch.mineshop.core.repository.ProductRepository;
import ru.stickoroch.mineshop.web.cache.CacheNames;
import ru.stickoroch.mineshop.web.dto.Dtos;
import ru.stickoroch.mineshop.web.media.CoverFileStorage;

import java.util.Locale;
import java.util.Optional;

@Transactional
@AllArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService {

    public static final int PAGE_SIZE = 10;
    public static final int MIN_SEARCH_QUERY_LENGTH = 2;
    public static final int MAX_SEARCH_QUERY_LENGTH = 64;

    ProductRepository productRepository;
    CoverFileStorage fileStorage;

    @Transactional(readOnly = true)
    public ProductModel getProduct(long id){
        return productRepository.findById(id).orElseThrow();
    }

    @Transactional(readOnly = true)
    public Page<ProductModel> list(int page){
        return productRepository.findAll(PageRequest.of(page, PAGE_SIZE));
    }

    @CacheEvict(cacheNames = {CacheNames.PRODUCT_CATALOG, CacheNames.PRODUCT_SEARCH}, allEntries = true)
    public ProductModel create(Dtos.ProductRequest r){
        ProductModel productModel = new ProductModel();
        productModel.setTitle(r.title());
        productModel.setDescription(r.description());
        productModel.setPrice(r.price());
        productModel.setGiveCommand(r.giveCommand());
        productModel.setIsActive(Boolean.TRUE.equals(r.isActive()));

        return productRepository.save(productModel);
    }

    @CacheEvict(cacheNames = {CacheNames.PRODUCT_CATALOG, CacheNames.PRODUCT_SEARCH}, allEntries = true)
    public void delete(long id){
        fileStorage.deleteCover(id);
        productRepository.deleteById(id);
    }

    @CacheEvict(cacheNames = {CacheNames.PRODUCT_CATALOG, CacheNames.PRODUCT_SEARCH}, allEntries = true)
    public void setActive(long id, boolean active){
        ProductModel productModel = getProduct(id);
        productModel.setIsActive(active);

        productRepository.save(productModel);
    }

    @CacheEvict(cacheNames = {CacheNames.PRODUCT_CATALOG, CacheNames.PRODUCT_SEARCH}, allEntries = true)
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

    @CacheEvict(cacheNames = {CacheNames.PRODUCT_CATALOG, CacheNames.PRODUCT_SEARCH}, allEntries = true)
    public void edit(long id, Dtos.ProductRequest r) {
        ProductModel productModel = getProduct(id);
        productModel.setTitle(r.title());
        productModel.setDescription(r.description());
        productModel.setPrice(r.price());
        productModel.setGiveCommand(r.giveCommand());
        productModel.setIsActive(Boolean.TRUE.equals(r.isActive()));

        productRepository.save(productModel);
    }

    @Transactional(readOnly = true)
    public Optional<ProductModel> findActiveProduct(long id) {
        return productRepository.findById(id)
                .filter(product -> Boolean.TRUE.equals(product.getIsActive()));
    }

    @Transactional(readOnly = true)
    public Page<ProductRepository.ProductPopularityView> getPopularProductsPage(int page) {
        return productRepository.findPopularActiveProjections(PageRequest.of(page, PAGE_SIZE));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.PRODUCT_CATALOG, key = "#page")
    public Page<ProductModel> getProductsPage(int page) {
        return productRepository.findAllByIsActiveIsTrue(PageRequest.of(page, PAGE_SIZE));
    }

    /**
     * Поиск активных товаров по названию и описанию.
     * Регистр не важен, спецсимволы LIKE экранируются через очистку запроса.
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.PRODUCT_SEARCH, key = "#query + ':' + #page")
    public Page<ProductModel> searchProducts(String query, int page) {
        String normalized = normalizeSearchQuery(query);

        if (normalized.length() < MIN_SEARCH_QUERY_LENGTH) {
            return Page.empty(PageRequest.of(page, PAGE_SIZE));
        }

        String pattern = "%" + normalized.toLowerCase(Locale.ROOT) + "%";

        return productRepository.searchActive(pattern, PageRequest.of(page, PAGE_SIZE));
    }

    /**
     * Нормализует пользовательский запрос: убирает лишние пробелы,
     * спецсимволы LIKE и ограничивает длину.
     */
    public static String normalizeSearchQuery(String query) {
        if (query == null) {
            return "";
        }

        String normalized = query.trim()
                .replaceAll("\\s+", " ")
                .replace("\\", "")
                .replace("%", "")
                .replace("_", "");

        if (normalized.length() > MAX_SEARCH_QUERY_LENGTH) {
            normalized = normalized.substring(0, MAX_SEARCH_QUERY_LENGTH).trim();
        }

        return normalized;
    }
}
