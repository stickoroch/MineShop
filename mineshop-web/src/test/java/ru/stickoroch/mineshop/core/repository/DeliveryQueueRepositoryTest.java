package ru.stickoroch.mineshop.core.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import ru.stickoroch.mineshop.application.MineShopApplication;
import ru.stickoroch.mineshop.core.DeliveryStatus;
import ru.stickoroch.mineshop.core.OrderStatus;
import ru.stickoroch.mineshop.core.PlatformType;
import ru.stickoroch.mineshop.core.PlatformUserId;
import ru.stickoroch.mineshop.core.model.OrderDeliveryModel;
import ru.stickoroch.mineshop.core.model.OrderModel;
import ru.stickoroch.mineshop.core.model.ProductModel;
import ru.stickoroch.mineshop.core.model.UserModel;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = MineShopApplication.class)
class DeliveryQueueRepositoryTest {

    @Autowired
    private DeliveryQueueRepository deliveryQueueRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void storesAndFindsDeliveryQueueEntry() {
        OrderModel order = savedOrder();

        OrderDeliveryModel delivery = deliveryQueueRepository.save(OrderDeliveryModel.builder()
                .order(order)
                .playerName("Steve")
                .command("give %player% diamond 1")
                .status(DeliveryStatus.PENDING)
                .build());

        assertThat(delivery.getId()).isNotNull();
        assertThat(deliveryQueueRepository.findByOrder_PaymentId(order.getPaymentId())).isPresent();
        assertThat(deliveryQueueRepository.findAllByStatusOrderByCreatedAtAsc(DeliveryStatus.PENDING))
                .extracting(OrderDeliveryModel::getId)
                .contains(delivery.getId());
    }

    @Test
    void findsPendingDeliveriesByPlayer() {
        OrderModel order = savedOrder();

        OrderDeliveryModel delivery = deliveryQueueRepository.save(OrderDeliveryModel.builder()
                .order(order)
                .playerName("Steve")
                .command("give %player% diamond 1")
                .status(DeliveryStatus.PENDING)
                .build());

        assertThat(deliveryQueueRepository
                .findAllByPlayerNameIgnoreCaseAndStatusOrderByCreatedAtAsc("steve", DeliveryStatus.PENDING))
                .extracting(OrderDeliveryModel::getId)
                .contains(delivery.getId());

        assertThat(deliveryQueueRepository
                .findAllByPlayerNameIgnoreCaseAndStatusOrderByCreatedAtAsc("Alex", DeliveryStatus.PENDING))
                .isEmpty();
    }

    private OrderModel savedOrder() {
        PlatformUserId userId = new PlatformUserId(1L, PlatformType.TELEGRAM);

        UserModel user = userRepository.save(UserModel.builder().id(userId).mcNickname("Steve").build());

        ProductModel product = productRepository.save(ProductModel.builder()
                .title("Алмазный меч")
                .price(new BigDecimal("100.00"))
                .giveCommand("give %player% diamond 1")
                .isActive(true)
                .build());

        return orderRepository.save(OrderModel.builder()
                .paymentId(UUID.randomUUID())
                .user(user)
                .product(product)
                .amount(1)
                .cost(product.getPrice())
                .status(OrderStatus.PAID)
                .paymentLink("https://yookassa.test/pay")
                .build());
    }
}
