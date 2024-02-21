package br.com.livelo.orderflight.service.order;

import java.util.Optional;

import br.com.livelo.orderflight.domain.dtos.repository.OrderProcess;
import br.com.livelo.orderflight.domain.dtos.repository.PaginationOrderProcessResponse;
import br.com.livelo.orderflight.domain.entity.OrderEntity;
import br.com.livelo.orderflight.domain.entity.OrderStatusEntity;
import br.com.livelo.orderflight.exception.OrderFlightException;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface OrderService {
  OrderEntity getOrderById(String id) throws OrderFlightException;

  PaginationOrderProcessResponse getOrdersByStatusCode(String status, Integer page, Integer rows) throws OrderFlightException;

  void addNewOrderStatus(OrderEntity order, OrderStatusEntity status);

  Optional<OrderEntity> findByCommerceOrderId(String commerceOrderId);

  void delete(OrderEntity order);

  OrderEntity save(OrderEntity order);

  void orderProcess(OrderProcess payload) throws JsonProcessingException;
}
