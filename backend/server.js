const express = require('express');
const axios = require('axios');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const sqlite3 = require('sqlite3').verbose();
const crypto = require('crypto');
require('dotenv').config();

const app = express();
const port = process.env.PORT || 3000;
const JWT_SECRET = process.env.JWT_SECRET || 'xbrighton-dev-secret-change-me';
const DB_PATH = process.env.DB_PATH || './xbrighton.db';

app.use(express.json());
app.use((req, res, next) => {
  res.header('Access-Control-Allow-Origin', '*');
  res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept, Authorization');
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  if (req.method === 'OPTIONS') {
    return res.sendStatus(200);
  }
  next();
});

const db = new sqlite3.Database(DB_PATH, (err) => {
  if (err) {
    console.error('Failed to connect to SQLite DB:', err.message);
  } else {
    db.run(`
      CREATE TABLE IF NOT EXISTS users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        email TEXT UNIQUE NOT NULL,
        password_hash TEXT NOT NULL,
        created_at TEXT DEFAULT CURRENT_TIMESTAMP
      )
    `, (tableErr) => {
      if (tableErr) console.error('Unable to create users table:', tableErr.message);
    });

    db.run(`
      CREATE TABLE IF NOT EXISTS refresh_tokens (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        token_hash TEXT NOT NULL,
        revoked INTEGER DEFAULT 0,
        created_at TEXT DEFAULT CURRENT_TIMESTAMP,
        UNIQUE(user_id, token_hash)
      )
    `, (tableErr) => {
      if (tableErr) console.error('Unable to create refresh_tokens table:', tableErr.message);
    });

    db.run(`
      CREATE TABLE IF NOT EXISTS cart_items (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        product_id TEXT NOT NULL,
        name TEXT NOT NULL,
        price REAL NOT NULL,
        quantity INTEGER DEFAULT 1,
        created_at TEXT DEFAULT CURRENT_TIMESTAMP
      )
    `, (tableErr) => {
      if (tableErr) console.error('Unable to create cart_items table:', tableErr.message);
    });

    db.run(`
      CREATE TABLE IF NOT EXISTS favorites (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        user_id INTEGER NOT NULL,
        product_id TEXT NOT NULL,
        created_at TEXT DEFAULT CURRENT_TIMESTAMP,
        UNIQUE(user_id, product_id)
      )
    `, (tableErr) => {
      if (tableErr) console.error('Unable to create favorites table:', tableErr.message);
    });
  }
});

const mockProducts = [
  { id: 'shoes-001', name: 'Galaxy Runner X', category: 'Shoes', price: 129.99, description: 'Performance comfort', color: 'Midnight Blue', size: '8' },
  { id: 'clothes-001', name: 'Aurora Street Jacket', category: 'Clothes', price: 189.0, description: 'Urban premium', color: 'Stone Grey', size: 'M' },
  { id: 'clothes-002', name: 'Velocity Knit Set', category: 'Clothes', price: 149.0, description: 'Minimal luxury', color: 'Ivory', size: 'L' }
];

const run = (sql, params = []) => new Promise((resolve, reject) => {
  db.run(sql, params, function (err) {
    if (err) return reject(err);
    resolve({ id: this.lastID, changes: this.changes });
  });
});

const get = (sql, params = []) => new Promise((resolve, reject) => {
  db.get(sql, params, (err, row) => {
    if (err) return reject(err);
    resolve(row || null);
  });
});

const all = (sql, params = []) => new Promise((resolve, reject) => {
  db.all(sql, params, (err, rows) => {
    if (err) return reject(err);
    resolve(rows || []);
  });
});

const hashToken = (token) => crypto.createHash('sha256').update(token).digest('hex');

function signToken(user, type = 'access') {
  const expiresIn = type === 'refresh' ? '7d' : '15m';
  return jwt.sign({ sub: user.id, email: user.email, type }, JWT_SECRET, { expiresIn });
}

async function saveRefreshToken(user, refreshToken) {
  const tokenHash = hashToken(refreshToken);
  await run('INSERT OR IGNORE INTO refresh_tokens (user_id, token_hash, revoked) VALUES (?, ?, 0)', [user.id, tokenHash]);
}

async function revokeRefreshToken(refreshToken) {
  if (!refreshToken) return;
  const tokenHash = hashToken(refreshToken);
  await run('UPDATE refresh_tokens SET revoked = 1 WHERE token_hash = ?', [tokenHash]);
}

async function revokeAllRefreshTokensForUser(userId) {
  await run('UPDATE refresh_tokens SET revoked = 1 WHERE user_id = ?', [userId]);
}

function createAuthPayload(user, refreshToken) {
  return {
    accessToken: signToken(user, 'access'),
    refreshToken: refreshToken || signToken(user, 'refresh'),
    user: { id: user.id, email: user.email }
  };
}

