package br.com.livelo.orderflight.proxies;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.livelo.orderflight.client.PartnerConnectorClient;
import br.com.livelo.orderflight.domain.dtos.connector.request.ConnectorConfirmOrderRequest;
import br.com.livelo.orderflight.domain.dtos.connector.response.ConnectorConfirmOrderResponse;
import br.com.livelo.orderflight.exception.OrderFlightException;
import br.com.livelo.orderflight.mock.MockBuilder;
import br.com.livelo.partnersconfigflightlibrary.dto.WebhookDTO;
import br.com.livelo.partnersconfigflightlibrary.services.impl.PartnersConfigServiceImpl;
import br.com.livelo.partnersconfigflightlibrary.utils.Webhooks;
import feign.FeignException;

@ExtendWith(MockitoExtension.class)
class ConnectorPartnersProxyTest {

  @Mock
  private PartnersConfigServiceImpl partnersConfigService;
  @Mock
  private PartnerConnectorClient partnerConnectorClient;
  @Mock
  private ObjectMapper objectMapper;

  @InjectMocks
  private ConnectorPartnersProxy proxy;

  @BeforeEach
  void beforeEach() {
    WebhookDTO webhook = WebhookDTO.builder().connectorUrl("http://url-mock.com").name("name").build();
    when(partnersConfigService.getPartnerWebhook(anyString(),
        any(Webhooks.class)))
        .thenReturn(webhook);
  }

  @Test
  void shouldReturnConfirmOnPartner() throws Exception {
    ResponseEntity<ConnectorConfirmOrderResponse> confirmOrderResponse = MockBuilder.connectorConfirmOrderResponse();
    when(partnerConnectorClient.confirmOrder(any(URI.class), any(ConnectorConfirmOrderRequest.class)))
        .thenReturn(confirmOrderResponse);

    ConnectorConfirmOrderResponse response = proxy.confirmOnPartner("CVC", MockBuilder.connectorConfirmOrderRequest());

    assertEquals(confirmOrderResponse.getBody(), response);
    assertEquals(200, confirmOrderResponse.getStatusCode().value());
    verify(partnerConnectorClient).confirmOrder(any(URI.class), any(ConnectorConfirmOrderRequest.class));
    verifyNoMoreInteractions(partnerConnectorClient);
  }

  @Test
  void shouldReturnFailedWhenCatchFeignException() throws OrderFlightException, JsonProcessingException {
    FeignException mockException = Mockito.mock(FeignException.class);
    when(mockException.contentUTF8())
        .thenReturn(
            new String(MockBuilder.connectorConfirmOrderResponse().getBody().toString().getBytes(),
                StandardCharsets.UTF_8));

    when(objectMapper.readValue(anyString(), eq(ConnectorConfirmOrderResponse.class)))
        .thenReturn(MockBuilder.connectorConfirmOrderResponse().getBody());

    when(partnerConnectorClient.confirmOrder(any(URI.class),
        any(ConnectorConfirmOrderRequest.class)))
        .thenThrow(mockException);

    ConnectorConfirmOrderResponse response = proxy.confirmOnPartner("CVC",
        MockBuilder.connectorConfirmOrderRequest());

    assertEquals(MockBuilder.connectorConfirmOrderResponse().getBody(),
        response);
  }

  @Test
  void shouldThrowException() {
    Exception exception = assertThrows(Exception.class, () -> {
      proxy.confirmOnPartner("CVC",
          MockBuilder.connectorConfirmOrderRequest());
    });

    assertTrue(exception.getMessage().contains("Cannot invoke"));
  }
}
