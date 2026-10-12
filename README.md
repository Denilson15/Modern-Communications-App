# Communication System

A real-time, multi-user chat system in Java: a TCP socket server, a Swing desktop client, group and private chats, and an IT audit mode. Built as a team project for CS 401: Software Engineering.

> **Live UI demo (no install):** https://denilson15.github.io/Modern-Communications-App/
> Use the **Classic ⟷ Modern** slider at the top to compare the original interface with an AI-generated redesign.

## About this repository

This is my personal copy of a six-person class project. The original team repository, with every contributor's full history, is here:
**https://github.com/Aircc0615/Communication-System-Group-Project**

I made this copy so the project can be **seen without compiling or running anything**:

- The **live demo** recreates the client in the browser so anyone can click through it. It runs entirely in the browser with **sample data**, so it does not connect to a server and nothing is saved.
- For the **real, fully working system** (actual server, real accounts, messages between clients), follow [Run the full application](#run-the-full-application) below. It takes about five minutes.

The team's original source code in this repo is **unchanged**. Everything added afterwards (the modern UI, the web demo, this README layout) is listed in [Added after submission](#added-after-submission) and is marked as AI-generated.

| Original UI (Classic) | AI-generated redesign (Modern) |
|---|---|
| ![Classic desktop UI](docs/screenshots/classic-desktop.png) | ![Modern desktop UI](docs/screenshots/modern-desktop.png) |

---

## My contributions

From the original repository's commit history. I'm the second-largest contributor, with about 88 of roughly 270 commits. My work was mainly on the networking layer and on connecting it to the GUI:

- **Client/server foundation:** wrote the starter code for the client and server, and the message protocol (`Message`, `MainType`, `SubType`, `Status`) that every operation uses.
- **Server and client operations:** implemented most of the request handling in `Server`, `ClientHandler` and `Client`: login, logout, account creation, creating group chats, and delivering messages to multiple users.
- **User login module:** created and implemented `UserLoginModule`.
- **GUI integration:** connected the Swing GUI to the client for login, account creation, chat creation, adding and removing chat members, and logging out when the window closes.
- **Documentation:** contributed to the Software Requirements Specification and the early requirements docs.

My teammates built the chat and user data model, the JUnit test suite, much of the GUI layout, and the design diagrams. See the original repository for the full picture.

---

## Run the full application

You need the **Java Development Kit (JDK) 17 or newer**. Nothing else needs to be installed; the project uses only the standard library.

**1. Check that Java is installed**
```bash
java -version
javac -version
```
If either command isn't found, install a JDK (for example from [Adoptium](https://adoptium.net/)) and open a new terminal.

**2. Download the code**
```bash
git clone https://github.com/Denilson15/Modern-Communications-App.git
cd Modern-Communications-App
```
(Or use **Code → Download ZIP** on GitHub and unzip it.)

**3. Compile**

macOS / Linux:
```bash
javac -d bin $(find src -name "*.java")
```
Windows (PowerShell):
```powershell
javac -d bin (Get-ChildItem -Recurse src -Filter *.java).FullName
```

**4. Start the server** (terminal 1, leave it running)
```bash
java -cp bin networking.Server
```
A small "shut down the Server!" window appears. The first run creates six accounts, usernames and passwords `1` through `6`. Account `1` is an IT account.

**5. Start a client** (terminal 2)
```bash
java -cp bin GUI.Launcher
```
Pick **Classic** (the original UI) or **Modern** with the slider and click **Launch**. To open the original UI directly, run `java -cp bin GUI.Main` instead.

**6. Chat between two users**
- Log in as `1` / `1` in the first client.
- Open a third terminal, run `java -cp bin GUI.Launcher` again, and log in as `2` / `2`.
- In one client, create a chat with the other user and send messages. They appear in the other window in real time.

**Useful details**
- **Server on another computer:** pass its IP address, e.g. `java -cp bin GUI.Launcher 192.168.1.20`. The server listens on TCP port `59090`.
- **Audit mode:** as an IT user (such as `1`), click your name at the bottom left and choose **Audit Mode**. Then select any user to view their chats and export a chat log.
- **Where data is saved:** chats go to `LocalFiles/Server`, users to `LocalFiles/Users`, and exports to `LocalFiles/IT_Export/Chats`. Delete `LocalFiles/` to start fresh. Restart the server after editing these files by hand.
- **Eclipse:** import the folder as an existing Java project, run `networking/Server.java`, then run `GUI/Launcher.java` (or `GUI/Main.java`).

---

## About the original project

The team went through the full software-engineering process before and during implementation:

- **Requirements:** the Software Requirements Specification, use case specification and diagram, class diagram, sequence diagrams for login/logout and message sending, and meeting minutes ([`Requirements/`](Requirements)).
- **Design:** the design specification, use cases, and design-phase meeting minutes ([`Design/`](Design)).
- **Planning:** the project schedule ([`projectSchedule.pdf`](projectSchedule.pdf), [`Project schedule.csv`](Project%20schedule.csv)).
- **Implementation and testing:** Java source in [`src/`](src) and JUnit tests in [`test/`](test).

### Overview
A scalable, text-only communication system that lets many users send messages to each other. Messages can be sent synchronously or asynchronously, in group chats or private chats. Users can only see messages meant for them, while IT staff can view all messages. Both users and IT staff use the system through the provided GUI.

### Architecture
A real-time system built on a two-way client-server connection. Authenticated users connect to the server over TCP/IP and exchange serialized `Message` objects. Each connected client is served by its own `ClientHandler` thread. Message history is stored in plain data files rather than an external database, so the system is less suited to keeping very large amounts of history.

### Constraints
1. The application must be developed in Java.
2. The system does not use databases, libraries, frameworks, or other outside technologies.
3. The system follows standard object-oriented design principles.
4. The application was developed in the Eclipse IDE.

---

## Added after submission

> ⚠️ **Disclaimer:** everything in this section was **entirely AI-generated**, after the original project was submitted and graded.
> It is a presentation layer only. **None of the original source code was changed.** `src/chat`, `src/networking`, `src/user`, `src/GUI/GUI.java`, `src/GUI/Main.java` and all tests are identical to the submitted version.

| File | Purpose |
|---|---|
| `src/GUI/Launcher.java` | New entry point. A start screen with a **Classic ⟷ Modern** slider that opens either the original `GUI` or the new `ModernGUI`. |
| `src/GUI/ModernGUI.java` | Modern restyle of the client. It extends the original `GUI` class and reuses the same `Client`, `User` and `Chat` code, so it has the same features with a new look. |
| `src/GUI/Theme.java` | Colors, fonts and custom-painted rounded components. Pure Swing, no libraries. |
| `web/` | Browser-only recreation of the client for the live demo, with the Classic/Modern slider. |
| `index.html`, `.nojekyll`, `netlify.toml` | Hosting setup for the web demo. GitHub Pages serves the repo root, and `index.html` forwards to `web/`. |
| `docs/screenshots/` | Screenshots used in this README. |

![Launcher](docs/screenshots/launcher.png)

### About the web demo
The real app is a Java Swing desktop client talking to a Java server over TCP sockets, so it can't run in a browser as-is. The [live demo](https://denilson15.github.io/Modern-Communications-App/) is a **front-end-only recreation** in HTML, CSS and JavaScript. Its users, chats and messages are sample data built into the page, and the replies you get are simulated, so you can try login, chats, group management, IT audit mode and export without installing anything. Refreshing the page resets it.

Demo accounts: `1` to `5` (the password is the same as the username). IT account: `it` / `it`.

| Web demo: Classic | Web demo: Modern |
|---|---|
| ![Web classic](docs/screenshots/web-classic.png) | ![Web modern](docs/screenshots/web-modern.png) |

The modern web view follows your device's light or dark setting:

![Web modern, dark mode](docs/screenshots/web-modern-dark.png)

---

### Thank you for checking out the project!
