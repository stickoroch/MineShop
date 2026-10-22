package ru.stickoroch.mineshop.core.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import ru.stickoroch.mineshop.application.MineShopApplication;
import ru.stickoroch.mineshop.core.model.ProductModel;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = MineShopApplication.class)
class ProductSearchTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findsActiveProductsByTitleAndDescriptionIgnoringCase() {
        productRepository.save(product("Алмазный меч", "Острый клинок", true));
        productRepository.save(product("Алмазная кирка", "Инструмент для камня", true));
        productRepository.save(product("Меч из дерева", "Учебный меч", false));
        productRepository.save(product("Зелье", "В описании тоже есть МЕЧ", true));

        Page<ProductModel> result = productRepository.searchActive("%меч%", PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(ProductModel::getTitle)
                .containsExactly("Алмазный меч", "Зелье");
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void paginatesSearchResults() {
        for (int i = 0; i < 3; i++) {
            productRepository.save(product("Меч " + i, null, true));
        }

        Page<ProductModel> firstPage = productRepository.searchActive("%меч%", PageRequest.of(0, 2));

        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.hasNext()).isTrue();

        Page<ProductModel> secondPage = productRepository.searchActive("%меч%", PageRequest.of(1, 2));

        assertThat(secondPage.getContent()).hasSize(1);
        assertThat(secondPage.hasNext()).isFalse();
    }

    @Test
    void returnsNothingForMissingText() {
        productRepository.save(product("Алмазный меч", "Острый клинок", true));

        Page<ProductModel> result = productRepository.searchActive("%несуществующий%", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    private ProductModel product(String title, String description, boolean active) {
        return ProductModel.builder()
                .title(title)
                .description(description)
                .price(new BigDecimal("100.00"))
                .giveCommand("give %player% diamond 1")
                .isActive(active)
                .build();
    }
}
