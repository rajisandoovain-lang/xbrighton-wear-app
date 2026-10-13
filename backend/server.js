const express = require('express');
const axios = require('axios');
require('dotenv').config();

const app = express();
const port = process.env.PORT || 3000;

app.use(express.json());

const mockProducts = [
  {
    id: 'shoes-001',
    name: 'Galaxy Runner X',
    category: 'Shoes',
    price: 129.99,
    description: 'Performance comfort',
    color: 'Midnight Blue',
    size: '8'
  },
  {
    id: 'clothes-001',
    name: 'Aurora Street Jacket',
    category: 'Clothes',
    price: 189.0,
    description: 'Urban premium',
    color: 'Stone Grey',
    size: 'M'
  },
  {
    id: 'clothes-002',
    name: 'Velocity Knit Set',
    category: 'Clothes',
    price: 149.0,
    description: 'Minimal luxury',
    color: 'Ivory',
    size: 'L'
  }
];

app.get('/products', async (req, res) => {
  try {
    const productsApiUrl = process.env.PRODUCTS_API_URL;
    if (productsApiUrl && productsApiUrl !== 'http://localhost:3000/products') {
      const response = await axios.get(productsApiUrl, {
        headers: { Accept: 'application/json' }
      });
      return res.json({ products: response.data.products || response.data });
    }

    return res.json({ products: mockProducts });
  } catch (error) {
    console.error('Product API fetch failed, falling back to mock data:', error.message);
    return res.json({ products: mockProducts });
  }
});

app.get('/ai-teacher', async (req, res) => {
  const apiKey = process.env.OPENAI_API_KEY;

  if (!apiKey || apiKey === 'your_openai_api_key_here') {
    return res.json({
      response: 'AI Teacher is ready for real integration. Add your OpenAI key to backend/.env to enable live responses.'
    });
  }

  try {
    const systemMessage = 'You are an expert fashion styling assistant for a premium apparel and shoe brand.';
    const userPrompt = req.query.prompt || 'Recommend a premium streetwear outfit with shoes and jacket.';

    const response = await axios.post(
      'https://api.openai.com/v1/chat/completions',
      {
        model: 'gpt-4o-mini',
        messages: [
          { role: 'system', content: systemMessage },
          { role: 'user', content: userPrompt }
        ],
        temperature: 0.7
      },
      {
        headers: {
          Authorization: `Bearer ${apiKey}`,
          'Content-Type': 'application/json'
        }
      }
    );

    const text = response.data.choices?.[0]?.message?.content || 'AI recommendation unavailable.';
    return res.json({ response: text.trim() });
  } catch (error) {
    console.error('OpenAI call failed:', error.response?.data || error.message);
    return res.json({
      response: 'AI Teacher is using fallback guidance. Add a valid OpenAI key to enable live responses.'
    });
  }
});

app.get('/market', async (req, res) => {
  const apiKey = process.env.ALPHAVANTAGE_API_KEY;

  if (!apiKey || apiKey === 'your_alphavantage_api_key_here') {
    return res.json({
      tickers: [
        { symbol: 'XBT', price: 68420.12, change: 1.84 },
        { symbol: 'AAPL', price: 225.91, change: -0.42 },
        { symbol: 'NVDA', price: 118.54, change: 2.14 }
      ]
    });
  }

  try {
    const response = await axios.get('https://www.alphavantage.co/query', {
      params: {
        function: 'GLOBAL_QUOTE',
        symbol: req.query.symbol || 'IBM',
        apikey: apiKey
      }
    });

    const quote = response.data['Global Quote'] || {};
    const symbol = quote['01. symbol'] || req.query.symbol || 'IBM';
    const price = parseFloat(quote['05. price'] || 0);
    const change = parseFloat(quote['10. change percent']?.replace('%', '') || 0);

    return res.json({
      tickers: [{ symbol, price, change }]
    });
  } catch (error) {
    console.error('Alpha Vantage call failed:', error.response?.data || error.message);
    return res.json({
      tickers: [
        { symbol: 'XBT', price: 68420.12, change: 1.84 },
        { symbol: 'AAPL', price: 225.91, change: -0.42 },
        { symbol: 'NVDA', price: 118.54, change: 2.14 }
      ]
    });
  }
});

app.post('/cart', (req, res) => {
  const { productId, quantity = 1 } = req.body;
  res.json({ ok: true, productId, quantity, message: 'Saved to cart' });
});

app.get('/health', (req, res) => {
  res.json({ ok: true, status: 'healthy' });
});

app.listen(port, () => {
  console.log(`XBrighton backend running on http://localhost:${port}`);
});
