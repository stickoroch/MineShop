package ru.stickoroch.mineshop.core.model;

import jakarta.persistence.*;
        import lombok.*;
import ru.stickoroch.mineshop.core.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductModel implements ProductRepository.ProductPopularityView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 128)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "image_cover", length = 512)
    private String imageCover;

    @Column(name = "give_command", nullable = false, columnDefinition = "TEXT")
    private String giveCommand; //player - %player% amount - %amount%

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderModel> orders = new ArrayList<>();

    @Override
    public Long getOrdersCount() {
        return (long) orders.size();
    }
}