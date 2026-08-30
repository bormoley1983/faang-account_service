package faang.school.accountservice.config.context;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserHeaderFilterTest {

    @Mock
    private FilterChain chain;

    private UserContext userContext;
    private UserHeaderFilter filter;

    @BeforeEach
    void setUp() {
        userContext = new UserContext();
        filter = new UserHeaderFilter(userContext);
    }

    @AfterEach
    void tearDown() {
        userContext.clear();
    }

    @Test
    void doFilter_whenHeaderPresent_setsUserContextDuringChainAndClearsAfterwards() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-user-id", "42");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Long[] observedDuringChain = new Long[1];
        doAnswer(invocation -> {
            observedDuringChain[0] = userContext.getUserId();
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(request, response, chain);

        assertThat(observedDuringChain[0]).isEqualTo(42L);
        // Context must be cleared after the chain completes.
        assertThat(userContext.getUserId()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_whenHeaderAbsent_leavesUserContextNull() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Long[] observedDuringChain = new Long[1];
        doAnswer(invocation -> {
            observedDuringChain[0] = userContext.getUserId();
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(request, response, chain);

        assertThat(observedDuringChain[0]).isNull();
    }

    @Test
    void doFilter_whenChainThrows_stillClearsUserContext() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-user-id", "7");
        MockHttpServletResponse response = new MockHttpServletResponse();

        org.mockito.Mockito.doThrow(new ServletException("boom"))
                .when(chain).doFilter(any(), any());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> filter.doFilter(request, response, chain))
                .isInstanceOf(ServletException.class);

        assertThat(userContext.getUserId()).isNull();
    }
}
