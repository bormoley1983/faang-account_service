package faang.school.accountservice.repository;

import faang.school.accountservice.model.SavingsAccount;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SavingsAccountRepository extends JpaRepository<SavingsAccount, Long> {

    @Query(
            value = "SELECT s.* " +
                    "FROM savings_account s " +
                    "JOIN account a ON s.account_id = a.id " +
                    "WHERE a.owner_id = :ownerId",
            nativeQuery = true
    )
    Optional<SavingsAccount> findByOwnerId(@Param("ownerId") Long ownerId);

    @Query(
            value = "SELECT s.* " +
                    "FROM savings_account s " +
                    "WHERE s.last_interest_date < :today " +
                    "OR s.last_interest_date IS NULL",
            nativeQuery = true
    )
    List<SavingsAccount> findAllAccountsRequiringInterest(@Param("today") LocalDate today);
}
