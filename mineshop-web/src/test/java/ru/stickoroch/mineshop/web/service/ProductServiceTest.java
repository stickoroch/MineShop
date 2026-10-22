package ru.stickoroch.mineshop.web.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductServiceTest {

    @Test
    void normalizesSearchQuery() {
        assertThat(ProductService.normalizeSearchQuery("  алмаз%ный__   меч  "))
                .isEqualTo("алмазный меч");
        assertThat(ProductService.normalizeSearchQuery(null)).isEmpty();
        assertThat(ProductService.normalizeSearchQuery("   ")).isEmpty();
    }

    @Test
    void limitsSearchQueryLength() {
        String query = "а".repeat(100);

        assertThat(ProductService.normalizeSearchQuery(query))
                .hasSize(ProductService.MAX_SEARCH_QUERY_LENGTH);
    }
}
