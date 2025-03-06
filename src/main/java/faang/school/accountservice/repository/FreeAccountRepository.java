package faang.school.accountservice.repository;

import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.model.FreeAccountId;
import faang.school.accountservice.model.FreeAccountNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FreeAccountRepository extends JpaRepository<FreeAccountNumber, FreeAccountId> {

    @Query(value = """
                DELETE FROM free_account_numbers WHERE type = :type AND account_number = (
                SELECT account_number FROM free_account_numbers WHERE type = :type LIMIT 1
                ) RETURNING type, account_number
            """, nativeQuery = true)
    FreeAccountNumber retrieveFirst(String type);

    @Query("SELECT COUNT(f) FROM FreeAccountNumber f WHERE f.id.type = :type")
    int countByType(AccountType type);
}