package br.com.livelo.orderflight.service.sku.impl;

import br.com.livelo.orderflight.constants.SkuConstant;
import br.com.livelo.orderflight.domain.dtos.sku.SkuItemResponse;
import br.com.livelo.orderflight.domain.entity.OrderItemEntity;
import br.com.livelo.orderflight.service.order.OrderService;
import br.com.livelo.orderflight.service.sku.SkuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkuServiceImpl implements SkuService {

    private final OrderService orderService;

    private final SkuConstant skuConstant;

    @Override
    public SkuItemResponse getSku(final String skuId, final String commerceItemId, final String currency){
        log.debug("SkuServiceImpl.getSku - start. [skuId]: {}, [commerceItemId]: {}, [currency]: {}", skuId, commerceItemId, currency);

        SkuItemResponse skuItemResponseDTOBase = buildSku(skuId);

        if(currencyAndCommerceItemIdIsPresent(currency, commerceItemId)){
            skuItemResponseDTOBase = enrichSkuWithCommerceItem(commerceItemId, skuItemResponseDTOBase);
            return skuItemResponseDTOBase;
        }

        return skuItemResponseDTOBase;
    }

    private Boolean currencyAndCommerceItemIdIsPresent(String currency, String commerceItemId){
        return StringUtils.hasText(commerceItemId) && StringUtils.hasText(currency);
    }

    private SkuItemResponse buildSku(String skuId){
        log.debug("SkuServiceImpl.buildSku - start [skuId]: {}, [skuConstants]: {}", skuId, skuConstant);

        return SkuItemResponse
                .builder()
                .skuId(skuId)
                .available(skuConstant.getAvailable())
                .salePrice(skuConstant.getSalePrice())
                .listPrice(skuConstant.getListPrice())
                .currency(skuConstant.getCurrency())
                .quantity(skuConstant.getQuantity())
                .description(skuConstant.getDescription() + " " + skuId + " SKU")
                .build();
    }

    private SkuItemResponse enrichSkuWithCommerceItem(String commerceItemId, SkuItemResponse skuItemResponseDTO){
        log.debug("SkuServiceImpl.enrichSkuWithCommerceItem - start commerceItemId and currency present start sku enrich process for [item]: {}", commerceItemId);

        OrderItemEntity orderItem = orderService.findByCommerceItemIdAndSkuId(commerceItemId, skuItemResponseDTO);

        double listPrice = orderItem.getPrice() != null && orderItem.getPrice().getPointsAmount() != null ? orderItem.getPrice().getPointsAmount().doubleValue() : skuConstant.getSalePrice();

        return skuItemResponseDTO
                .toBuilder()
                .commerceItemId(commerceItemId)
                .listPrice(listPrice)
                .salePrice(listPrice)
                .build();

    }

}