async function requireAuth(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Missing bearer token' });
  }

  try {
    const token = authHeader.split(' ')[1];
    const payload = jwt.verify(token, JWT_SECRET);
    if (payload.type !== 'access') {
      return res.status(401).json({ error: 'Invalid token type' });
    }

    const user = await get('SELECT id, email FROM users WHERE id = ?', [payload.sub]);
    if (!user) {
      return res.status(401).json({ error: 'User not found' });
    }

    req.user = user;
    next();
  } catch (error) {
    return res.status(401).json({ error: 'Invalid or expired token' });
  }
}

app.post('/auth/register', async (req, res) => {
  const { email, password } = req.body || {};
  if (!email || !password) {
    return res.status(400).json({ error: 'Email and password are required' });
  }

  const normalizedEmail = String(email).trim().toLowerCase();
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalizedEmail)) {
    return res.status(400).json({ error: 'Valid email is required' });
  }

  if (String(password).length < 6) {
    return res.status(400).json({ error: 'Password must be at least 6 characters' });
  }

  try {
    const existing = await get('SELECT id FROM users WHERE email = ?', [normalizedEmail]);
    if (existing) {
      return res.status(409).json({ error: 'User already exists' });
    }

    const passwordHash = await bcrypt.hash(password, 10);
    const result = await run('INSERT INTO users (email, password_hash) VALUES (?, ?)', [normalizedEmail, passwordHash]);
    const user = await get('SELECT id, email FROM users WHERE id = ?', [result.id]);
    const refreshToken = signToken(user, 'refresh');
    await saveRefreshToken(user, refreshToken);

    return res.status(201).json({ message: 'User registered successfully', ...createAuthPayload(user, refreshToken) });
  } catch (error) {
    console.error('Register failed:', error);
    return res.status(500).json({ error: 'Registration failed' });
  }
});

app.post('/auth/login', async (req, res) => {
  const { email, password } = req.body || {};
  if (!email || !password) {
    return res.status(400).json({ error: 'Email and password are required' });
  }

  const normalizedEmail = String(email).trim().toLowerCase();

  try {
    const user = await get('SELECT id, email, password_hash FROM users WHERE email = ?', [normalizedEmail]);
    if (!user) {
      return res.status(401).json({ error: 'Invalid credentials' });
    }

    const matches = await bcrypt.compare(String(password), user.password_hash);
    if (!matches) {
      return res.status(401).json({ error: 'Invalid credentials' });
    }

    const safeUser = { id: user.id, email: user.email };
    const refreshToken = signToken(safeUser, 'refresh');
    await saveRefreshToken(safeUser, refreshToken);

    return res.json({ message: 'Login successful', ...createAuthPayload(safeUser, refreshToken) });
  } catch (error) {
    console.error('Login error:', error);
    return res.status(500).json({ error: 'Login failed' });
  }
});

app.post('/auth/refresh', async (req, res) => {
  const { refreshToken } = req.body || {};
  if (!refreshToken) {
    return res.status(400).json({ error: 'Refresh token is required' });
  }

  try {
    const payload = jwt.verify(refreshToken, JWT_SECRET);
    if (payload.type !== 'refresh') {
      return res.status(401).json({ error: 'Invalid refresh token' });
    }

    const tokenHash = hashToken(refreshToken);
    const tokenRow = await get('SELECT id, user_id FROM refresh_tokens WHERE user_id = ? AND token_hash = ? AND revoked = 0', [payload.sub, tokenHash]);
    if (!tokenRow) {
      return res.status(401).json({ error: 'Refresh token revoked or invalid' });
    }

    const user = await get('SELECT id, email FROM users WHERE id = ?', [payload.sub]);
    if (!user) {
      return res.status(401).json({ error: 'User not found' });
    }

    await run('UPDATE refresh_tokens SET revoked = 1 WHERE id = ?', [tokenRow.id]);
    const newRefreshToken = signToken(user, 'refresh');
    await saveRefreshToken(user, newRefreshToken);

    return res.json({ accessToken: signToken(user, 'access'), refreshToken: newRefreshToken, user: { id: user.id, email: user.email } });
  } catch (error) {
    return res.status(401).json({ error: 'Refresh token expired or invalid' });
  }
});

app.post('/auth/logout', requireAuth, async (req, res) => {
  const { refreshToken } = req.body || {};
  if (refreshToken) {
    await revokeRefreshToken(refreshToken);
  }
  await revokeAllRefreshTokensForUser(req.user.id);
  return res.json({ message: 'Logged out successfully', user: req.user });
});

app.get('/profile', requireAuth, (req, res) => {
  return res.json({ user: req.user, message: 'Authenticated user profile' });
});

