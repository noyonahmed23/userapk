# Security gate before enabling real-money features

## Changes already made in the Android app

- Authentication no longer invents demo wallet balances, tournament statistics, or game UIDs.
- Authentication no longer silently logs a user in as a local/demo account when Firebase is unavailable.
- The Admin APK now requires a server-issued Firebase custom claim (`admin: true` or `role: "admin"`) before showing the Admin Console.

## Critical backend blockers — do not enable real money yet

The Android app currently calls `/api.php?action=state` and exchanges one shared application-state JSON document. The same endpoint is referenced by both Android apps, and the client build configuration embeds a shared `X-Khelo-App-Key`. A value shipped in an APK is extractable and must not be treated as a private credential.

A backend that only stores a global JSON state cannot securely enforce per-account wallet ownership or make wallet changes atomic. The server must not trust wallet balances, transaction history, deposit approvals, withdrawal approvals, tournament stakes, or prize amounts submitted by a client.

The PHP implementation of `khelobdapp-api` and its current database schema were not present in this Android repository when this change was made. Therefore this commit does **not** claim to have secured the live server.

## Required server-side implementation

1. Verify Firebase ID tokens on the server (signature, issuer, audience/project ID, expiry) for protected endpoints.
2. Authorize Admin operations on the server with a server-controlled admin allowlist/role. A screen gate in the APK is defense in depth only.
3. Replace the shared mutable app-state wallet with per-user wallet-account rows and an append-only ledger. Update balances only in database transactions with row locking and unique idempotency keys.
4. Treat a submitted bKash/Nagad TrxID as an unverified claim, not payment proof. Do not credit deposits until verified through the official merchant/provider flow or a documented manual reconciliation process; retain provider references and an audit log.
5. Reserve/hold withdrawal funds atomically, enforce limits and rate limits, prevent duplicate requests, and only mark payouts complete after server-side confirmation.
6. Remove or disable the old endpoint's ability to accept client-supplied wallet balances, transactions, deposit statuses, withdrawal statuses, admin roles, and other privileged state.
7. Keep database credentials, Firebase Admin SDK credentials, provider credentials and webhook secrets on the server only; rotate the embedded shared API key when the legacy endpoint is retired.
8. Test unauthorized reads/writes, cross-user access, repeated TrxIDs, concurrent withdrawals, duplicated webhooks, rejected payments, and audit history before any real-money launch.

## Deployment note

Do not put service-account JSON, database passwords, payment secrets, or private signing material in source control or in either APK. To patch the actual hosted API safely, the existing `khelobdapp-api` PHP source, configuration with credentials removed, and SQL schema/migrations are required. Replace all secrets with placeholders before sharing.
