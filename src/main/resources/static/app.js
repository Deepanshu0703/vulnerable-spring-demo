// ── Navigation ──────────────────────────────────────────────────────────────
const navItems = document.querySelectorAll('.nav-item');
const pages    = document.querySelectorAll('.page');
const pageTitle = document.getElementById('page-title');

const titles = {
    users:   'User Management',
    system:  'System Utilities',
    files:   'File Browser',
    content: 'Content Board',
};

function navigate(id) {
    navItems.forEach(n => n.classList.toggle('active', n.dataset.page === id));
    pages.forEach(p => p.classList.toggle('active', p.id === 'page-' + id));
    pageTitle.textContent = titles[id] || id;
}

navItems.forEach(n => n.addEventListener('click', () => navigate(n.dataset.page)));
navigate('users');

// ── Helpers ──────────────────────────────────────────────────────────────────
function box(id) { return document.getElementById(id); }

function show(boxEl, data, isError = false) {
    boxEl.classList.remove('empty', 'error');
    if (isError) boxEl.classList.add('error');
    boxEl.textContent = typeof data === 'string' ? data : JSON.stringify(data, null, 2);
}

async function api(method, path, params = {}, body = null) {
    let url = path;
    if (Object.keys(params).length) {
        url += '?' + new URLSearchParams(params).toString();
    }
    const opts = { method };
    if (body !== null) {
        if (typeof body === 'string') {
            opts.body = body;
            opts.headers = { 'Content-Type': 'text/plain' };
        } else {
            opts.body = new URLSearchParams(body).toString();
            opts.headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
        }
    }
    try {
        const res = await fetch(url, opts);
        const text = await res.text();
        try { return { ok: res.ok, data: JSON.parse(text), status: res.status }; }
        catch { return { ok: res.ok, data: text, status: res.status }; }
    } catch (e) {
        return { ok: false, data: e.message, status: 0 };
    }
}

// ── USERS ────────────────────────────────────────────────────────────────────
box('btn-user-search').onclick = async () => {
    const r = await api('GET', '/api/users/search', { username: box('user-search-input').value });
    show(box('user-search-resp'), r.data, !r.ok);
};

box('btn-user-login').onclick = async () => {
    const r = await api('POST', '/api/users/login', {},
        { username: box('login-user').value, password: box('login-pass').value });
    show(box('user-login-resp'), r.data, !r.ok);
};

box('btn-user-byid').onclick = async () => {
    const r = await api('GET', '/api/users/byId', { id: box('user-id-input').value });
    show(box('user-byid-resp'), r.data, !r.ok);
};

box('btn-user-list').onclick = async () => {
    const r = await api('GET', '/api/users/list', { sortBy: box('user-sort').value });
    show(box('user-list-resp'), r.data, !r.ok);
};

// ── SYSTEM ───────────────────────────────────────────────────────────────────
box('btn-nslookup').onclick = async () => {
    const r = await api('GET', '/api/system/nslookup', { domain: box('nslookup-domain').value });
    show(box('nslookup-resp'), r.data, !r.ok);
};

box('btn-digest').onclick = async () => {
    const r = await api('GET', '/api/system/digest', { filename: box('digest-file').value });
    show(box('digest-resp'), r.data, !r.ok);
};

// ── FILES ────────────────────────────────────────────────────────────────────
box('btn-file-read').onclick = async () => {
    const r = await api('GET', '/api/files/read', { filename: box('file-read-input').value });
    show(box('file-read-resp'), r.data, !r.ok);
};

box('btn-file-view').onclick = async () => {
    const r = await api('GET', '/api/files/view', { path: box('file-view-input').value });
    show(box('file-view-resp'), r.data, !r.ok);
};

box('btn-file-list').onclick = async () => {
    const r = await api('GET', '/api/files/list', { dir: box('file-list-input').value });
    show(box('file-list-resp'), r.data, !r.ok);
};

// ── CONTENT ───────────────────────────────────────────────────────────────────
box('btn-echo').onclick = async () => {
    const r = await api('GET', '/api/content/echo', { message: box('echo-input').value });
    const el = box('echo-resp');
    el.classList.remove('empty', 'error');
    el.innerHTML = r.ok
        ? '<iframe srcdoc="' + r.data.replace(/"/g, '&quot;') + '" style="width:100%;height:120px;border:none;background:#fff;border-radius:4px"></iframe>'
        : '<span style="color:#f87171">' + r.data + '</span>';
};

box('btn-search').onclick = async () => {
    const r = await api('GET', '/api/content/search', { q: box('search-input').value });
    const el = box('search-resp');
    el.classList.remove('empty', 'error');
    el.innerHTML = r.ok
        ? '<iframe srcdoc="' + r.data.replace(/"/g, '&quot;') + '" style="width:100%;height:120px;border:none;background:#fff;border-radius:4px"></iframe>'
        : '<span style="color:#f87171">' + r.data + '</span>';
};

box('btn-greet').onclick = async () => {
    const r = await api('GET', '/api/content/greet', { name: box('greet-input').value });
    const el = box('greet-resp');
    el.classList.remove('empty', 'error');
    el.innerHTML = r.ok
        ? '<iframe srcdoc="' + r.data.replace(/"/g, '&quot;') + '" style="width:100%;height:90px;border:none;background:#fff;border-radius:4px"></iframe>'
        : '<span style="color:#f87171">' + r.data + '</span>';
};

box('btn-comment').onclick = async () => {
    const r = await api('POST', '/api/content/comment', {},
        { comment: box('comment-input').value, author: box('comment-author').value });
    const el = box('comment-resp');
    el.classList.remove('empty', 'error');
    el.innerHTML = r.ok
        ? '<iframe srcdoc="' + r.data.replace(/"/g, '&quot;') + '" style="width:100%;height:100px;border:none;background:#fff;border-radius:4px"></iframe>'
        : '<span style="color:#f87171">' + r.data + '</span>';
};
