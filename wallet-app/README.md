# UBT Wallet

A wallet app for the Hyena point service in this repository. One Expo
(React Native + TypeScript) codebase builds the **web**, **Android** and
**iOS** apps.

| Screen | What it does |
| :-- | :-- |
| Sign in | Choose server URL, user ID and point type. Checks that the server is reachable and the type exists. |
| Wallet | Available balance, on hold / spent / expired totals, recent activity, pull to refresh. |
| Send | Send to another user ID, with validation, a confirmation step and an optional memo. |
| Receive | Your ID as text and QR code, copy and share. |
| Activity | Full paginated history. |
| Settings | Account details, sign out, and an opt-in developer tool to add test funds. |

Deep links pre-fill the Send screen: `ubtwallet://send?to=user_456&amount=5`
on mobile, `/send?to=user_456&amount=5` on web. The Receive QR code holds
this link.

## Run the backend

The app talks to the Spring Boot demo server in `hyena-spring-boot-starter-test`.
It needs MySQL and Redis, configured in
`hyena-spring-boot-starter-test/src/main/resources/application.yml`
(by default `localhost:3306/hyena` with user `test` / `123456`, and Redis on `localhost:6379`).

The Gradle wrapper (5.6) needs **JDK 11**:

```bash
JAVA_HOME=/path/to/jdk-11 ./gradlew :hyena-spring-boot-starter-test:bootJar
```

```bash
java -jar hyena-spring-boot-starter-test/build/libs/hyena-spring-boot-starter-test-0.0.21.jar
```

Hyena creates its tables on startup but has no point types until you add one:

```bash
curl -X POST "http://localhost:8080/hyena/system/addPointType?name=default"
```

The web app runs on a different origin, so the server must allow it through
CORS. `hyena.cors.allowed-origins` in `application.yml` lists the allowed
origins, and defaults to the Expo web dev server, `http://localhost:8081`. Add
the origin where you deploy the web build.

## Run the app

```bash
cd wallet-app
npm install
```

```bash
npm run web
```

Opens the web app at http://localhost:8081.

```bash
npm run android
```

```bash
npm run ios
```

These start the dev server for a device, emulator or simulator. The app uses
only modules bundled in Expo Go, so you can scan the QR code with Expo Go.

Server URL defaults:

- Web and iOS simulator: `http://localhost:8080`
- Android emulator: `http://10.0.2.2:8080` (the emulator's address for the host machine)
- A physical phone needs your computer's LAN address, such as `http://192.168.1.20:8080`. Enter it on the sign-in screen, or set `EXPO_PUBLIC_HYENA_URL` before starting.

## Build for release

```bash
npm run build:web
```

Writes a static site to `dist/` that you can host on any static host.

```bash
npx eas-cli@latest build --platform android
```

```bash
npx eas-cli@latest build --platform ios
```

EAS builds the store binaries in the cloud. iOS needs an Apple Developer
account. The bundle ID and package name are `com.universalblocktoken.wallet`
in `app.json`; change them before publishing.

Release builds block plain `http://` by default on both Android and iOS. Serve
the API over HTTPS.

## Checks

```bash
npm run typecheck
```

```bash
npm run lint
```

```bash
npm test
```

## Before real use

Hyena is a back-office ledger service. It was not designed to be called by end
users' devices, and the following matter before this app holds real value.

1. **No authentication.** Signing in only picks a user ID. Anyone who can
   reach the server can read any balance and call any endpoint, including
   `increase`, which creates balance from nothing. Put an authenticating
   gateway or backend-for-frontend in front of Hyena. It should derive `uid`
   from the login token and expose only the operations a user may perform.
2. **Transfers are not atomic.** Hyena has no transfer endpoint, so
   `src/lib/transfer.ts` debits the sender and then credits the recipient.
   Each call carries an idempotency `seq`, so retries are safe. If the server
   refuses the credit, the sender is refunded. If the credit's outcome is
   unknown, nothing is reversed and the user gets a reference to report. A
   server-side transfer in one database transaction is the right long-term
   fix, and fits naturally in the gateway from point 1.
3. **The server allows overdrafts.** `/hyena/point/decrease` does not check
   the available balance (the check is commented out upstream). The app
   validates the amount before sending, but two sends at once, or any other
   client, can push a balance below zero.
4. **`getPoint` cache quirk.** A `getPoint` call without `subUid` reads a
   different in-memory cache entry from the one writes update, so it can
   return a stale balance. The client always sends `subUid=` to avoid this
   (see `src/api/hyena.ts`).

## Layout

```
src/app/            Screens (Expo Router file-based routes)
  _layout.tsx       Root stack; sign-in vs. signed-in tabs
  sign-in.tsx
  (tabs)/           Wallet, Send, Receive, Activity, Settings
src/api/            Hyena REST client and response types
src/lib/            Amount parsing, activity labels, transfer flow
src/state/          Session, balance and preferences (React context, AsyncStorage)
src/components/     Theme (light/dark) and shared UI
```
