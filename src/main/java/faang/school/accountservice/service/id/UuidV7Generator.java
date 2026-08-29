package faang.school.accountservice.service.id;

import java.util.UUID;

@FunctionalInterface
public interface UuidV7Generator {

    UUID generate();
}
