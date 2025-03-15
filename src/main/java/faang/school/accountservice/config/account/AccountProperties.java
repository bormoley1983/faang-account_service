package faang.school.accountservice.config.account;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "account")
public class AccountProperties {

    private Map<String, String> baseNumbers;

    public String getBaseNumber(String accountType) {
        return baseNumbers.getOrDefault(accountType, "0000");
    }
}