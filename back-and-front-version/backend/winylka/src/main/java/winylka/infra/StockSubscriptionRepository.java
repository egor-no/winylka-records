package winylka.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import winylka.model.StockSubscription;
import winylka.model.StockSubscriptionType;

import java.util.List;
import java.util.Optional;

public interface StockSubscriptionRepository
        extends JpaRepository<StockSubscription, Long> {

    Optional<StockSubscription>
    findByProductIdAndEmailIgnoreCaseAndType(int productId, String email, StockSubscriptionType type);

    List<StockSubscription> findAllByProductIdAndActiveTrue(int productId);
}