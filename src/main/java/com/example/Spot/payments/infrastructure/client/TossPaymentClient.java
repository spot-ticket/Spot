package com.example.Spot.payments.infrastructure.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.Spot.payments.infrastructure.dto.TossPaymentResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TossPaymentClient {

  private final RestTemplate restTemplate;

  @Value("${toss.payments.secret-key}")
  private String secretKey;

  @Value("${toss.payments.base-url:https://api.tosspayments.com}")
  private String baseUrl;

  public TossPaymentResponse requestBillingPayment(
      String billingKey,
      Long amount,
      String orderId,
      String orderName,
      String customerKey,
      Integer timeout) {
    String url = baseUrl + "/v1/billing/" + billingKey;

    Map<String, Object> requestBody =
        Map.of(
            "amount", amount,
            "orderId", orderId,
            "orderName", orderName,
            "customerKey", customerKey);

    HttpHeaders headers = createHeaders();
    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

    try {
      ResponseEntity<TossPaymentResponse> response =
          restTemplate.postForEntity(url, request, TossPaymentResponse.class);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      throw new RuntimeException("자동결제 실패: " + e.getMessage());
    }
  }

  public TossPaymentResponse cancelPayment(
      String paymentKey, String cancelReason, Integer timeout) {
    String url = baseUrl + "/v1/payments/" + paymentKey + "/cancel";

    Map<String, Object> requestBody = Map.of("cancelReason", cancelReason);

    HttpHeaders headers = createHeaders();
    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

    try {
      ResponseEntity<TossPaymentResponse> response =
          restTemplate.postForEntity(url, request, TossPaymentResponse.class);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      throw new RuntimeException("결제 취소 실패: " + e.getMessage());
    }
  }

  public TossPaymentResponse cancelPaymentPartial(
      String paymentKey, Long cancelAmount, String cancelReason) {
    String url = baseUrl + "/v1/payments/" + paymentKey + "/cancel";

    Map<String, Object> requestBody =
        Map.of(
            "cancelAmount", cancelAmount,
            "cancelReason", cancelReason);

    HttpHeaders headers = createHeaders();
    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

    try {
      ResponseEntity<TossPaymentResponse> response =
          restTemplate.postForEntity(url, request, TossPaymentResponse.class);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      throw new RuntimeException("결제 부분 취소 실패: " + e.getMessage());
    }
  }

  private HttpHeaders createHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBasicAuth(secretKey, "");
    return headers;
  }
}
