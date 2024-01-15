package br.com.livelo.orderflight.domain.mappers;

import br.com.livelo.orderflight.domain.dto.CartItem;
import br.com.livelo.orderflight.domain.dto.PartnerReservationItem;
import br.com.livelo.orderflight.domain.entity.OrderItemEntity;
import br.com.livelo.orderflight.domain.entity.OrderItemPriceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = CartItemPriceMapper.class)
public interface CartItemMapper {
    CartItemMapper INSTANCE = Mappers.getMapper(CartItemMapper.class);

    @Mapping(target = "commerceItemId", source = "cartItem.commerceItemId")
    @Mapping(target = "productId", source = "cartItem.productId")
    @Mapping(target = "quantity", source = "partnerReservationItem.quantity")
    @Mapping(target = "price", expression = "java(mapPrice(partnerReservationItem))")
    OrderItemEntity toOrderItemEntity(CartItem cartItem, PartnerReservationItem partnerReservationItem);


    default OrderItemPriceEntity mapPrice(PartnerReservationItem partnerReservationItem) {
        CartItemPriceMapper cartItemPriceMapper = Mappers.getMapper(CartItemPriceMapper.class);

        return cartItemPriceMapper.toOrderItemPriceEntity(partnerReservationItem);
    }


}