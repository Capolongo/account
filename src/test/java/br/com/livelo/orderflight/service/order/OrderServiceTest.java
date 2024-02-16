package br.com.livelo.orderflight.service.order;

import br.com.livelo.orderflight.mappers.ConfirmOrderMapper;
import br.com.livelo.orderflight.mappers.OrderProcessMapper;
import br.com.livelo.orderflight.domain.dtos.connector.response.ConnectorConfirmOrderStatusResponse;
import br.com.livelo.orderflight.domain.dtos.repository.OrderProcess;
import br.com.livelo.orderflight.domain.dtos.repository.PaginationOrderProcessResponse;
import br.com.livelo.orderflight.domain.entity.OrderEntity;
import br.com.livelo.orderflight.domain.entity.OrderStatusEntity;
import br.com.livelo.orderflight.exception.OrderFlightException;
import br.com.livelo.orderflight.mock.MockBuilder;
import br.com.livelo.orderflight.repository.OrderRepository;
import br.com.livelo.orderflight.service.order.impl.OrderServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ConfirmOrderMapper confirmOrderMapper;
    @Mock
    private OrderProcessMapper orderProcessMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void shouldReturnFoundOrderById() throws OrderFlightException {
        Optional<OrderEntity> mockedOrder = Optional.of(MockBuilder.orderEntity());
        when(orderRepository.findById(anyString())).thenReturn(mockedOrder);
        OrderEntity order = orderService.getOrderById("id");
        assertEquals(mockedOrder.get(), order);
    }

    @Test
    void shouldThrowReservationExceptionWhenOrderNotFound() throws OrderFlightException {
        when(orderRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(OrderFlightException.class, () -> {
            orderService.getOrderById("id");
        });
    }

    @Test
    void shouldAddNewStatusToOrder() {
        OrderEntity order = MockBuilder.orderEntity();
        OrderStatusEntity status = MockBuilder.statusFailed();

        when(confirmOrderMapper
                .connectorConfirmOrderStatusResponseToStatusEntity(any(ConnectorConfirmOrderStatusResponse.class)))
                .thenReturn(status);

        orderService.addNewOrderStatus(order,
                confirmOrderMapper.connectorConfirmOrderStatusResponseToStatusEntity(
                        MockBuilder.connectorConfirmOrderStatusResponse()));

        assertEquals(status, order.getCurrentStatus());
    }

    @Test
    void shouldFindOrderByCommerceOrderId() {
        var orderMock = mock(OrderEntity.class);
        when(this.orderRepository.findByCommerceOrderId(anyString())).thenReturn(Optional.of(orderMock));
        var response = this.orderService.findByCommerceOrderId("123");

        assertAll(
                () -> assertTrue(response.isPresent()),
                () -> assertInstanceOf(OrderEntity.class, response.get()));
    }

    @Test
    void shouldntFindOrderByCommerceOrderId() {
        when(this.orderRepository.findByCommerceOrderId(anyString())).thenReturn(Optional.empty());
        var response = this.orderService.findByCommerceOrderId("123");

        assertTrue(!response.isPresent());
    }

    @Test
    void shouldDeleteOrder() {
        var orderMock = mock(OrderEntity.class);
        doNothing().when(orderRepository).delete(any(OrderEntity.class));
        this.orderService.delete(orderMock);

        verify(orderRepository, times(1)).delete(orderMock);
    }

    @Test
    void shouldSaveOrder() {
        var orderMock = mock(OrderEntity.class);
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderMock);
        var response = this.orderService.save(orderMock);

        assertNotNull(response);
    }

    @Test
    void shouldReturnSuccessGetOrdersByStatusCode() throws OrderFlightException {
        ReflectionTestUtils.setField(orderService, "orderProcessMaxRows", 500);

        String statusCode = "LIVPNR-1006";
        int page = 1;
        int rows = 4;

        Page<OrderProcess> repositoryResponse = Page.empty();
        PaginationOrderProcessResponse mappedRepositoryResponse = MockBuilder.paginationOrderProcessResponse(page, rows);

        when(orderRepository.findAllByCurrentStatusCode(anyString(), any(Pageable.class))).thenReturn(repositoryResponse);
        when(orderProcessMapper.pageRepositoryToPaginationResponse(any())).thenReturn(mappedRepositoryResponse);

        PaginationOrderProcessResponse response = orderService.getOrdersByStatusCode(statusCode, page, rows);

        assertEquals(mappedRepositoryResponse, response);
        assertEquals(mappedRepositoryResponse.getOrders().size(), response.getRows());
        verify(orderRepository).findAllByCurrentStatusCode(statusCode, PageRequest.of(page -  1, rows));
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void shouldReturnSuccessGetOrdersByStatusCodeWithRowsGraterThenMaxLimit() throws OrderFlightException {
        ReflectionTestUtils.setField(orderService, "orderProcessMaxRows", 500);

        String statusCode = "LIVPNR-1006";
        int page = 1;
        int rows = 600;

        Page<OrderProcess> repositoryResponse = Page.empty();
        PaginationOrderProcessResponse mappedRepositoryResponse = MockBuilder.paginationOrderProcessResponse(page, rows);

        when(orderRepository.findAllByCurrentStatusCode(anyString(), any(Pageable.class))).thenReturn(repositoryResponse);
        when(orderProcessMapper.pageRepositoryToPaginationResponse(any())).thenReturn(mappedRepositoryResponse);

        PaginationOrderProcessResponse response = orderService.getOrdersByStatusCode(statusCode, page, rows);

        assertEquals(mappedRepositoryResponse, response);
        assertEquals(mappedRepositoryResponse.getOrders().size(), response.getRows());
        verify(orderRepository).findAllByCurrentStatusCode(statusCode, PageRequest.of(page -  1, 500));
        verifyNoMoreInteractions(orderRepository);
    }
}
