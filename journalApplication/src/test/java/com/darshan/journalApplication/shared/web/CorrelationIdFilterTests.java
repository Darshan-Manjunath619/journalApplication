package com.darshan.journalApplication.shared.web;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.*;
import org.slf4j.MDC;
import org.springframework.mock.web.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CorrelationIdFilterTests {
    CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test void preservesValidClientCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HEADER, "client-request_123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request, response, chain);
        assertEquals("client-request_123", response.getHeader(CorrelationIdFilter.HEADER));
        assertEquals("client-request_123", request.getAttribute(CorrelationIdFilter.ATTRIBUTE));
        assertNull(MDC.get(CorrelationIdFilter.MDC_KEY));
    }

    @Test void replacesUnsafeCorrelationIdAndCleansMdcAfterFailure() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HEADER, "unsafe value with spaces");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        doThrow(new RuntimeException("boom")).when(chain).doFilter(any(), any());
        assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, chain));
        String generated = response.getHeader(CorrelationIdFilter.HEADER);
        assertNotNull(generated);
        assertNotEquals("unsafe value with spaces", generated);
        assertNull(MDC.get(CorrelationIdFilter.MDC_KEY));
    }
}
