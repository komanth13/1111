"""Run only with owner-controlled Application Default Credentials; never from the APK."""
import argparse
import json
from pathlib import Path

import firebase_admin
from firebase_admin import auth, credentials


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--project-id", required=True)
    parser.add_argument("--revoke", action="store_true")
    args = parser.parse_args()
    config = json.loads((Path(__file__).resolve().parents[1] / "auth/firebase-client.json").read_text())
    email = config.get("admin_email", "").strip().lower()
    if not email or config.get("project_id") != args.project_id:
        raise SystemExit("Explicit administrator email and matching Firebase project are required")
    app = firebase_admin.initialize_app(credentials.ApplicationDefault(), {"projectId": args.project_id})
    owner = auth.get_user_by_email(email, app=app)
    if owner.disabled or not owner.email_verified:
        raise SystemExit("Administrator must first sign in and verify their email")
    # A single explicit administrator: revoke old admin claims, preserving other claims.
    for user in auth.list_users(app=app).iterate_all():
        claims = dict(user.custom_claims or {})
        desired = user.uid == owner.uid and not args.revoke
        if claims.get("admin") is True or desired:
            claims["admin"] = desired
            auth.set_custom_user_claims(user.uid, claims, app=app)
            if not desired:
                auth.revoke_refresh_tokens(user.uid, app=app)
    print("Administrator role updated. Refresh account status in the app.")


if __name__ == "__main__":
    main()
