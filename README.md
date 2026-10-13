# XBrighton Wear App

A starter project for an XBrighton Technologies Galaxy Watch app focused on:
- fashion storefront for clothes and shoes
- AI Teacher assistant for style and sizing guidance
- trading ticker / market watch mock for a companion experience
- phone-to-watch sync for product browsing and cart handoff

## Architecture

- Mobile app: Android Kotlin shell (companion app)
- Wear app: Wear OS Kotlin + Jetpack Compose shell for Galaxy Watch
- Backend: Node.js mock API for products, AI assistant, and market data

## Repository layout

- `app/` — Android companion app module
- `wear/` — Wear OS app module
- `backend/` — mock backend with product and AI/trading endpoints
- `README.md` — setup instructions

## Quick start

### 1) Backend

```bash
cd backend
npm install
npm start
```

The mock API will run at:
- http://localhost:3000/products
- http://localhost:3000/cart
- http://localhost:3000/ai-teacher
- http://localhost:3000/market

### 2) Open in Android Studio

- Open the root folder in Android Studio
- Let Gradle sync download dependencies
- Run the `app` or `wear` configuration on an emulator or connected device

### 3) Wear app flow

- Product list screen
- Product detail screen
- AI Teacher assistant
- Market ticker / trading mock
- Save to cart / handoff to phone

## Notes

This is intentionally a starter scaffold. It is designed for easy extension into final backend integrations and real wearables deployment.

## Recommended next steps

1. Replace the mock catalog with your real product API
2. Add real authentication and account sync
3. Integrate a real LLM endpoint for the AI Teacher
4. Connect the trading screen to live or simulated market data
5. Add secure checkout and cart syncing with the phone app
6. Publish the wear app to the Google Play console for Galaxy Watch devices

## License

MIT
