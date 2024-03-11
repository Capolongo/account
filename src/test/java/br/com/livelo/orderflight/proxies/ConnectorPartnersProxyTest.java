package br.com.livelo.orderflight.proxies;

import br.com.livelo.exceptions.WebhookException;
import br.com.livelo.orderflight.client.PartnerConnectorClient;
import br.com.livelo.orderflight.domain.dto.reservation.request.PartnerReservationRequest;
import br.com.livelo.orderflight.domain.dto.reservation.response.PartnerReservationResponse;
import br.com.livelo.orderflight.domain.dtos.connector.request.ConnectorConfirmOrderRequest;
import br.com.livelo.orderflight.domain.dtos.connector.response.ConnectorConfirmOrderResponse;
import br.com.livelo.orderflight.domain.dtos.connector.response.ConnectorReservationResponse;
import br.com.livelo.orderflight.exception.ConnectorReservationBusinessException;
import br.com.livelo.orderflight.exception.ConnectorReservationInternalException;
import br.com.livelo.orderflight.exception.OrderFlightException;
import br.com.livelo.orderflight.exception.enuns.OrderFlightErrorType;
import br.com.livelo.orderflight.mock.MockBuilder;
import br.com.livelo.partnersconfigflightlibrary.dto.WebhookDTO;
import br.com.livelo.partnersconfigflightlibrary.services.impl.PartnersConfigServiceImpl;
import br.com.livelo.partnersconfigflightlibrary.utils.Webhooks;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static br.com.livelo.orderflight.exception.enuns.OrderFlightErrorType.*;
import static br.com.livelo.partnersconfigflightlibrary.utils.ErrorsType.PARTNER_INACTIVE;
import static br.com.livelo.partnersconfigflightlibrary.utils.ErrorsType.UNKNOWN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    private void setup() {
        WebhookDTO webhook = WebhookDTO.builder().connectorUrl("http://url-mock.com").name("name").build();
        when(partnersConfigService.getPartnerWebhook(anyString(), any(Webhooks.class))).thenReturn(webhook);
    }

    @Test
    void shouldReturnConfirmOnPartner() {
        ResponseEntity<ConnectorConfirmOrderResponse> confirmOrderResponse = MockBuilder.connectorConfirmOrderResponse();
        setup();
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
        setup();
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

    @Test
    void shouldThrowOrderFlightExceptionOnConfirmOnPartner() throws OrderFlightException, JsonProcessingException {
        FeignException mockException = Mockito.mock(FeignException.class);
        when(mockException.contentUTF8())
                .thenReturn(
                        new String(MockBuilder.connectorConfirmOrderResponse().getBody().toString().getBytes(),
                                StandardCharsets.UTF_8));

        var mock = MockBuilder.connectorConfirmOrderResponse().getBody();
        mock.setCurrentStatus(null);

        when(objectMapper.readValue(anyString(), eq(ConnectorConfirmOrderResponse.class)))
                .thenReturn(mock);

        when(partnerConnectorClient.confirmOrder(any(URI.class),
                any(ConnectorConfirmOrderRequest.class)))
                .thenThrow(mockException);

        when(partnersConfigService.getPartnerWebhook("CVC", Webhooks.CONFIRMATION))
                .thenReturn(WebhookDTO.builder().connectorUrl("www").build());

        try {
            proxy.confirmOnPartner("CVC", MockBuilder.connectorConfirmOrderRequest());
        } catch (OrderFlightException exception) {
            assertEquals(OrderFlightErrorType.ORDER_FLIGHT_CONNECTOR_INTERNAL_ERROR, exception.getOrderFlightErrorType());
        }
    }

    @Test
    void shouldMakeReservation() {
        var request = mock(PartnerReservationRequest.class);
        var response = mock(PartnerReservationResponse.class);
        setup();

        when(request.getPartnerCode()).thenReturn("cvc");
        when(partnerConnectorClient.createReserve(any(), any(), any()))
                .thenReturn(ResponseEntity.ok(response));

        PartnerReservationResponse result = proxy.createReserve(request, "transactionID");
        assertNotNull(result);
    }

    @Test
    void shouldThrowsException_WhenFeignResponse5xxStatus() {
        var request = mock(PartnerReservationRequest.class);
        setup();
        var feignException = makeFeignMockExceptionWithStatus(500);
        makeException(request, feignException);

        var exception = assertThrows(ConnectorReservationInternalException.class,
                () -> proxy.createReserve(request, "transactionId"));

        assertEquals(OrderFlightErrorType.ORDER_FLIGHT_CONNECTOR_INTERNAL_ERROR, exception.getOrderFlightErrorType());
    }

    @Test
    void shouldThrowsException_WhenFeignExceptionResponseIsDifferentOf5xxxStatus() {
        var request = mock(PartnerReservationRequest.class);
        var feignException = makeFeignMockExceptionWithStatus(400);
        setup();
        makeException(request, feignException);

        var exception = assertThrows(ConnectorReservationBusinessException.class,
                () -> proxy.createReserve(request, "transactionId"));

        assertEquals(OrderFlightErrorType.ORDER_FLIGHT_CONNECTOR_BUSINESS_ERROR, exception.getOrderFlightErrorType());
    }

    @Test
    void shouldThrowsOrderFlightException_WhenFeignExceptionResponseIsDifferentOf5xxxStatus() {
        var request = mock(PartnerReservationRequest.class);
        setup();
        when(request.getPartnerCode()).thenReturn("cvc");
        doThrow(OrderFlightException.class).when(partnerConnectorClient).createReserve(any(), any(), anyString());

        assertThrows(OrderFlightException.class, () -> proxy.createReserve(request, "transactionId"));
    }

    @Test
    void shouldThrowException_WhenFeignReturnSomethingWrong() {
        var request = mock(PartnerReservationRequest.class);
        setup();
        when(partnerConnectorClient.createReserve(any(), any(), any())).thenThrow(new RuntimeException("Simulated internal error"));

        assertThrows(OrderFlightException.class,
                () -> proxy.createReserve(mock(PartnerReservationRequest.class), "transactionId"));

        var exception = assertThrows(OrderFlightException.class,
                () -> proxy.createReserve(request, "transactionId"));

        assertEquals(ORDER_FLIGHT_INTERNAL_ERROR, exception.getOrderFlightErrorType());
    }

    @Test
    void shouldThrowFlightConnectorBusinessError_WhenThereIsBadRequest() {
        var request = mock(PartnerReservationRequest.class);
        var feignException = makeFeignMockExceptionWithStatus(400);

        WebhookDTO webhook = WebhookDTO.builder().connectorUrl("http://url-mock.com").name("name").build();
        when(partnersConfigService.getPartnerWebhook(anyString(),
                any(Webhooks.class)))
                .thenReturn(webhook);
        makeException(request, feignException);

        var exception = assertThrows(ConnectorReservationBusinessException.class,
                () -> proxy.createReserve(request, "transactionId"));

        assertEquals(OrderFlightErrorType.ORDER_FLIGHT_CONNECTOR_BUSINESS_ERROR, exception.getOrderFlightErrorType());
    }

    @Test
    void shouldThrowFlightConnectorInternalError_WhenThereIsSomeInternalError() {
        var request = mock(PartnerReservationRequest.class);
        var feignException = makeFeignMockExceptionWithStatus(400);
        makeException(request, feignException);
        setup();
        var exception = assertThrows(OrderFlightException.class,
                () -> proxy.createReserve(request, "transactionId"));

        assertEquals(OrderFlightErrorType.ORDER_FLIGHT_CONNECTOR_BUSINESS_ERROR, exception.getOrderFlightErrorType());

    }

    @Test
    void shouldThrowInternalErrorException_WhenCallPartnerWebhookErrorTypeIsUnknown() {
        var request = mock(PartnerReservationRequest.class);
        var exceptionExpected = mock(WebhookException.class);
        doThrow(exceptionExpected).when(partnersConfigService).getPartnerWebhook(any(), any());

        when(exceptionExpected.getError()).thenReturn(UNKNOWN);

        var exception = assertThrows(OrderFlightException.class,
                () -> proxy.createReserve(request, "transactionId"));

        assertEquals(OrderFlightErrorType.ORDER_FLIGHT_CONNECTOR_INTERNAL_ERROR, exception.getOrderFlightErrorType());
    }

    @Test
    void shouldThrowBusinessErrorException_WhenCallPartnerInactive() {
        var request = mock(PartnerReservationRequest.class);
        var exceptionExpected = mock(WebhookException.class);

        doThrow(exceptionExpected).when(partnersConfigService).getPartnerWebhook(any(), any());

        when(exceptionExpected.getError()).thenReturn(PARTNER_INACTIVE);

        var exception = assertThrows(OrderFlightException.class,
                () -> proxy.createReserve(request, "transactionId"));

        assertEquals(OrderFlightErrorType.ORDER_FLIGHT_CONNECTOR_BUSINESS_ERROR, exception.getOrderFlightErrorType());
    }

    @Test
    void shouldGetReservation() {
        var expected = mock(PartnerReservationResponse.class);

        WebhookDTO webhook = WebhookDTO.builder().connectorUrl("http://url-mock.com").name("name").build();
        when(partnersConfigService.getPartnerWebhook(anyString(),
                any(Webhooks.class)))
                .thenReturn(webhook);
        when(partnerConnectorClient.getReservation(any(), any(), any(), any()))
                .thenReturn(ResponseEntity.ok(expected));
        var response = this.proxy.getReservation("123", "123", "123", List.of("asdf", "fdsa"));
        assertEquals(expected, response);
    }

    @Test
    void shouldThrowInternalError_WhenGetReservation() {
        var segmentsPartnerIds = List.of("asdf", "fdsa");
        var exceptionExpected = new WebhookException(UNKNOWN, "");

        doThrow(exceptionExpected).when(partnersConfigService).getPartnerWebhook(any(), any());
        var exception = assertThrows(OrderFlightException.class,
                () -> this.proxy.getReservation("123", "123", "123", segmentsPartnerIds));
        assertEquals(ORDER_FLIGHT_CONNECTOR_INTERNAL_ERROR, exception.getOrderFlightErrorType());
    }

    @Test
    void shouldThrowBusinessError_WhenGetReservation() {
        var segmentsPartnerIds = List.of("asdf", "fdsa");
        var exceptionExpected = new WebhookException(PARTNER_INACTIVE, "");

        doThrow(exceptionExpected).when(partnersConfigService).getPartnerWebhook(any(), any());
        var exception = assertThrows(OrderFlightException.class,
                () -> this.proxy.getReservation("123", "123", "123", segmentsPartnerIds));
        assertEquals(ORDER_FLIGHT_CONNECTOR_BUSINESS_ERROR, exception.getOrderFlightErrorType());
    }

    @Test
    void shouldThrowInternalError_WhenStatus400() {
        var segmentsPartnerIds = List.of("asdf", "fdsa");
        var exceptionExpected = mock(FeignException.class);

        doReturn(400).when(exceptionExpected).status();
        doThrow(exceptionExpected).when(partnersConfigService).getPartnerWebhook(any(), any());
        var exception = assertThrows(OrderFlightException.class,
                () -> this.proxy.getReservation("123", "123", "123", segmentsPartnerIds));
        assertEquals(ORDER_FLIGHT_CONNECTOR_BUSINESS_ERROR, exception.getOrderFlightErrorType());
    }

    @Test
    void shouldThrowInternalError_WhenStatus500() {
        var segmentsPartnerIds = List.of("asdf", "fdsa");
        var exceptionExpected = mock(FeignException.class);

        doReturn(500).when(exceptionExpected).status();
        doThrow(exceptionExpected).when(partnersConfigService).getPartnerWebhook(any(), any());
        var exception = assertThrows(OrderFlightException.class,
                () -> this.proxy.getReservation("123", "123", "123", segmentsPartnerIds));
        assertEquals(ORDER_FLIGHT_CONNECTOR_INTERNAL_ERROR, exception.getOrderFlightErrorType());
    }

    @Test
    void shouldThrowInternalError_WhenUnknownError() {
        var segmentsPartnerIds = List.of("asdf", "fdsa");

        var exception = assertThrows(OrderFlightException.class,
                () -> this.proxy.getReservation("123", "123", "123", segmentsPartnerIds));
        assertEquals(ORDER_FLIGHT_INTERNAL_ERROR, exception.getOrderFlightErrorType());
    }

    private void makeException(PartnerReservationRequest partnerReservationRequest, FeignException feignException) {
        when(partnerReservationRequest.getPartnerCode()).thenReturn("cvc");
        when(partnerConnectorClient.createReserve(any(), any(), any())).thenThrow(feignException);
    }

    private FeignException makeFeignMockExceptionWithStatus(Integer statusCode) {
        var feignException = Mockito.mock(FeignException.class);
        Mockito.when(feignException.status()).thenReturn(statusCode);
        return feignException;
    }

    @Test
    void shouldReturnGetConfirmationOnPartner() throws Exception {
        ResponseEntity<ConnectorConfirmOrderResponse> confirmOrderResponse = MockBuilder.connectorConfirmOrderResponse();
        setup();
        when(partnerConnectorClient.getConfirmation(any(URI.class))).thenReturn(confirmOrderResponse);

        ConnectorConfirmOrderResponse response = proxy.getConfirmationOnPartner("CVC", "10071014", "lf123");

        assertEquals(confirmOrderResponse.getBody(), response);
        assertEquals(200, confirmOrderResponse.getStatusCode().value());
        verify(partnerConnectorClient).getConfirmation(any(URI.class));
        verifyNoMoreInteractions(partnerConnectorClient);
    }

    @Test
    void shouldThrowExceptionGetConfirmationOnPartner() {
        setup();
        when(partnerConnectorClient.getConfirmation(any(URI.class))).thenThrow(FeignException.class);

        assertThrows(OrderFlightException.class, () -> {
            proxy.getConfirmationOnPartner("CVC", "10071014", "lf123");
        });
    }

    @Test
    void shouldReturnGetVoucherOnPartner() throws Exception {
        ResponseEntity<ConnectorConfirmOrderResponse> voucherResponse = MockBuilder.connectorVoucherResponse();
        setup();
        when(partnerConnectorClient.getVoucher(any(URI.class))).thenReturn(voucherResponse);

        ConnectorConfirmOrderResponse response = proxy.getVoucherOnPartner("CVC", "partnerOrderId", "orderId");

        assertEquals(voucherResponse.getBody(), response);
        assertEquals(200, voucherResponse.getStatusCode().value());
        verify(partnerConnectorClient).getVoucher(any(URI.class));
        verifyNoMoreInteractions(partnerConnectorClient);
    }

    @Test
    void shouldThrowExceptionGetVoucherOnPartner() {
        setup();
        when(partnerConnectorClient.getVoucher(any(URI.class))).thenThrow(FeignException.class);

        assertThrows(OrderFlightException.class, () -> {
            proxy.getVoucherOnPartner("CVC", "10071014", "lf123");
        });
    }
}
