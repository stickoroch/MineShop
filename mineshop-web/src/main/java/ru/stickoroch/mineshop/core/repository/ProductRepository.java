package ru.stickoroch.mineshop.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.stickoroch.mineshop.core.model.ProductModel;

import java.math.BigDecimal;

@Repository
public interface ProductRepository extends JpaRepository<ProductModel, Long> {

    public interface ProductPopularityView {
        Long getId();
        String getTitle();
        BigDecimal getPrice();
        String getImageCover();
        Long getOrdersCount();
    }

    @Query(
            value = """
        SELECT p.id AS id, p.title AS title, p.price AS price,
               p.imageCover AS imageCover, COUNT(o) AS ordersCount
        FROM ProductModel p
        LEFT JOIN p.orders o
        WHERE p.isActive = true
        GROUP BY p.id, p.title, p.price, p.imageCover
        ORDER BY COUNT(o) DESC, p.id ASC
        """,
            countQuery = "SELECT COUNT(p) FROM ProductModel p WHERE p.isActive = true"
    )
    Page<ProductPopularityView> findPopularActiveProjections(Pageable pageable);

    Page<ProductModel> findAllByIsActiveIsTrue(Pageable page);

    /**
     * Поиск активных товаров по названию или описанию.
     * {@code pattern} уже подготовлен сервисом: приведён к нижнему регистру,
     * очищен от спецсимволов LIKE и обёрнут в {@code %...%}.
     */
    @Query("""
            SELECT p FROM ProductModel p
            WHERE p.isActive = true
              AND (LOWER(p.title) LIKE :pattern
                   OR LOWER(COALESCE(p.description, '')) LIKE :pattern)
            ORDER BY p.title ASC
            """)
    Page<ProductModel> searchActive(@Param("pattern") String pattern, Pageable pageable);
}
