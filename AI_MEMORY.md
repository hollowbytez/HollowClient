# 🧠 HOLLOW CLIENT - WORKSPACE AI MEMORY
> **Persistent memory index for Google Antigravity / AI Agents & Developers.**  
> *Use this file to instantly onboard when migrating or initializing this workspace on any new Operating System (e.g. Linux Ubuntu).*

---

## 📂 1. Repository Architecture & Directory Mapping
The project is split into two primary components: the **Minecraft Client Mod** (Fabric 1.21.1) and the **Authentication & Telemetry Server** (Node.js/Express/MongoDB).

```
HollowClient-Source/
├── src/                          # Minecraft Client Mod Source
│   └── main/
│       ├── java/net/hollowclient/
│       │   ├── client/
│       │   │   ├── gui/          # UI Screens (Auth, Settings, Main Menu)
│       │   │   ├── hud/          # PvP HUD Renderers & Modules
│       │   │   └── cos/          # Cosmetics rendering logic
│       │   └── mixin/            # Mixin Injection Hooks
│       └── resources/            # Mod resources (assets, configs, logo)
├── server/                       # Backend Authentication & Telemetry Server
│   ├── public/                   # Static web assets
│   │   ├── admin.html            # Dark-Cyberpunk Admin Dashboard
│   │   └── logo.png              # Client branding image
│   ├── server.js                 # Express Application & REST Endpoints
│   ├── package.json              # Backend dependencies
│   └── .env                      # Environment configurations (Secrets)
├── build.gradle                  # Gradle compilation configurations
├── gradlew / gradlew.bat         # Gradle wrapper scripts
├── hollowclient-1.0.0.jar        # Final compiled output (release build)
└── AI_MEMORY.md                  # This file
```

---

## 🔄 2. Git Branching Model
The repository is split into two distinct branches:
1. **`source-code` branch (Local workspace & Development):**
   * Contains all `.java` files, gradle assets, server directories, and development tools.
   * Remote Target: Pushed to `source/main` (`https://github.com/hollowbytez/HollowClient-Source.git`).
2. **`main` branch (Production & Client Releases):**
   * Contains *only* the compiled release `hollowclient-1.0.0.jar` and general client README (no source code is public here to prevent cracked distributions).
   * Remote Target: Pushed to `origin/main` (`https://github.com/hollowbytez/HollowClient.git`).

---

## 🔒 3. Authentication & Verification Flow
Before starting Minecraft, the client checks the validity of the license key:
* **Entry Point:** [HollowAuthScreen.java](file:///A:/MCMDS/src/main/java/net/hollowclient/client/gui/HollowAuthScreen.java)
* **HWID Generation:** Generates a unique 16-character hardware identifier based on System specifications:
  `OS name + OS Arch + Username + CPU Identifier + Core Count` hashed via SHA-256.
* **REST Handshake:** Client makes a POST request to `/api/verify` containing:
  ```json
  {
    "key": "HOLLOW-XXXX-XXXX-XXXX",
    "hwid": "952929431A81F5E9",
    "username": "PlayerUsername"
  }
  ```
* **Server Logic:**
  1. Checks if the `HWID` is blacklisted (hardware banned).
  2. Verifies if the key exists, is `active`, and has not expired.
  3. Checks device limits (`maxDevices`). If the HWID is new but slots are open, registers it. If slots are full, rejects it.
  4. Records the active player username.

---

## 👁️ 4. Live Telemetry & In-Game Surveillance
Once the client is authenticated, it actively reports player details in the background.
* **Tick Injector:** [MinecraftClientMixin.java](file:///A:/MCMDS/src/main/java/net/hollowclient/mixin/client/MinecraftClientMixin.java#L61)
* **Heartbeat:** Runs asynchronously every **100 game ticks (5 seconds)**.
* **Data Collected:**
  * Minecraft Username
  * Current Server Address (e.g. `hypixel.net`, `Singleplayer`, or `MainMenu`)
  * Accurate Coordinates (`X, Y, Z`)
  * World Name
  * World Seed (Only available in singleplayer worlds due to vanilla server protection limits)
* **API Target:** Client POSTs telemetry to the server route `/api/telemetry`.

---

## 📊 5. Web Admin Dashboard (`admin.html`)
The Admin panel allows full owner control.
* **Endpoint URL:** `https://hollowclient-source.onrender.com/admin.html`
* **Access Password (Admin Token):** `Minhal786.New`
* **Features:**
  * **Surveillance Tab:** Live table with auto-refresh (every 3.5 seconds) displaying online player lists, current servers, coords, world names, seeds, and ping/latency checks.
  * **License Manager:** Lists all keys, statuses, and bound hardware locks. Actions: *Activate*, *Suspend*, *Reset HWID* (clears device lock), and *Delete*.
  * **Key Generator:** Generate licenses in bulk with custom prefixes, custom device limits (how many devices can share one key), and expiration durations (days).
  * **Hardware Ban (Blacklist):** Ban players at the hardware level (`HWID`). A blacklisted hardware ID can never start the client, regardless of key status.
  * **Auto-Updates:** Push mandatory client updates (.jar files) that force users to update before launching.

---

## 🛠️ 6. Build Commands & Tooling (Ubuntu / Linux Setup)
When running on Ubuntu Linux, compile and run using these commands:

### Compile client mod & copy to local launcher:
```bash
# Set Java 21 Home path and build mod
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"
./gradlew copyToModsFolder --no-daemon
```

### Run Server locally:
```bash
cd server
npm install
# Set Environment Secrets locally inside .env:
# MONGODB_URI=...
# ADMIN_TOKEN=Minhal786.New
node server.js
```

---

## 🤖 7. Instructions for Future AI Assistants
When starting a new conversation session or onboarding this workspace on Linux:
1. Run `git checkout source-code` immediately to ensure the Java codebase is loaded into workspace directory.
2. Read [server.js](file:///A:/MCMDS/server/server.js) to inspect the API endpoints.
3. Read [MinecraftClientMixin.java](file:///A:/MCMDS/src/main/java/net/hollowclient/mixin/client/MinecraftClientMixin.java) and [HollowAuthScreen.java](file:///A:/MCMDS/src/main/java/net/hollowclient/client/gui/HollowAuthScreen.java) to understand security gates and telemetry.
4. Database documents (legacy keys) may lack properties like `hwids`. Always use fallback syntax in JavaScript views (`k.hwids || []`) and schemas to prevent crashes.
