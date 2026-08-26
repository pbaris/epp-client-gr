package gr.netmechanics.epp.client;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;

@RequiredArgsConstructor
public class EppCookieInterceptor implements ClientHttpRequestInterceptor {

    private final EppSessionCookieStore cookieStore;

    @Override
    @NonNull
    public ClientHttpResponse intercept(
        @NonNull final HttpRequest request,
        @NonNull final byte[] body,
        @NonNull final ClientHttpRequestExecution execution) throws IOException {

        String cookie = cookieStore.get();
        if (cookie != null) {
            request.getHeaders().add(HttpHeaders.COOKIE, cookie);
        }

        ClientHttpResponse response = execution.execute(request, body);

        List<String> setCookieHeaders = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (setCookieHeaders != null && !setCookieHeaders.isEmpty()) {
            cookieStore.set(toCookieHeader(setCookieHeaders));
        }

        return response;
    }

    private static String toCookieHeader(final List<String> setCookieHeaders) {
        return setCookieHeaders.stream()
            .map(header -> header.split(";", 2)[0].trim())
            .collect(Collectors.joining("; "));
    }
}
