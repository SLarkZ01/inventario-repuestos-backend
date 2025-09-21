# Next.js (React) quick guide

This document shows simple examples to call the backend from Next.js (both server and client), store tokens and call protected endpoints.

1) Login example (client-side using fetch):

```js
async function login(usernameOrEmail, password) {
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ usernameOrEmail, password })
  });
  if (!res.ok) throw new Error('Login failed');
  const data = await res.json();
  // store tokens (example: httpOnly cookie via server-side, or localStorage for demo)
  localStorage.setItem('accessToken', data.accessToken);
  localStorage.setItem('refreshToken', data.refreshToken);
  return data.user;
}
```

2) Fetch wrapper that attaches JWT and attempts refresh on 401

```js
async function apiFetch(url, opts = {}) {
  const token = localStorage.getItem('accessToken');
  const headers = { 'Content-Type': 'application/json', ...(opts.headers || {}) };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  let res = await fetch(url, { ...opts, headers });
  if (res.status === 401) {
    // try refresh
    const refresh = localStorage.getItem('refreshToken');
    if (refresh) {
      const r = await fetch('/api/auth/refresh', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ refreshToken: refresh })});
      if (r.ok) {
        const rb = await r.json();
        localStorage.setItem('accessToken', rb.accessToken);
        headers['Authorization'] = `Bearer ${rb.accessToken}`;
        res = await fetch(url, { ...opts, headers });
      }
    }
  }
  return res;
}
```

3) Example: create taller

```js
async function crearTaller(nombre) {
  const res = await apiFetch('/api/talleres', { method: 'POST', body: JSON.stringify({ nombre }) });
  if (!res.ok) throw new Error('No se pudo crear taller');
  return res.json();
}
```

Server-side notes:
- Prefer setting `refreshToken` in HttpOnly secure cookies from the server after login, to avoid storing refresh tokens in localStorage.
- On Next.js API routes (server-side) you can forward the `Authorization` header from the client, or perform token refresh server-side and then proxy requests to the backend.
