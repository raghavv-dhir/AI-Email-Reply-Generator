# AI Email Reply Generator

A production-ready AI-powered Gmail reply assistant consisting of a **Spring Boot 3.x backend** (Java 21) and a **Chrome Extension (Manifest V3)** integrated with **Groq AI** (Llama 3.3).

The Chrome extension injects an **AI Reply** button directly into Gmail compose and reply windows. Select a tone, and Groq generates a contextual reply that is inserted into your compose editor.

---

## Features

- Gmail compose/reply/forward/new message detection via MutationObserver
- 10 reply tones: Professional, Friendly, Casual, Formal, Concise, Detailed, Persuasive, Apologetic, Thank You, Follow Up
- Groq AI integration (Llama 3.3 70B) via Spring WebClient
- CORS restricted to Chrome extension origins
- Input validation, request size limits, and secure error handling
- Gmail light and dark theme support
- Loading spinner, error messages, regenerate, and copy actions
- Unit and integration tests

---

## Project Structure

```
AI-Email-Reply-Generator/
├── backend/                    # Spring Boot API
│   └── src/main/java/com/emailgenerator/
│       ├── config/
│       ├── controller/
│       ├── service/
│       ├── client/
│       ├── dto/
│       ├── entity/
│       ├── exception/
│       └── util/
├── chrome-extension/           # Manifest V3 Chrome Extension
│   ├── manifest.json
│   ├── content.js
│   ├── background.js
│   ├── popup.html
│   ├── popup.js
│   ├── styles.css
│   └── icons/
├── postman/                    # Postman API collection
├── .env.example
└── README.md
```

---

## Prerequisites

| Requirement | Version |
|---|---|
| Java JDK | 21+ |
| Maven | 3.8+ |
| Google Chrome | Latest |
| Groq API Key | [Groq Console](https://console.groq.com/keys) |

---

## Installation Guide

### 1. Clone the Repository

```bash
git clone <repository-url>
cd AI-Email-Reply-Generator
```

### 2. Configure Environment Variables

Copy the example environment file:

```bash
cp .env.example .env
```

Edit `.env` and set your Groq credentials:

```env
GROQ_API_KEY=YOUR_GROQ_API_KEY_HERE
GROQ_API_URL=https://api.groq.com/openai/v1/chat/completions
GROQ_MODEL=llama-3.3-70b-versatile
```

> **Important:** Never commit your `.env` file or expose your API key.

---

## Backend Run Guide

### Set Environment Variables

**Windows (PowerShell):**

```powershell
$env:GROQ_API_KEY="YOUR_GROQ_API_KEY"
$env:GROQ_API_URL="https://api.groq.com/openai/v1/chat/completions"
```

**Linux / macOS:**

```bash
export GROQ_API_KEY="YOUR_GROQ_API_KEY"
export GROQ_API_URL="https://api.groq.com/openai/v1/chat/completions"
```

### Build and Run

```bash
cd backend
mvn clean package
mvn spring-boot:run
```

The backend starts on `http://localhost:8080`.

### Verify Health

```bash
curl http://localhost:8080/actuator/health
```

Expected response:

```json
{"status":"UP"}
```

### API Endpoint

**POST** `/api/v1/email/generate`

Request:

```json
{
  "emailContent": "Can we meet next Tuesday?",
  "subject": "Meeting Request",
  "senderName": "Jane Doe",
  "tone": "PROFESSIONAL"
}
```

Response:

```json
{
  "generatedReply": "Thank you for reaching out..."
}
```

Supported tones: `PROFESSIONAL`, `FRIENDLY`, `CASUAL`, `FORMAL`, `CONCISE`, `DETAILED`, `PERSUASIVE`, `APOLOGETIC`, `THANK_YOU`, `FOLLOW_UP`

### Run Tests

```bash
cd backend
mvn test
```

---

## Chrome Extension Setup Guide

### 1. Load the Extension

1. Open Chrome and navigate to `chrome://extensions/`
2. Enable **Developer mode** (top-right toggle)
3. Click **Load unpacked**
4. Select the `chrome-extension/` folder from this project

### 2. Configure Backend URL

1. Click the extension icon in the Chrome toolbar
2. Set the **Backend URL** to `http://localhost:8080`
3. Click **Save Settings**

### 3. Use in Gmail

1. Open [Gmail](https://mail.google.com)
2. Open an email and click **Reply**, **Reply All**, **Forward**, or **Compose**
3. Look for the **AI Reply** button in the compose toolbar
4. Click **AI Reply** and select a tone
5. The generated reply is inserted into the compose editor

### Extension Features

- **Regenerate** — Re-generate using the last selected tone
- **Copy** — Copy the last generated reply to clipboard
- Works with multiple compose windows simultaneously
- Supports Gmail light and dark themes
- Uses subtle Material Design UI and robust dropdown handling to avoid Gmail CSS conflicts

---

## Postman Collection

Import the collection from:

```
postman/AI-Email-Reply-Generator.postman_collection.json
```

Set the `baseUrl` variable to `http://localhost:8080` and run the requests.

---

## Security

- API keys are read only from environment variables
- API keys are masked in logs (last 4 characters visible)
- CORS is restricted to `chrome-extension://*` origins
- Internal stack traces are never exposed to clients
- Request body size is limited to 1 MB
- Email content is limited to 50,000 characters

---

## Troubleshooting

| Issue | Solution |
|---|---|
| Backend fails to start | Ensure `GROQ_API_KEY` is set |
| AI Reply button not visible | Refresh Gmail; ensure extension is enabled |
| Generation fails | Verify backend is running at the configured URL |
| CORS errors | Ensure requests go through the background service worker |
| Empty reply | Check Groq API key validity and quota |

---

## License

MIT
