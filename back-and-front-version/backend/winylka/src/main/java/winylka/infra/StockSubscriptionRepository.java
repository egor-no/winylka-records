package winylka.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import winylka.model.StockSubscription;

import java.util.List;
import java.util.Optional;

public interface StockSubscriptionRepository
        extends JpaRepository<StockSubscription, Long> {

    Optional<StockSubscription> findByProductIdAndEmailIgnoreCase(int productId, String email);

    List<StockSubscription> findAllByProductIdAndActiveTrue(int productId);
}