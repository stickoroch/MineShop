package ru.stickoroch.mineshop.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
