package faang.school.accountservice.repository;

import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.model.FreeAccountId;
import faang.school.accountservice.model.FreeAccountNumber;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FreeAccountRepository extends JpaRepository<FreeAccountNumber, FreeAccountId> {

    @Query(value = """
            DELETE FROM free_account_numbers
            WHERE type = :type
            AND account_number = (
                SELECT account_number FROM free_account_numbers
                WHERE type = :type
                ORDER BY account_number
                LIMIT 1
                FOR UPDATE
            )
            RETURNING type, account_number;
            """, nativeQuery = true)
    Optional<FreeAccountNumber> retrieveFirst(@Param("type") String type);

    @Query("SELECT COUNT(f) FROM FreeAccountNumber f WHERE f.id.type = :type")
    int countByType(@Param("type") AccountType type);

    @Modifying
    @Query(value = """
            INSERT INTO free_account_numbers (type, account_number)
            SELECT :type, :baseNumber + sequence_offset
            FROM generate_series(:initialCounter, :counterExclusive - 1) AS sequence_offset
            ON CONFLICT (type, account_number) DO NOTHING
            """, nativeQuery = true)
    int insertGeneratedBatch(@Param("type") String type,
                             @Param("baseNumber") long baseNumber,
                             @Param("initialCounter") long initialCounter,
                             @Param("counterExclusive") long counterExclusive);
}
