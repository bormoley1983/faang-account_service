package faang.school.accountservice.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "free_account_numbers")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class FreeAccountNumber {

    @EmbeddedId
    private FreeAccountId id;
}