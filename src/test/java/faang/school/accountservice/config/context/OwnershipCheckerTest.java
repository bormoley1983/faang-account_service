package faang.school.accountservice.config.context;

import faang.school.accountservice.model.Account;
import faang.school.accountservice.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnershipCheckerTest {

    private static final long OWNER_ID = 10L;
    private static final long OTHER_USER_ID = 20L;
    private static final long ADMIN_ID = 99L;
    private static final long ACCOUNT_ID = 5L;

    @Mock
    private AccountService accountService;
    @Mock
    private UserContext userContext;

    private OwnershipChecker ownershipChecker;

    @BeforeEach
    void setUp() {
        ownershipChecker = new OwnershipChecker(accountService, userContext);
        ReflectionTestUtils.setField(ownershipChecker, "adminUserIdsRaw", String.valueOf(ADMIN_ID));
    }

    private Account accountOwnedBy(long ownerId) {
        Account account = new Account();
        account.setId(ACCOUNT_ID);
        account.setOwnerId(ownerId);
        return account;
    }

    @Test
    void assertCanAccess_whenNoAuthenticatedUser_throwsSecurityException() {
        when(userContext.getUserId()).thenReturn(null);

        assertThatThrownBy(() -> ownershipChecker.assertCanAccess(ACCOUNT_ID))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Authenticated user is required");
    }

    @Test
    void assertCanAccess_whenUserIsOwner_allows() {
        when(userContext.getUserId()).thenReturn(OWNER_ID);
        when(accountService.getAccount(ACCOUNT_ID)).thenReturn(accountOwnedBy(OWNER_ID));

        assertThatCode(() -> ownershipChecker.assertCanAccess(ACCOUNT_ID)).doesNotThrowAnyException();
    }

    @Test
    void assertCanAccess_whenUserIsAdmin_allowsEvenIfNotOwner() {
        when(userContext.getUserId()).thenReturn(ADMIN_ID);

        assertThatCode(() -> ownershipChecker.assertCanAccess(ACCOUNT_ID)).doesNotThrowAnyException();
        verifyNoInteractions(accountService);
    }

    @Test
    void assertCanAccess_whenUserIsNotOwner_throwsSecurityException() {
        when(userContext.getUserId()).thenReturn(OTHER_USER_ID);
        when(accountService.getAccount(ACCOUNT_ID)).thenReturn(accountOwnedBy(OWNER_ID));

        assertThatThrownBy(() -> ownershipChecker.assertCanAccess(ACCOUNT_ID))
                .isInstanceOf(SecurityException.class)
                .hasMessage("User " + OTHER_USER_ID + " is not allowed to access account owned by " + OWNER_ID);
    }

    @Test
    void assertCanAccessAccount_whenNoAuthenticatedUser_throwsSecurityException() {
        when(userContext.getUserId()).thenReturn(null);

        assertThatThrownBy(() -> ownershipChecker.assertCanAccess(accountOwnedBy(OWNER_ID)))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Authenticated user is required");
    }

    @Test
    void assertCanAccessAccount_whenUserIsOwner_allows() {
        when(userContext.getUserId()).thenReturn(OWNER_ID);

        assertThatCode(() -> ownershipChecker.assertCanAccess(accountOwnedBy(OWNER_ID))).doesNotThrowAnyException();
        verifyNoInteractions(accountService);
    }

    @Test
    void assertCanAccessAccount_whenUserIsNotOwner_throwsSecurityException() {
        when(userContext.getUserId()).thenReturn(OTHER_USER_ID);

        assertThatThrownBy(() -> ownershipChecker.assertCanAccess(accountOwnedBy(OWNER_ID)))
                .isInstanceOf(SecurityException.class)
                .hasMessage("User " + OTHER_USER_ID + " is not allowed to access account owned by " + OWNER_ID);
    }
}
