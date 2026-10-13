const express = require('express');
const admin = require('firebase-admin');
require('dotenv').config();

const app = express();
const port = process.env.PORT || 3000;

app.use(express.json());

if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert({
      projectId: process.env.FIREBASE_PROJECT_ID,
      clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
      privateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n')
    })
  });
}

async function verifyToken(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Missing bearer token' });
  }

  try {
    const token = authHeader.split(' ')[1];
    const decodedToken = await admin.auth().verifyIdToken(token);
    req.user = decodedToken;
    next();
  } catch (error) {
    return res.status(401).json({ error: 'Invalid Firebase token' });
  }
}

app.get('/profile', verifyToken, (req, res) => {
  res.json({
    uid: req.user.uid,
    email: req.user.email || 'user@xbrighton.com',
    name: 'XBrighton User'
  });
});

app.get('/health', (req, res) => {
  res.json({ ok: true, status: 'healthy' });
});

app.listen(port, () => {
  console.log(`XBrighton auth backend ready on http://localhost:${port}`);
});
