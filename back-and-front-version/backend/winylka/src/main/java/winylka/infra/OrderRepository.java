package winylka.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import winylka.model.CustomerOrder;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findAllByOrderByCreatedAtDesc();

    @Query("""
            select distinct o
            from CustomerOrder o
            left join fetch o.items i
            left join fetch i.product
            where o.id = :id
            """)
    Optional<CustomerOrder> findDetailedById(@Param("id") Long id);

    @Query("""
        select distinct o
        from CustomerOrder o
        left join fetch o.items
        order by o.createdAt desc
        """)
    List<CustomerOrder> findAllForAdmin();
}