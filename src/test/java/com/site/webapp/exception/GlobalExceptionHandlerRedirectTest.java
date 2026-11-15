package com.site.webapp.exception;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Тесты защиты от Open Redirect в GlobalExceptionHandler.redirectToReferer().
 * Проверяется, что разрешены только относительные внутренние пути.
 */
class GlobalExceptionHandlerRedirectTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private String redirect(HttpServletRequestStub request) throws Exception {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        if (request.referer != null) {
            servletRequest.addHeader("Referer", request.referer);
        }
        Method method = GlobalExceptionHandler.class
                .getDeclaredMethod("redirectToReferer", jakarta.servlet.http.HttpServletRequest.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(handler, servletRequest, "/all-tasks");
    }

    private record HttpServletRequestStub(String referer) {
    }

    @Test
    void externalReferer_isRejected() throws Exception {
        assertEquals("redirect:/all-tasks", redirect(new HttpServletRequestStub("https://evil.com/phish")));
    }

    @Test
    void protocolRelativeReferer_isRejected() throws Exception {
        assertEquals("redirect:/all-tasks", redirect(new HttpServletRequestStub("//evil.com")));
    }

    @Test
    void javascriptScheme_isRejected() throws Exception {
        assertEquals("redirect:/all-tasks", redirect(new HttpServletRequestStub("javascript:alert(1)")));
    }

    @Test
    void backslashReferer_isRejected() throws Exception {
        assertEquals("redirect:/all-tasks", redirect(new HttpServletRequestStub("/\\evil.com")));
    }

    @Test
    void nullReferer_usesFallback() throws Exception {
        assertEquals("redirect:/all-tasks", redirect(new HttpServletRequestStub(null)));
    }

    @Test
    void emptyReferer_usesFallback() throws Exception {
        assertEquals("redirect:/all-tasks", redirect(new HttpServletRequestStub("")));
    }

    @Test
    void relativeReferer_isAllowed() throws Exception {
        assertEquals("redirect:/teams/1", redirect(new HttpServletRequestStub("/teams/1")));
    }
}