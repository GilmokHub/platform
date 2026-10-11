package kr.gilmok.platform.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(PrometheusMetricsService.class)
class PrometheusMetricsServiceTest {

    @Autowired
    private PrometheusMetricsService prometheusMetricsService;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Prometheus에서 demo-backend의 5xx 에러율을 정상적으로 수집하고 파싱한다")
    void getCurrentErrorRate_Success() {
        // given
        String mockPrometheusResponse = """
                {
                  "status": "success",
                  "data": {
                    "resultType": "vector",
                    "result": [
                      {
                        "metric": { "job": "demo-backend" },
                        "value": [ 1708740000.000, "0.045" ]
                      }
                    ]
                  }
                }
                """;

        mockServer.expect(requestTo(startsWith("http://localhost:9090/api/v1/query")))
                .andExpect(requestTo(containsString("demo-backend")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mockPrometheusResponse, MediaType.APPLICATION_JSON));

        // when
        String errorRate = prometheusMetricsService.getCurrentErrorRate();

        // then
        assertThat(errorRate).isEqualTo("4.50%");
        mockServer.verify();
    }

    @Test
    @DisplayName("Prometheus에 결과 데이터가 없으면 0.00%를 반환한다")
    void getCurrentErrorRate_EmptyResult() {
        // given
        String mockPrometheusResponse = """
                {
                  "status": "success",
                  "data": {
                    "resultType": "vector",
                    "result": []
                  }
                }
                """;

        mockServer.expect(requestTo(startsWith("http://localhost:9090/api/v1/query")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mockPrometheusResponse, MediaType.APPLICATION_JSON));

        // when
        String errorRate = prometheusMetricsService.getCurrentErrorRate();

        // then
        assertThat(errorRate).isEqualTo("0.00%");
        mockServer.verify();
    }
}
