// Browser recreation of the Java Swing client (src/GUI/GUI.java) for demo purposes.
// All data is fake and lives in memory only; nothing talks to the real Java server.
(() => {
  "use strict";

  const $ = (id) => document.getElementById(id);
  const COLORS = ["#6366F1", "#EC4899", "#14B8A6", "#F59E0B", "#8B5CF6", "#0EA5E9"];

  // ---------------------------------------------------------------- fake data
  const minsAgo = (m) => new Date(Date.now() - m * 60000);
  const accounts = {
    "1": { password: "1", it: false },
    "2": { password: "2", it: false },
    "3": { password: "3", it: false },
    "4": { password: "4", it: false },
    "5": { password: "5", it: false },
    "it": { password: "it", it: true },
  };
  let nextChatId = 4;
  const chats = [
    { id: 1, creator: "1", members: ["1", "2"], messages: [
      { user: "2", text: "Hey! Are we still meeting for the CS 401 demo today?", time: minsAgo(52) },
      { user: "1", text: "Yes — 3pm in the lab. I'll bring the laptop with the server running.", time: minsAgo(50) },
      { user: "2", text: "Perfect, I'll test the audit export before then.", time: minsAgo(49) },
    ]},
    { id: 2, creator: "1", members: ["1", "2", "3", "4"], messages: [
      { user: "3", text: "Pushed the ChatList fixes, tests are green.", time: minsAgo(120) },
      { user: "4", text: "Nice. Did anyone update the sequence diagrams?", time: minsAgo(118) },
      { user: "1", text: "On it — will upload to the Design folder tonight.", time: minsAgo(110) },
      { user: "2", text: "Reminder: meeting minutes are due Friday 📝", time: minsAgo(95) },
    ]},
    { id: 3, creator: "3", members: ["3", "1"], messages: [
      { user: "3", text: "Can you review my ClientHandler PR when you get a sec?", time: minsAgo(30) },
    ]},
  ];
  const replies = [
    "Sounds good 👍", "Got it, thanks!", "Haha yes", "I'll take a look tonight.",
    "Can we sync after class?", "Agreed.", "Let me check and get back to you.",
  ];

  // -------------------------------------------------------------------- state
  let me = null;          // logged-in username
  let viewing = null;     // whose chats are shown (differs from `me` in audit mode)
  let audit = false;
  let currentChat = -1;

  // -------------------------------------------------------------------- theme
  const sw = $("themeSwitch");
  function applyTheme(modern) {
    document.body.classList.toggle("theme-modern", modern);
    document.body.classList.toggle("theme-classic", !modern);
    $("newChatBtn").textContent = audit ? (modern ? "Select user to audit" : "Select User")
                                        : (modern ? "New chat" : "Create New Chat");
    $("exportBtn").textContent = modern ? "Export chat log" : "Export";
    $("loginBtn").textContent = modern ? "Log in" : "Login";
    try { localStorage.setItem("cs-theme", modern ? "modern" : "classic"); } catch (e) { /* ignore */ }
    if (me) render();
  }
  let saved = null;
  try { saved = localStorage.getItem("cs-theme"); } catch (e) { /* ignore */ }
  sw.checked = saved !== "classic";
  sw.addEventListener("change", () => applyTheme(sw.checked));
  const modern = () => sw.checked;

  // ------------------------------------------------------------------ helpers
  const colorFor = (name) => {
    let h = 0;
    for (const ch of name) h = (h * 31 + ch.charCodeAt(0)) | 0;
    return COLORS[Math.abs(h) % COLORS.length];
  };
  function avatar(name, cls = "") {
    const a = document.createElement("span");
    a.className = "avatar " + cls;
    a.style.setProperty("--c", colorFor(name));
    a.textContent = name.charAt(0).toUpperCase();
    return a;
  }
  const fmtTime = (d) => d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  const chatsOf = (u) => chats.filter((c) => c.members.includes(u));
  const chatById = (id) => chats.find((c) => c.id === id);

  // Same naming rule as GUI.reloadChatList()
  function chatName(chat, forUser) {
    const others = [...new Set(chat.members.filter((m) => m !== forUser))];
    if (others.length === 1) return others[0];
    if (others.length > 1) return "Group " + chat.id;
    return "Chat " + chat.id;
  }

  let toastTimer;
  function toast(text) {
    const t = $("toast");
    t.textContent = text;
    t.hidden = false;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => (t.hidden = true), 2600);
  }

  // -------------------------------------------------------------------- login
  function credentials() {
    return { u: $("u").value.trim(), p: $("p").value, it: $("itBox").checked };
  }
  $("loginBtn").addEventListener("click", () => {
    const { u, p } = credentials();
    const acct = accounts[u];
    if (!acct || acct.password !== p) {
      alert("Login failed! Invalid username/password, or the User is already online!");
      return;
    }
    me = viewing = u;
    audit = false;
    currentChat = -1;
    $("login").hidden = true;
    $("app").hidden = false;
    applyTheme(modern());
  });
  $("p").addEventListener("keydown", (e) => { if (e.key === "Enter") $("loginBtn").click(); });
  $("createBtn").addEventListener("click", () => {
    const { u, p, it } = credentials();
    if (u.length < 6 || p.length < 6 || accounts[u]) {
      alert("Please provide a unique username. Username and password must be a minimum of 6 characters in length.");
      return;
    }
    accounts[u] = { password: p, it };
    alert("Your account was successfully created. Please try logging in!");
  });

  // ------------------------------------------------------------------- render
  function render() {
    document.body.classList.toggle("audit", audit);
    $("sideTitle").textContent = audit ? "Audit Mode" : "Chats";
    $("meName").textContent = me;
    const ma = $("meAvatar");
    ma.style.setProperty("--c", colorFor(me));
    ma.textContent = me.charAt(0).toUpperCase();
    $("meSub").textContent = modern()
      ? (audit ? "Auditing " + viewing + " · options" : (accounts[me].it ? "IT staff · options" : "Click for options"))
      : "view profile";
    $("composer").hidden = audit;
    $("auditBar").hidden = !audit;
    renderList();
    renderChat();
  }

  function renderList() {
    const list = $("chatList");
    list.innerHTML = "";
    const mine = chatsOf(viewing);
    if (!mine.length) {
      const e = document.createElement("div");
      e.className = "empty";
      e.textContent = audit ? "Select a user to audit" : "No chats yet — create one!";
      list.appendChild(e);
    }
    mine.forEach((chat) => {
      const name = chatName(chat, viewing);
      const b = document.createElement("button");
      b.className = "chat-item" + (chat.id === currentChat ? " active" : "");
      b.appendChild(avatar(name));
      const txt = document.createElement("div");
      txt.className = "ci-text";
      const n = document.createElement("div");
      n.className = "ci-name";
      n.textContent = name;
      const p = document.createElement("div");
      p.className = "ci-prev";
      const last = chat.messages[chat.messages.length - 1];
      p.textContent = last ? `${last.user}: ${last.text}` : "No messages yet";
      txt.append(n, p);
      b.appendChild(txt);
      b.addEventListener("click", () => {
        currentChat = chat.id;
        $("app").classList.add("show-conv");
        renderList();
        renderChat();
      });
      list.appendChild(b);
    });
  }

  function renderChat() {
    const box = $("messages");
    box.innerHTML = "";
    const chat = chatById(currentChat);
    if (!chat || !chat.members.includes(viewing)) {
      currentChat = -1;
      $("convTitle").textContent = "Select a chat";
      $("convSub").textContent = "Pick a conversation from the left";
      $("manageBtn").hidden = true;
      box.innerHTML = '<div class="placeholder"><big>💬</big>Select a conversation to start chatting</div>';
      return;
    }
    $("convTitle").textContent = modern() ? chatName(chat, viewing) : "Chat ID: " + chat.id;
    $("convSub").textContent = `${chat.members.length} members · Chat #${chat.id}`;
    // Same rule as the original: menu only for the creator of a chat with more than 2 members
    $("manageBtn").hidden = audit || chat.creator !== me || chat.members.length <= 2;

    let prev = null;
    chat.messages.forEach((m) => {
      const mine = m.user === viewing;
      const row = document.createElement("div");
      row.className = "msg" + (mine ? " mine" : "") + (m.user !== prev ? " first" : "");
      row.appendChild(avatar(m.user, "sm" + (m.user === prev ? " ghost" : "")));
      const wrap = document.createElement("div");
      wrap.className = "bubble-wrap";
      const meta = document.createElement("div");
      meta.className = "meta";
      meta.textContent = modern() ? `${mine ? "You" : m.user} · ${fmtTime(m.time)}`
                                  : `${m.user} - ${fmtTime(m.time)}`;
      const bubble = document.createElement("div");
      bubble.className = "bubble";
      bubble.textContent = m.text;
      wrap.append(meta, bubble);
      row.appendChild(wrap);
      box.appendChild(row);
      prev = m.user;
    });
    box.scrollTop = box.scrollHeight;
  }

  // ------------------------------------------------------------------ actions
  $("composer").addEventListener("submit", (e) => {
    e.preventDefault();
    const input = $("msgInput");
    const text = input.value;
    const chat = chatById(currentChat);
    input.value = "";
    if (!chat || !text.trim()) return;
    chat.messages.push({ user: me, text, time: new Date() });
    render();
    // Simulated reply from another member so the demo feels alive
    const others = chat.members.filter((m) => m !== me);
    if (others.length) {
      setTimeout(() => {
        const from = others[Math.floor(Math.random() * others.length)];
        chat.messages.push({ user: from, text: replies[Math.floor(Math.random() * replies.length)], time: new Date() });
        if (me) render();
      }, 1200 + Math.random() * 1200);
    }
  });

  $("newChatBtn").addEventListener("click", () => {
    if (audit) {
      const u = prompt("Enter username:");
      if (!u || !u.trim()) return;
      if (!accounts[u.trim()]) { toast("No such user: " + u.trim()); return; }
      viewing = u.trim();
      currentChat = -1;
      render();
      return;
    }
    const input = prompt("Enter usernames separated by commas:");
    if (input == null) return;
    const names = [...new Set(input.split(",").map((s) => s.trim()).filter((s) => s && s !== me))];
    const bad = names.filter((n) => !accounts[n]);
    if (!names.length || bad.length) { toast(bad.length ? "Unknown user(s): " + bad.join(", ") : "Enter at least one username"); return; }
    const chat = { id: nextChatId++, creator: me, members: [me, ...names], messages: [] };
    chats.push(chat);
    currentChat = chat.id;
    $("app").classList.add("show-conv");
    render();
  });

  $("manageBtn").addEventListener("click", (e) => {
    e.stopPropagation();
    $("manageMenu").hidden = !$("manageMenu").hidden;
  });
  $("manageMenu").addEventListener("click", (e) => {
    const act = e.target.dataset.act;
    const chat = chatById(currentChat);
    $("manageMenu").hidden = true;
    if (!act || !chat) return;
    const u = (prompt(act === "add" ? "Enter username to add:" : "Enter username to remove:") || "").trim();
    if (!u) return;
    if (act === "add") {
      if (!accounts[u] || chat.members.includes(u)) { toast("Failed to add user."); return; }
      chat.members.push(u);
      toast("User added to chat.");
    } else {
      if (!chat.members.includes(u) || u === me) { toast("Failed to remove user."); return; }
      chat.members = chat.members.filter((m) => m !== u);
      toast("User removed from chat.");
    }
    render();
  });

  $("profile").addEventListener("click", (e) => {
    if (e.target.closest(".menu")) return;
    e.stopPropagation();
    const menu = $("profileMenu");
    menu.innerHTML = "";
    const add = (label, fn) => {
      const b = document.createElement("button");
      b.textContent = label;
      b.addEventListener("click", (ev) => { ev.stopPropagation(); menu.hidden = true; fn(); });
      menu.appendChild(b);
    };
    if (audit) {
      add("Exit Audit Mode", () => { audit = false; viewing = me; currentChat = -1; applyTheme(modern()); });
      menu.appendChild(document.createElement("hr"));
    } else if (accounts[me].it) {
      add("Audit Mode", () => { audit = true; currentChat = -1; applyTheme(modern()); });
      menu.appendChild(document.createElement("hr"));
    }
    add("Logout", () => {
      me = viewing = null;
      audit = false;
      document.body.classList.remove("audit");
      $("app").hidden = true;
      $("app").classList.remove("show-conv");
      $("login").hidden = false;
      $("p").value = "";
    });
    menu.hidden = !menu.hidden;
  });

  $("exportBtn").addEventListener("click", () => {
    const chat = chatById(currentChat);
    if (!chat) { toast("Select a chat to export"); return; }
    const lines = [`Chat ${chat.id} (members: ${chat.members.join(", ")})`, ""]
      .concat(chat.messages.map((m) => `[${m.time.toISOString()}] ${m.user}: ${m.text}`));
    const a = document.createElement("a");
    a.href = URL.createObjectURL(new Blob([lines.join("\n")], { type: "text/plain" }));
    a.download = `chat_${chat.id}.txt`;
    a.click();
    URL.revokeObjectURL(a.href);
    toast("Chat log exported");
  });

  $("backBtn").addEventListener("click", () => $("app").classList.remove("show-conv"));
  document.addEventListener("click", () => { $("profileMenu").hidden = true; $("manageMenu").hidden = true; });

  applyTheme(sw.checked);
})();
