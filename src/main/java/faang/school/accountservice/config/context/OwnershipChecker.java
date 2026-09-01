package faang.school.accountservice.config.context;

import faang.school.accountservice.model.Account;
import faang.school.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Centralized ownership/authorization check for account-scoped operations.
 * <p>
 * A request is allowed when the authenticated user either:
 * <ul>
 *   <li>owns the account ({@code account.ownerId == currentUserId}), or</li>
 *   <li>is listed in the configured admin/manager user-ID set.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class OwnershipChecker {

    private final AccountService accountService;
    private final UserContext userContext;

    @Value("${account.security.admin-user-ids:}")
    private String adminUserIdsRaw;

    public void assertCanAccess(long accountId) {
        Long currentUserId = userContext.getUserId();
        if (currentUserId == null) {
            throw new SecurityException("Authenticated user is required");
        }
        if (isAdmin(currentUserId)) {
            return;
        }
        Account account = accountService.getAccount(accountId);
        if (!account.getOwnerId().equals(currentUserId)) {
            throw new SecurityException(
                    "User " + currentUserId + " is not allowed to access account owned by " + account.getOwnerId());
        }
    }

    public void assertCanAccess(Account account) {
        Long currentUserId = userContext.getUserId();
        if (currentUserId == null) {
            throw new SecurityException("Authenticated user is required");
        }
        if (isAdmin(currentUserId)) {
            return;
        }
        if (!account.getOwnerId().equals(currentUserId)) {
            throw new SecurityException(
                    "User " + currentUserId + " is not allowed to access account owned by " + account.getOwnerId());
        }
    }

    public void assertAdmin() {
        Long currentUserId = userContext.getUserId();
        if (currentUserId == null) {
            throw new SecurityException("Authenticated user is required");
        }
        if (!isAdmin(currentUserId)) {
            throw new SecurityException(
                    "User " + currentUserId + " is not an admin and cannot perform this operation");
        }
    }

    public void assertAuthenticated() {
        if (userContext.getUserId() == null) {
            throw new SecurityException("Authenticated user is required");
        }
    }

    private boolean isAdmin(Long userId) {
        Set<Long> adminIds = parseAdminIds();
        return adminIds.contains(userId);
    }

    private Set<Long> parseAdminIds() {
        if (adminUserIdsRaw == null || adminUserIdsRaw.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(adminUserIdsRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }
}
