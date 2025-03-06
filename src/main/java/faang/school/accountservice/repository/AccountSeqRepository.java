package faang.school.accountservice.repository;

import faang.school.accountservice.model.AccountSeq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountSeqRepository extends JpaRepository<AccountSeq, String> {

    @Query(nativeQuery = true,
            value = """
                    UPDATE account_number_sequence SET counter = counter + :batchSize
                    WHERE type = :type
                    RETURNING type, counter, counter - :batchSize AS initialCounter
                    """)
    List<Object[]> incrementCounter(String type, int batchSize);
}
