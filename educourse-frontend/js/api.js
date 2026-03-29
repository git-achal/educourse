/* ============================================================
   api.js  —  EduCourse shared helpers
   Load this FIRST on every page before any other script.
   ============================================================ */

const BASE = "http://localhost:8080";

/* ── localStorage session ───────────────────────────────── */
const session = {
  set:     (k, v) => localStorage.setItem(k, typeof v === "object" ? JSON.stringify(v) : v),
  get:     (k)    => localStorage.getItem(k),
  getJSON: (k)    => { try { return JSON.parse(localStorage.getItem(k)); } catch { return null; } },
  clear:   ()     => localStorage.clear(),
};

/* ── Auth helpers ────────────────────────────────────────── */
const auth = {
  token:          () => session.get("token"),
  email:          () => session.get("email"),
  fullName:       () => session.get("fullName"),
  roles:          () => session.getJSON("roles") || [],
  isAdmin:        () => auth.roles().includes("ROLE_ADMIN"),
  isStudentAdmin: () => auth.roles().includes("ROLE_STUDENT_ADMIN"),
  logout:         () => { session.clear(); location.href = "login.html"; },
  guard:          () => { if (!auth.token()) { location.href = "login.html"; } },
};

/* ── Server-down banner ──────────────────────────────────── */
let _banner = null, _retryTimer = null, _countdown = 10;

function _showBanner() {
  if (_banner) return;
  if (!document.getElementById("_spin_style")) {
    const s = document.createElement("style");
    s.id = "_spin_style";
    s.textContent = "@keyframes _spin{to{transform:rotate(360deg)}}";
    document.head.appendChild(s);
  }
  _banner = document.createElement("div");
  _banner.style.cssText =
    "position:fixed;top:0;left:0;right:0;z-index:9999;background:#1e3a5f;" +
    "color:#e0eaff;font-size:14px;font-weight:500;padding:12px 20px;" +
    "display:flex;align-items:center;gap:12px;box-shadow:0 2px 12px rgba(0,0,0,.3)";
  const spin = document.createElement("span");
  spin.style.cssText =
    "width:16px;height:16px;border:2px solid rgba(255,255,255,.25);" +
    "border-top-color:#93c5fd;border-radius:50%;animation:_spin .8s linear infinite;flex-shrink:0";
  const txt = document.createElement("span");
  txt.id = "_banner_txt";
  _banner.appendChild(spin);
  _banner.appendChild(txt);
  document.body.prepend(_banner);
  _countdown = 10;
  _updateBanner();
  _retryTimer = setInterval(() => {
    _countdown--;
    if (_countdown <= 0) { clearInterval(_retryTimer); location.reload(); }
    else _updateBanner();
  }, 1000);
}

function _hideBanner() {
  if (_banner) { _banner.remove(); _banner = null; }
  if (_retryTimer) { clearInterval(_retryTimer); _retryTimer = null; }
}

function _updateBanner() {
  const el = document.getElementById("_banner_txt");
  if (el) el.textContent =
    "\u23F3 Server not responding \u2014 retrying in " + _countdown + "s. Make sure the server is up and running!";
}

/* ── Fetch wrapper ───────────────────────────────────────── */
async function apiFetch(path, opts) {
  opts = opts || {};
  const token = auth.token();
  let res;
  try {
    res = await fetch(BASE + path, Object.assign({}, opts, {
      headers: Object.assign(
        { "Content-Type": "application/json" },
        token ? { "Authorization": "Bearer " + token } : {},
        opts.headers || {}
      )
    }));
  } catch (_) {
    _showBanner();
    const err = new Error("Server is not reachable.");
    err.isNetworkError = true;
    throw err;
  }
  _hideBanner();
  const ct   = res.headers.get("content-type") || "";
  const body = ct.includes("json") ? await res.json() : await res.text();
  if (!res.ok) throw new Error((body && body.message) || body || "Request failed");
  return body;
}

