package gr.netmechanics.epp.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

class EppCookieInterceptorTest {

    @Test
    void strips_set_cookie_attributes_and_stores_only_name_value_pair() throws Exception {
        EppSessionCookieStore cookieStore = new EppSessionCookieStore();
        EppCookieInterceptor interceptor = new EppCookieInterceptor(cookieStore);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.put(HttpHeaders.SET_COOKIE, List.of("JSESSIONID=abc123; Path=/; HttpOnly; SameSite=Lax"));

        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getHeaders()).thenReturn(responseHeaders);

        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(any(), any())).thenReturn(response);

        HttpRequest request = mock(HttpRequest.class);
        when(request.getHeaders()).thenReturn(new HttpHeaders());

        interceptor.intercept(request, new byte[0], execution);

        assertThat(cookieStore.get()).isEqualTo("JSESSIONID=abc123");
    }

    @Test
    void sends_stored_cookie_as_cookie_header_on_next_request() throws Exception {
        EppSessionCookieStore cookieStore = new EppSessionCookieStore();
        cookieStore.set("JSESSIONID=abc123");
        EppCookieInterceptor interceptor = new EppCookieInterceptor(cookieStore);

        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getHeaders()).thenReturn(new HttpHeaders());

        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(any(), any())).thenReturn(response);

        HttpHeaders requestHeaders = new HttpHeaders();
        HttpRequest request = mock(HttpRequest.class);
        when(request.getHeaders()).thenReturn(requestHeaders);

        interceptor.intercept(request, new byte[0], execution);

        assertThat(requestHeaders.get(HttpHeaders.COOKIE)).containsExactly("JSESSIONID=abc123");
    }

    @Test
    void does_not_add_cookie_header_when_store_is_empty() throws Exception {
        EppSessionCookieStore cookieStore = new EppSessionCookieStore();
        EppCookieInterceptor interceptor = new EppCookieInterceptor(cookieStore);

        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(response.getHeaders()).thenReturn(new HttpHeaders());

        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(any(), any())).thenReturn(response);

        HttpHeaders requestHeaders = new HttpHeaders();
        HttpRequest request = mock(HttpRequest.class);
        when(request.getHeaders()).thenReturn(requestHeaders);

        interceptor.intercept(request, new byte[0], execution);

        assertThat(requestHeaders.get(HttpHeaders.COOKIE)).isNull();
    }
}
