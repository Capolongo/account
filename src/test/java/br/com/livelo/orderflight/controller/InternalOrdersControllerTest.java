package br.com.livelo.orderflight.controller;

import br.com.livelo.orderflight.entities.OrderEntity;
import br.com.livelo.orderflight.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import br.com.livelo.orderflight.domain.entity.OrderEntity;
import br.com.livelo.orderflight.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class InternalOrdersControllerTest {
    @InjectMocks
    private InternalOrdersController controller;
    @Mock
    private OrderRepository orderRepository;
    private OrderEntity order;

    @BeforeEach
    void setup() {
        order = OrderEntity.builder()
                .id("lf1")
                .build();
    }

    @Test
    void shouldCreateOrder() throws Exception {
        var expected = mock(OrderEntity.class);
        Mockito.when(orderRepository.save(Mockito.any(OrderEntity.class))).thenReturn(expected);

        var response = controller.createOrder(expected);
        assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertNotNull(response),
                () -> assertEquals(expected, response.getBody())
        );
    }

    //TODO ESCREVER TESTES PARA CENÁRIOS DE ERRO

    @Test
    void shouldRetrieveOrder() {
        Mockito.when(orderRepository.findById(Mockito.anyString())).thenReturn(Optional.of(order));

        var response = controller.getById(order.getId());
        assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertNotNull(response),
                () -> assertEquals(order, response.getBody())
        );
    }
}







