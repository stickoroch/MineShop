package ru.stickoroch.mineshop.core.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import ru.stickoroch.mineshop.application.MineShopApplication;
import ru.stickoroch.mineshop.core.OrderStatus;
import ru.stickoroch.mineshop.core.PlatformType;
import ru.stickoroch.mineshop.core.PlatformUserId;
import ru.stickoroch.mineshop.core.model.OrderModel;
import ru.stickoroch.mineshop.core.model.ProductModel;
import ru.stickoroch.mineshop.core.model.UserModel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = MineShopApplication.class)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findsUserOrdersByStatuses() {
        PlatformUserId userId = new PlatformUserId(1L, PlatformType.TELEGRAM);
        UserModel user = userRepository.save(UserModel.builder().id(userId).mcNickname("Steve").build());
        ProductModel product = productRepository.save(ProductModel.builder()
                .title("Алмазный меч")
                .price(new BigDecimal("100.00"))
                .giveCommand("give %player% diamond_sword 1")
                .isActive(true)
                .build());

        OrderModel paid = orderRepository.save(order(UUID.randomUUID(), user, product, OrderStatus.PAID));
        orderRepository.save(order(UUID.randomUUID(), user, product, OrderStatus.PENDING));

        Page<OrderModel> history = orderRepository.findAllByUser_IdAndStatusIn(
                userId,
                List.of(OrderStatus.PAID, OrderStatus.DELIVERED, OrderStatus.PENDING_DELIVERY, OrderStatus.CANCELED),
                PageRequest.of(0, 10));

        assertThat(history.getContent())
                .extracting(OrderModel::getPaymentId)
                .containsExactly(paid.getPaymentId());

        Optional<OrderModel> found = orderRepository.findByPaymentIdAndUser_Id(paid.getPaymentId(), userId);

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void doesNotReturnAnotherUsersOrder() {
        PlatformUserId firstUser = new PlatformUserId(1L, PlatformType.TELEGRAM);
        PlatformUserId secondUser = new PlatformUserId(2L, PlatformType.TELEGRAM);

        UserModel user = userRepository.save(UserModel.builder().id(firstUser).build());
        userRepository.save(UserModel.builder().id(secondUser).build());

        ProductModel product = productRepository.save(ProductModel.builder()
                .title("Кирка")
                .price(new BigDecimal("50.00"))
                .giveCommand("give %player% diamond_pickaxe 1")
                .isActive(true)
                .build());

        OrderModel order = orderRepository.save(order(UUID.randomUUID(), user, product, OrderStatus.PAID));

        assertThat(orderRepository.findByPaymentIdAndUser_Id(order.getPaymentId(), secondUser)).isEmpty();
    }

    private OrderModel order(UUID paymentId, UserModel user, ProductModel product, OrderStatus status) {
        return OrderModel.builder()
                .paymentId(paymentId)
                .user(user)
                .product(product)
                .amount(1)
                .cost(product.getPrice())
                .status(status)
                .paymentLink("https://yookassa.test/" + paymentId)
                .build();
    }
}
