package softwave.backend.backend_mobile;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SoftWaveFinanceApplicationTests {

    @LocalServerPort
    int port;

    private String base() {
        return "http://127.0.0.1:" + port;
    }

    @Test
    void contextLoads() {
    }

    @Test
    void internalApiRequerToken() throws Exception {
        HttpClient c = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(base() + "/internal/v1/tenants/1/transacoes/resumo?dataInicio=2026-01-01&dataFim=2026-01-31"))
                .GET()
                .build();
        HttpResponse<String> res = c.send(req, HttpResponse.BodyHandlers.ofString());
        assertThat(res.statusCode()).isEqualTo(401);
    }

    @Test
    void internalApiComTokenSemTenantRetorna404() throws Exception {
        HttpClient c = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(base() + "/internal/v1/tenants/99999/transacoes/resumo?dataInicio=2026-01-01&dataFim=2026-01-31"))
                .header("X-Internal-Token", "test-internal-token-for-ci")
                .GET()
                .build();
        HttpResponse<String> res = c.send(req, HttpResponse.BodyHandlers.ofString());
        assertThat(res.statusCode()).isEqualTo(404);
    }

    @Test
    void healthPublico() throws Exception {
        HttpClient c = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(base() + "/v1/health"))
                .GET()
                .build();
        HttpResponse<String> res = c.send(req, HttpResponse.BodyHandlers.ofString());
        assertThat(res.statusCode()).isEqualTo(200);
    }
}
