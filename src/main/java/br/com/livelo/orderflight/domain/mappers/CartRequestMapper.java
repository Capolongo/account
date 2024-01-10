package br.com.livelo.orderflight.domain.mappers;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.com.livelo.orderflight.domain.dto.CartItem;
import br.com.livelo.orderflight.domain.dto.CartRequest;
import br.com.livelo.orderflight.domain.dto.PartnerReservationRequest;
import br.com.livelo.orderflight.domain.dto.PartnerReservationResponse;
import br.com.livelo.orderflight.domain.entity.OrderEntity;
import br.com.livelo.orderflight.domain.entity.OrderItemEntity;
import br.com.livelo.orderflight.domain.entity.OrderItemPriceEntity;
import br.com.livelo.orderflight.domain.entity.OrderPriceDescriptionEntity;
import br.com.livelo.orderflight.domain.entity.OrderPriceEntity;
import lombok.AllArgsConstructor;

//TODO UTILIZAR O MAPSTRUCT PARA CRIAÇÃO DOS MAPPERS
@Component
@AllArgsConstructor
public class CartRequestMapper {

	private final CartPaxMapper cartPaxMapper;
	
	public PartnerReservationRequest toPartnerReservationRequest(CartRequest cartRequest) {
		return PartnerReservationRequest.builder()
				.partnerCode(cartRequest.getPartnerCode())
				.segmentsPartnerIds(cartRequest.getSegmentsPartnerIds())
				.paxs(cartPaxMapper.toListPartnerReservationPax(cartRequest.getPaxs()))
				.build();

	}
	
	public OrderEntity toOrderEntity(CartRequest cartRequest, String transacationId, String customerId, String channel, PartnerReservationResponse partnerReservationResponse) {
		return OrderEntity.builder()
				.commerceOrderId(cartRequest.getCommerceOrderId())
				.partnerOrderId(cartRequest.getPartnerOrderId())
				.partnerCode(cartRequest.getPartnerCode())
				.channel(channel)
				.customerIdentifier(customerId)
				.transactionId(transacationId)
				.expirationDate(partnerReservationResponse.getExpirationDate())
				.price(OrderPriceEntity.builder()
						.amount(partnerReservationResponse.getAmount())
						.ordersPriceDescription(
								partnerReservationResponse.getOrdersPriceDescription()
									.stream()
									.map(ordersPriceDescription -> OrderPriceDescriptionEntity.builder()
											.amount(ordersPriceDescription.getAmount())
											.type(ordersPriceDescription.getType())
											.description(ordersPriceDescription.getDescription())
											.build())
									.collect(Collectors.toSet()))
						.build())
				.items(partnerReservationResponse.getItems()
						.stream()
						.map(itemPartnerReservationResponse->{
							Optional<CartItem> optionalCartItem = cartRequest.getItems().stream()
					                .filter(cartItem -> itemPartnerReservationResponse.getType().equals(cartItem.getProductType()))
					                .findFirst();
							
							return OrderItemEntity.builder()
									.commerceItemId(optionalCartItem.get().getCommerceItemId())
									.skuId(optionalCartItem.get().getSkuId())
									.productId(optionalCartItem.get().getProductId())
									.quantity(itemPartnerReservationResponse.getQuantity())
									.price(OrderItemPriceEntity.builder()
											.listPrice(String.valueOf(itemPartnerReservationResponse.getListPrice()))
											.amount(itemPartnerReservationResponse.getAmount())
											.build())
									.travelInfo(null)
									.segments(null)
									.build();
						})
						.collect(Collectors.toSet()))
				.build();
	}
}
