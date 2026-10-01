"""Authorization boundary for future remote administrative API endpoints."""
from firebase_admin import auth


def require_administrator(id_token: str, administrator_email: str):
    # Verify signature, issuer/audience, expiry and revoked/disabled account status on the server.
    claims = auth.verify_id_token(id_token, check_revoked=True)
    expected = administrator_email.strip().lower()
    if not (expected and claims.get("admin") is True and claims.get("email_verified") is True
            and claims.get("email", "").strip().lower() == expected and claims.get("uid")):
        raise PermissionError("Administrator access required")
    return claims