app.get('/products', async (req, res) => {
  try {
    const productsApiUrl = process.env.PRODUCTS_API_URL;
    if (productsApiUrl && productsApiUrl !== 'http://localhost:3000/products') {
      const response = await axios.get(productsApiUrl, { headers: { Accept: 'application/json' } });
      return res.json({ products: response.data.products || response.data });
    }
    return res.json({ products: mockProducts });
  } catch (error) {
    console.error('Product API fetch failed, falling back to mock data:', error.message);
    return res.json({ products: mockProducts });
  }
});

app.get('/cart', requireAuth, async (req, res) => {
  const cart = await all('SELECT * FROM cart_items WHERE user_id = ? ORDER BY created_at DESC', [req.user.id]);
  return res.json({ items: cart });
});

app.post('/cart', requireAuth, async (req, res) => {
  const { productId, name, price, quantity = 1 } = req.body || {};
  if (!productId || !name || !price) {
    return res.status(400).json({ error: 'productId, name, and price are required' });
  }

  const existing = await get('SELECT id FROM cart_items WHERE user_id = ? AND product_id = ?', [req.user.id, productId]);
  if (existing) {
    await run('UPDATE cart_items SET quantity = quantity + ? WHERE id = ?', [quantity, existing.id]);
    const updated = await get('SELECT * FROM cart_items WHERE id = ?', [existing.id]);
    return res.json({ ok: true, item: updated, message: 'Cart updated' });
  }

  const item = await run('INSERT INTO cart_items (user_id, product_id, name, price, quantity) VALUES (?, ?, ?, ?, ?)', [req.user.id, productId, name, price, quantity]);
  const created = await get('SELECT * FROM cart_items WHERE id = ?', [item.id]);
  return res.json({ ok: true, item: created, message: 'Item added to cart' });
});

app.delete('/cart/:id', requireAuth, async (req, res) => {
  const itemId = Number(req.params.id);
  if (!itemId) {
    return res.status(400).json({ error: 'Invalid cart item id' });
  }

  const item = await get('SELECT * FROM cart_items WHERE id = ? AND user_id = ?', [itemId, req.user.id]);
  if (!item) {
    return res.status(404).json({ error: 'Cart item not found' });
  }

  await run('DELETE FROM cart_items WHERE id = ? AND user_id = ?', [itemId, req.user.id]);
  return res.json({ ok: true, removed: item, message: 'Item removed from cart' });
});

app.get('/favorites', requireAuth, async (req, res) => {
  const items = await all('SELECT * FROM favorites WHERE user_id = ? ORDER BY created_at DESC', [req.user.id]);
  return res.json({ items });
});

app.post('/favorites', requireAuth, async (req, res) => {
  const { productId } = req.body || {};
  if (!productId) {
    return res.status(400).json({ error: 'productId is required' });
  }

  const existing = await get('SELECT id FROM favorites WHERE user_id = ? AND product_id = ?', [req.user.id, productId]);
  if (existing) {
    return res.json({ ok: true, message: 'Product already saved', item: existing });
  }

  const result = await run('INSERT INTO favorites (user_id, product_id) VALUES (?, ?)', [req.user.id, productId]);
  const created = await get('SELECT * FROM favorites WHERE id = ?', [result.id]);
  return res.status(201).json({ ok: true, item: created, message: 'Product saved to favorites' });
});

app.get('/market', async (req, res) => {
  const apiKey = process.env.ALPHAVANTAGE_API_KEY;
  if (!apiKey || apiKey === 'your_alphavantage_api_key_here') {
    return res.json({ tickers: [{ symbol: 'XBT', price: 68420.12, change: 1.84 }, { symbol: 'AAPL', price: 225.91, change: -0.42 }, { symbol: 'NVDA', price: 118.54, change: 2.14 }] });
  }

  try {
    const response = await axios.get('https://www.alphavantage.co/query', {
      params: { function: 'GLOBAL_QUOTE', symbol: req.query.symbol || 'IBM', apikey: apiKey }
    });

    const quote = response.data['Global Quote'] || {};
    const symbol = quote['01. symbol'] || req.query.symbol || 'IBM';
    const price = parseFloat(quote['05. price'] || 0);
    const change = parseFloat(quote['10. change percent']?.replace('%', '') || 0);

    return res.json({ tickers: [{ symbol, price, change }] });
  } catch (error) {
    console.error('Alpha Vantage call failed:', error.response?.data || error.message);
    return res.json({ tickers: [{ symbol: 'XBT', price: 68420.12, change: 1.84 }, { symbol: 'AAPL', price: 225.91, change: -0.42 }, { symbol: 'NVDA', price: 118.54, change: 2.14 }] });
  }
});

app.get('/health', (req, res) => {
  res.json({ ok: true, status: 'healthy' });
});

app.listen(port, () => {
  console.log(`XBrighton backend running on http://localhost:${port}`);
});
