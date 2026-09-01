package faang.school.accountservice.model;

import faang.school.accountservice.enums.AccountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FreeAccountIdTest {

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("two instances with the same type and number are equal")
        void sameTypeAndNumber_areEqual() {
            // Arrange
            FreeAccountId a = new FreeAccountId(AccountType.DEBIT, 4200000000000001L);
            FreeAccountId b = new FreeAccountId(AccountType.DEBIT, 4200000000000001L);

            // Act / Assert
            assertThat(a).isEqualTo(b);
            assertThat(b).isEqualTo(a);
        }

        @Test
        @DisplayName("an instance is equal to itself")
        void sameInstance_isEqualToItself() {
            FreeAccountId a = new FreeAccountId(AccountType.SAVINGS, 5536000000000100L);

            assertThat(a).isEqualTo(a);
        }

        @Test
        @DisplayName("different account numbers are not equal")
        void differentNumber_notEqual() {
            FreeAccountId a = new FreeAccountId(AccountType.DEBIT, 4200000000000001L);
            FreeAccountId b = new FreeAccountId(AccountType.DEBIT, 4200000000000002L);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("different account types are not equal")
        void differentType_notEqual() {
            FreeAccountId a = new FreeAccountId(AccountType.DEBIT, 4200000000000001L);
            FreeAccountId b = new FreeAccountId(AccountType.SAVINGS, 4200000000000001L);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("null is not equal to an instance")
        void null_notEqual() {
            FreeAccountId a = new FreeAccountId(AccountType.DEBIT, 4200000000000001L);

            assertThat(a).isNotEqualTo(null);
        }

        @Test
        @DisplayName("an unrelated object is not equal to an instance")
        void otherType_notEqual() {
            FreeAccountId a = new FreeAccountId(AccountType.DEBIT, 4200000000000001L);

            assertThat(a).isNotEqualTo("not-a-free-account-id");
        }
    }

    @Nested
    @DisplayName("Hash code")
    class HashCode {

        @Test
        @DisplayName("equal instances have the same hash code")
        void equalInstances_sameHashCode() {
            FreeAccountId a = new FreeAccountId(AccountType.DEBIT, 4200000000000001L);
            FreeAccountId b = new FreeAccountId(AccountType.DEBIT, 4200000000000001L);

            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("hash code is stable across repeated calls")
        void hashCode_isStable() {
            FreeAccountId a = new FreeAccountId(AccountType.SAVINGS, 5536000000000100L);

            assertThat(a.hashCode()).isEqualTo(a.hashCode());
        }
    }

    @Nested
    @DisplayName("Accessors")
    class Accessors {

        @Test
        @DisplayName("getters expose the identifier fields")
        void getters_exposeFields() {
            FreeAccountId id = new FreeAccountId(AccountType.DEBIT, 4200000000000001L);

            assertThat(id.getType()).isEqualTo(AccountType.DEBIT);
            assertThat(id.getAccountNumber()).isEqualTo(4200000000000001L);
        }

        @Test
        @DisplayName("setters update the identifier fields")
        void setters_updateFields() {
            FreeAccountId id = new FreeAccountId(AccountType.DEBIT, 4200000000000001L);

            id.setType(AccountType.SAVINGS);
            id.setAccountNumber(5536000000000100L);

            assertThat(id.getType()).isEqualTo(AccountType.SAVINGS);
            assertThat(id.getAccountNumber()).isEqualTo(5536000000000100L);
        }

        @Test
        @DisplayName("no-args constructor yields a blank identifier")
        void noArgsConstructor_yieldsBlankInstance() {
            FreeAccountId id = new FreeAccountId();

            assertThat(id.getType()).isNull();
            assertThat(id.getAccountNumber()).isZero();
        }
    }
}
