package br.com.livelo.orderflight.service.order.impl;

import br.com.livelo.orderflight.domain.dtos.repository.OrderProcess;
import br.com.livelo.orderflight.domain.dtos.repository.PaginationOrderProcessResponse;
import br.com.livelo.orderflight.domain.dtos.sku.SkuItemResponse;
import br.com.livelo.orderflight.domain.dtos.update.UpdateOrderDTO;
import br.com.livelo.orderflight.domain.entity.OrderEntity;
import br.com.livelo.orderflight.domain.entity.OrderItemEntity;
import br.com.livelo.orderflight.domain.entity.OrderStatusEntity;
import br.com.livelo.orderflight.domain.entity.ProcessCounterEntity;
import br.com.livelo.orderflight.enuns.StatusLivelo;
import br.com.livelo.orderflight.exception.OrderFlightException;
import br.com.livelo.orderflight.exception.enuns.OrderFlightErrorType;
import br.com.livelo.orderflight.mappers.LiveloPartnersMapper;
import br.com.livelo.orderflight.mappers.OrderProcessMapper;
import br.com.livelo.orderflight.proxies.LiveloPartnersProxy;
import br.com.livelo.orderflight.repository.ItemRepository;
import br.com.livelo.orderflight.repository.OrderRepository;
import br.com.livelo.orderflight.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderProcessMapper orderMapper;
    private final ItemRepository itemRepository;
    private final LiveloPartnersProxy liveloPartnersProxy;
    private final LiveloPartnersMapper liveloPartnersMapper;

    @Value("${order.orderProcessMaxRows}")
    private int orderProcessMaxRows;

    public OrderEntity getOrderById(String id) throws OrderFlightException {
        Optional<OrderEntity> order = orderRepository.findById(id);

        if (order.isEmpty()) {
            OrderFlightErrorType errorType = OrderFlightErrorType.VALIDATION_ORDER_NOT_FOUND;
            throw new OrderFlightException(errorType, errorType.getTitle(), null);
        }

        return order.get();
    }

    public void addNewOrderStatus(OrderEntity order, OrderStatusEntity status) {
        if (isSameStatus(status.getCode(), order.getCurrentStatus().getCode())) {
            return;
        }

        order.getStatusHistory().add(status);
        order.setCurrentStatus(status);
    }

    public OrderItemEntity getFlightFromOrderItems(Set<OrderItemEntity> orderItemsEntity) throws OrderFlightException {
        Optional<OrderItemEntity> itemFlight = orderItemsEntity.stream().filter(item -> !item.getSkuId().toLowerCase().contains("tax")).findFirst();

        if (itemFlight.isEmpty()) {
            OrderFlightErrorType errorType = OrderFlightErrorType.VALIDATION_ORDER_NOT_FOUND;
            throw new OrderFlightException(errorType, errorType.getTitle(), null);
        }

        return itemFlight.get();
    }

    public OrderItemEntity getTaxFromOrderItems(Set<OrderItemEntity> orderItemsEntity) throws OrderFlightException {
        Optional<OrderItemEntity> itemTax = orderItemsEntity.stream().filter(item -> item.getSkuId().toLowerCase().contains("tax")).findFirst();

        if (itemTax.isEmpty()) {
            OrderFlightErrorType errorType = OrderFlightErrorType.VALIDATION_ORDER_NOT_FOUND;
            throw new OrderFlightException(errorType, errorType.getTitle(), null);
        }

        return itemTax.get();
    }

    public void orderDetailLog(String invokedBy, String newStatusCode, OrderEntity order) {
        OrderItemEntity flightItem =  getFlightFromOrderItems(order.getItems());
        OrderItemEntity taxItem =  getTaxFromOrderItems(order.getItems());
        log.info("OrderService.orderDetailLog - " + invokedBy + " - orderId: [{}], partnerCode: [{}], statusCode: [{}], amount: [{}], pointsAmount: [{}], partnerAmount: [{}], flightAmount: [{}], flightPointsAmount: [{}], flightPartnerAmount: [{}], taxAmount: [{}], taxPointsAmount: [{}], taxPartnerAmount: [{}],", order.getId(), order.getPartnerCode(), newStatusCode, order.getPrice().getAmount(), order.getPrice().getPointsAmount(), order.getPrice().getPartnerAmount(), flightItem.getPrice().getAmount(),  flightItem.getPrice().getPointsAmount(),  flightItem.getPrice().getPartnerAmount(), taxItem.getPrice().getAmount(),  taxItem.getPrice().getPointsAmount(),  taxItem.getPrice().getPartnerAmount());
    }

    public boolean isSameStatus(String currentStatus, String newStatus) {
        return currentStatus.equals(newStatus);
    }

    public OrderStatusEntity buildOrderStatusFailed(String cause) {
        return OrderStatusEntity.builder()
                .partnerCode(String.valueOf(500))
                .code(StatusLivelo.FAILED.getCode())
                .partnerResponse(cause)
                .partnerDescription("failed")
                .description(StatusLivelo.FAILED.getDescription())
                .statusDate(LocalDateTime.now())
                .build();
    }

    public void incrementProcessCounter(ProcessCounterEntity processCounter) {
        processCounter.setCount(processCounter.getCount() + 1);
    }

    public ProcessCounterEntity getProcessCounter(OrderEntity order, String process) {
        var processCounter = order.getProcessCounters().stream().filter(counter -> process.equals(counter.getProcess())).findFirst();

        if (processCounter.isEmpty()) {
            var newProcessCounter = ProcessCounterEntity.builder().process(process).count(0).build();
            order.getProcessCounters().add(newProcessCounter);
            return newProcessCounter;
        }

        return processCounter.get();
    }

    public void updateVoucher(OrderItemEntity orderItem, String voucher) {
        orderItem.getTravelInfo().setVoucher(voucher);
        if (voucher != null) {
            log.info("OrderService.updateVoucher - voucher updated - orderId: [{}]", orderItem.getId());
        }
    }

    public void updateSubmittedDate(OrderEntity order, String date) {
        order.setSubmittedDate(LocalDateTime.parse(date));
    }

    public Optional<OrderEntity> findByCommerceOrderId(String commerceOrderId) {
        return this.orderRepository.findByCommerceOrderId(commerceOrderId);
    }

    public void delete(OrderEntity order) {
        this.orderRepository.delete(order);
    }

    public OrderEntity save(OrderEntity order) {
        return this.orderRepository.save(order);
    }

    public PaginationOrderProcessResponse getOrdersByStatusCode(String statusCode, Optional<String> limitArrivalDate, Integer page, Integer rows)
            throws OrderFlightException {
        Pageable pagination = pageRequestOf(page, rows);

        Page<OrderProcess> foundOrders;
        if (limitArrivalDate.isPresent()) {
            var arglimitArrivalDate = LocalDate.parse(limitArrivalDate.get(), DateTimeFormatter.ISO_DATE).atTime(0, 0);
            foundOrders = orderRepository.findAllByCurrentStatusCodeAndArrivalDateLessThan(statusCode.toUpperCase(),
                    arglimitArrivalDate, pagination);
        } else {
            foundOrders = orderRepository.findAllByCurrentStatusCode(statusCode.toUpperCase(), pagination);
        }

        return orderMapper.pageRepositoryToPaginationResponse(foundOrders);
    }

    public OrderItemEntity findByCommerceItemIdAndSkuId(String commerceItemId, SkuItemResponse skuItemResponseDTO) {
        final Optional<OrderItemEntity> itemOptional = itemRepository.findByCommerceItemIdAndSkuId(commerceItemId, skuItemResponseDTO.getSkuId());

        if (itemOptional.isEmpty()) {
            OrderFlightErrorType errorType = OrderFlightErrorType.VALIDATION_COMMERCE_ITEM_ID_OR_ID_SKU_NOT_FOUND;
            throw new OrderFlightException(errorType, errorType.getTitle(), null);
        }

        return itemOptional.get();
    }

    public void updateOrderOnLiveloPartners(OrderEntity order, String oldStatus) {
        if (isSameStatus(order.getCurrentStatus().getCode(), oldStatus)) {
            return;
        }

        UpdateOrderDTO updateOrderDTO = liveloPartnersMapper.orderEntityToUpdateOrderDTO(order);
        liveloPartnersProxy.updateOrder(order.getId(), updateOrderDTO);
    }
    private Pageable pageRequestOf(Integer page, Integer rows) throws OrderFlightException {
        if (page <= 0) {
            throw new OrderFlightException(OrderFlightErrorType.VALIDATION_INVALID_PAGINATION,
                    OrderFlightErrorType.VALIDATION_INVALID_PAGINATION.getDescription(), null);
        }
        return PageRequest.of(page - 1, rows > orderProcessMaxRows ? orderProcessMaxRows : rows);
    }

}
