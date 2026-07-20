package winylka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import winylka.event.OrderShippedEvent;
import winylka.event.OrderShippedEventListener;
import winylka.service.OrderNotificationService;

import java.time.Instant;

import static org.mockito.Mockito.*;

class OrderShippedEventListenerTest {

    private OrderNotificationService notificationService;
    private OrderShippedEventListener listener;

    @BeforeEach
    void setUp() {
        notificationService = mock(OrderNotificationService.class);

        listener = new OrderShippedEventListener(
                notificationService
        );
    }

    @Test
    void shouldNotifyCustomerWhenOrderWasShipped() {
        OrderShippedEvent event = new OrderShippedEvent(
                1001L,
                "Egor Test",
                "egor@example.com",
                "TRACK-12345",
                Instant.parse("2026-07-20T12:00:00Z")
        );

        listener.handle(event);

        verify(notificationService).notifyOrderShipped(
                1001L,
                "Egor Test",
                "egor@example.com",
                "TRACK-12345"
        );

        verifyNoMoreInteractions(notificationService);
    }
}