const api = {
  get:    function(p)    { return apiFetch(p); },
  post:   function(p, d) { return apiFetch(p, { method: "POST",   body: JSON.stringify(d) }); },
  put:    function(p, d) { return apiFetch(p, { method: "PUT",    body: JSON.stringify(d) }); },
  delete: function(p)    { return apiFetch(p, { method: "DELETE" }); },
};

/* ── Flash messages ──────────────────────────────────────── */
function msg(id, text, type) {
  const el = document.getElementById(id);
  if (!el) return;
  el.textContent = text;
  el.className = "msg " + (type || "error");
}
function hideMsg(id) {
  const el = document.getElementById(id);
  if (el) el.className = "msg hidden";
}

/* ── Silent session refresh ──────────────────────────────── */
// Re-fetches /api/user/me on every page load so role changes
// (e.g. admin grants STUDENT_ADMIN) are visible without re-login.
async function refreshSession() {
  if (!auth.token()) return;
  try {
    const me = await api.get("/api/user/me");
    session.set("fullName", me.fullName);
    session.set("roles",    me.roles);
  } catch (e) {
    if (!e.isNetworkError) {
      const m = (e.message || "").toLowerCase();
      if (m.includes("401") || m.includes("403") || m.includes("unauthorized")) {
        auth.logout();
      }
    }
  }
}

/* ── Navbar init ─────────────────────────────────────────── */
function initNav() {
  const userEl   = document.getElementById("nav-user");
  const logoutEl = document.getElementById("btn-logout");
  if (userEl)   userEl.textContent = auth.fullName() || auth.email() || "";
  if (logoutEl) logoutEl.addEventListener("click", auth.logout);
}

/* ── Footer ──────────────────────────────────────────────── */
function injectFooter() {
  const footer = document.createElement("footer");
  footer.className = "site-footer";
  footer.innerHTML =
    '<div class="footer-inner">' +
      '<div class="footer-brand"><img src="favicon.svg" alt="" class="footer-logo"/><span>EduCourse</span></div>' +
      '<div class="footer-about" id="about">' +
        '<h4>About EduCourse</h4>' +
        '<p>EduCourse is a modern online learning platform connecting passionate instructors with eager learners. ' +
        'Browse 50+ expert-crafted courses across programming, data science, web development, DevOps, and more.</p>' +
      '</div>' +
      '<div class="footer-contact"><h4>Contact</h4><ul>' +
        '<li><svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg><a href="mailto:achalchaudhari25@gmail.com">achalchaudhari25@gmail.com</a></li>' +
        '<li><svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 12 19.79 19.79 0 0 1 1.65 3.18 2 2 0 0 1 3.62 1h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 8.91a16 16 0 0 0 5.61 5.61l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/></svg><a href="tel:+919049360885">+91 9049360885</a></li>' +
      '</ul></div>' +
      '<div class="footer-links"><h4>Connect</h4><ul>' +
        '<li><svg width="14" height="14" fill="currentColor" viewBox="0 0 24 24"><path d="M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22"/></svg><a href="https://github.com/git-achal/educourse" target="_blank" rel="noopener">github.com/git-achal/educourse</a></li>' +
        '<li><svg width="14" height="14" fill="currentColor" viewBox="0 0 24 24"><path d="M16 8a6 6 0 0 1 6 6v7h-4v-7a2 2 0 0 0-2-2 2 2 0 0 0-2 2v7h-4v-7a6 6 0 0 1 6-6z"/><rect x="2" y="9" width="4" height="12"/><circle cx="4" cy="4" r="2"/></svg><a href="https://linkedin.com/in/achal-chaudhari25" target="_blank" rel="noopener">Achal Chaudhari</a></li>' +
        '<li><a href="#about" class="footer-about-btn" onclick="document.getElementById(\'about\').scrollIntoView({behavior:\'smooth\'});return false;">About Us &#8593;</a></li>' +
      '</ul></div>' +
    '</div>' +
    '<div class="footer-bottom"><p>&copy; 2025 EduCourse &middot; Developed by <strong>Achal & Elkorf</strong> &middot; All rights reserved.</p></div>';
  document.body.appendChild(footer);
}
