package faang.school.accountservice.client;

import faang.school.accountservice.config.context.UserContext;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class FeignUserInterceptorTest {

    private UserContext userContext;
    private FeignUserInterceptor interceptor;

    @BeforeEach
    void setUp() {
        userContext = new UserContext();
        interceptor = new FeignUserInterceptor(userContext);
    }

    @AfterEach
    void tearDown() {
        userContext.clear();
    }

    @Test
    void apply_whenUserIdPresent_setsHeaderOnTemplate() {
        userContext.setUserId(42L);
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        Collection<String> values = template.headers().get("x-user-id");
        assertThat(values).containsExactly("42");
    }

    @Test
    void apply_whenNoUserId_setsLiteralNullHeader() {
        // Production behavior: String.valueOf(null) yields the literal "null" string.
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        Collection<String> values = template.headers().get("x-user-id");
        assertThat(values).containsExactly("null");
    }
}
