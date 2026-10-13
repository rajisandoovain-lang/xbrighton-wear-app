const express = require('express');
const app = express();
const port = process.env.PORT || 3000;

app.use(express.json());

const products = [
  {
    id: 'shoes-001',
    name: 'Galaxy Runner X',
    category: 'Shoes',
    price: 129.99,
    color: 'Midnight Blue',
    size: '8',
    image: 'https://images.unsplash.com/...'
  },
  {
    id: 'clothes-001',
    name: 'Aurora Street Jacket',
    category: 'Clothes',
    price: 189.00,
    color: 'Stone Grey',
    size: 'M',
    image: 'https://images.unsplash.com/...'
  },
  {
    id: 'clothes-002',
    name: 'Velocity Knit Set',
    category: 'Clothes',
    price: 149.00,
    color: 'Ivory',
    size: 'L',
    image: 'https://images.unsplash.com/...'
  }
];

app.get('/products', (req, res) => {
  res.json({ products });
});

app.get('/cart', (req, res) => {
  res.json({ cart: [{ id: 'shoes-001', quantity: 1 }] });
});

app.post('/cart', (req, res) => {
  const { productId, quantity = 1 } = req.body;
  res.json({ ok: true, productId, quantity, message: 'Saved to cart' });
});

app.get('/ai-teacher', (req, res) => {
  res.json({
    response: 'For a modern streetwear look, pair the Aurora Street Jacket with slim dark trousers and the Galaxy Runner X sneakers. For sizing, choose a half-size up if you prefer a roomier feel.'
  });
});

app.get('/market', (req, res) => {
  res.json({
    tickers: [
      { symbol: 'XBT', price: 68420.12, change: 1.84 },
      { symbol: 'NVDA', price: 118.54, change: 2.14 },
      { symbol: 'AAPL', price: 225.91, change: -0.42 }
    ]
  });
});

app.listen(port, () => {
  console.log(`XBrighton backend running on http://localhost:${port}`);
});
