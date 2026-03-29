/* courses.js — All Courses page
   Public: anyone can browse. Login required to save/enroll. */

var favIds = [], purchIds = [], _searchTimer = null;

async function boot() {
  await refreshSession();          // sync roles from server (no-op if not logged in)
  setupNav();
  await loadCategories();
  await loadUserData();
  await loadCourses();
}

function setupNav() {
  var loggedIn = !!auth.token();
  if (!loggedIn) return;

  document.getElementById("btn-login-nav").style.display = "none";
  document.getElementById("btn-logout").style.display    = "";
  document.getElementById("link-fav").style.display      = "";
  document.getElementById("link-learn").style.display    = "";
  document.getElementById("nav-user").textContent        = auth.fullName() || auth.email() || "";
  document.getElementById("page-sub").textContent        = "Browse and enroll in available courses";
  if (auth.isAdmin())        document.getElementById("link-admin").style.display  = "";
  if (auth.isStudentAdmin()) document.getElementById("link-manage").style.display = "";
  document.getElementById("btn-logout").addEventListener("click", auth.logout);
}

async function loadCategories() {
  try {
    var cats = await api.get("/api/courses/categories");
    var sel  = document.getElementById("cat-select");
    cats.forEach(function(cat) {
      var opt = document.createElement("option");
      opt.value = cat; opt.textContent = cat;
      sel.appendChild(opt);
    });
  } catch (e) { /* silent — banner handles server-down */ }
}

async function loadUserData() {
  if (!auth.token()) return;
  try {
    var results = await Promise.all([api.get("/api/favorites"), api.get("/api/purchases")]);
    favIds   = results[0].map(function(c) { return c.id; });
    purchIds = results[1].map(function(c) { return c.id; });
  } catch (e) { /* silent */ }
}

async function loadCourses() {
  var q        = document.getElementById("search").value.trim();
  var category = document.getElementById("cat-select").value;
  var params   = [];
  if (q)        params.push("q="        + encodeURIComponent(q));
  if (category) params.push("category=" + encodeURIComponent(category));
  try {
    var url     = "/api/courses/filter" + (params.length ? "?" + params.join("&") : "");
    var courses = await api.get(url);
    var cnt     = document.getElementById("result-count");
    if (cnt) cnt.textContent = courses.length + " course" + (courses.length !== 1 ? "s" : "");
    render(courses);
  } catch (e) {
    if (!e.isNetworkError) msg("err", e.message, "error");
  }
}

function render(list) {
  var grid = document.getElementById("grid");
  if (!list.length) {
    grid.innerHTML = '<div class="empty-state"><div class="big">\uD83D\uDD0D</div><p>No courses match your search.</p></div>';
    return;
  }
  grid.innerHTML = list.map(courseCard).join("");

  grid.querySelectorAll(".btn-fav").forEach(function(btn) {
    btn.addEventListener("click", async function() {
      if (!auth.token()) { location.href = "login.html"; return; }
      var id = +btn.dataset.id;
      try {
        if (favIds.indexOf(id) >= 0) {
          await api.delete("/api/favorites/" + id);
          favIds = favIds.filter(function(f) { return f !== id; });
        } else {
          await api.post("/api/favorites/" + id);
          favIds.push(id);
        }
        var active = favIds.indexOf(id) >= 0;
        btn.classList.toggle("active", active);
        btn.textContent = active ? "\u2665 Saved" : "\u2661 Save";
      } catch (e) { alert(e.message); }
    });
  });

  grid.querySelectorAll(".btn-buy").forEach(function(btn) {
    btn.addEventListener("click", async function() {
      if (!auth.token()) { location.href = "login.html"; return; }
      var id = +btn.dataset.id;
      if (!confirm("Enroll in this course?")) return;
      try {
        await api.post("/api/purchases/" + id);
        purchIds.push(id);
        btn.outerHTML = '<span class="badge badge-green">Enrolled</span>';
      } catch (e) { alert(e.message); }
    });
  });
}

function courseCard(c) {
  var fav   = favIds.indexOf(c.id) >= 0;
  var purch = purchIds.indexOf(c.id) >= 0;
  var priceHtml = c.price === 0
    ? '<span class="card-price free">Free</span>'
    : '<span class="card-price">\u20B9' + c.price + '</span>';
  var buyHtml = purch
    ? '<span class="badge badge-green">Enrolled</span>'
    : '<button class="btn btn-amber btn-sm btn-buy" data-id="' + c.id + '">' + (c.price === 0 ? "Enroll" : "Buy") + '</button>';
  return (
    '<div class="course-card">' +
      '<div class="card-category">'    + c.category   + '</div>' +
      '<div class="card-title">'       + c.title      + '</div>' +
      '<div class="card-instructor">'  + c.instructor + '</div>' +
      '<div class="card-tags">' +
        '<span class="tag">' + c.level + '</span>' +
        '<span class="tag">' + c.duration + '</span>' +
      '</div>' +
      '<div class="card-description">' + c.description + '</div>' +
      '<div class="card-footer">' +
        priceHtml +
        '<div class="card-actions">' +
          '<button class="btn-fav ' + (fav ? "active" : "") + '" data-id="' + c.id + '">' + (fav ? "\u2665 Saved" : "\u2661 Save") + '</button>' +
          buyHtml +
        '</div>' +
      '</div>' +
    '</div>'
  );
}

document.getElementById("search").addEventListener("input", function() {
  clearTimeout(_searchTimer);
  _searchTimer = setTimeout(loadCourses, 400);
});
document.getElementById("cat-select").addEventListener("change", loadCourses);

boot();
