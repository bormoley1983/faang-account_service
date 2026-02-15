package faang.school.accountservice.repository;

import faang.school.accountservice.enums.AccountType;
import faang.school.accountservice.model.AccountSeq;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountSeqRepository extends JpaRepository<AccountSeq, AccountType> {

    @Query(nativeQuery = true, value = """
            UPDATE account_number_sequence
            SET counter = counter + :batchSize
            WHERE type = :type
            RETURNING type, counter, counter - :batchSize AS initialValue
            """)
    Optional<AccountSeq> incrementCounter(@Param("type") String type, @Param("batchSize") int batchSize);
}