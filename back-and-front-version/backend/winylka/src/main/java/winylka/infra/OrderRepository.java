package winylka.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import winylka.model.CustomerOrder;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
}