package com.example.security

import org.junit.Assert.*
import org.junit.Test

class AccountAccessPolicyTest {
    private val owner = "owner@example.com"
    private val valid = AccountAccess("firebase-uid", owner, true, true, 2000L)

    @Test fun verifiedOwnerWithServerClaimHasAccess() { assertTrue(AccountAccessPolicy.isAdmin(valid, owner, 1000L)) }
    @Test fun emailAloneNeverGrantsAccess() { assertFalse(AccountAccessPolicy.isAdmin(valid.copy(adminClaim = false), owner, 1000L)) }
    @Test fun unverifiedEmailNeverGrantsAccess() { assertFalse(AccountAccessPolicy.isAdmin(valid.copy(emailVerified = false), owner, 1000L)) }
    @Test fun claimForAnotherEmailDoesNotGrantAccess() { assertFalse(AccountAccessPolicy.isAdmin(valid.copy(email = "other@example.com"), owner, 1000L)) }
    @Test fun absentAdministratorConfigurationDeniesEveryone() { assertFalse(AccountAccessPolicy.isAdmin(valid, "", 1000L)) }
    @Test fun missingAuthenticatedIdentityDeniesAccess() { assertFalse(AccountAccessPolicy.isAdmin(valid.copy(uid = ""), owner, 1000L)) }
    @Test fun staleSessionCannotKeepAdminAccess() { assertFalse(AccountAccessPolicy.isAdmin(valid, owner, 2000L)) }
    @Test fun emailCaseAndWhitespaceAreNormalizedWithoutChangingAddress() {
        assertTrue(AccountAccessPolicy.isAdmin(valid.copy(email = " Owner@Example.com "), owner, 1000L))
        assertFalse(AccountAccessPolicy.isAdmin(valid.copy(email = "owner+other@example.com"), owner, 1000L))
    }
}
