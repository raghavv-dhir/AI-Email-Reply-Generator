const DEFAULT_BACKEND_URL = 'http://localhost:8080';

/**
 * Retrieves the configured backend URL from extension storage.
 */
async function getBackendUrl() {
  return new Promise((resolve) => {
    chrome.storage.sync.get({ backendUrl: DEFAULT_BACKEND_URL }, (items) => {
      resolve(items.backendUrl.replace(/\/$/, ''));
    });
  });
}

/**
 * Calls the backend API to generate an email reply.
 */
async function generateReply(payload) {
  const backendUrl = await getBackendUrl();
  const response = await fetch(`${backendUrl}/api/v1/email/generate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });

  if (!response.ok) {
    let message = 'Failed to generate reply';
    try {
      const errorBody = await response.json();
      message = errorBody.message || message;
    } catch {
      // use default message
    }
    throw new Error(message);
  }

  return response.json();
}

chrome.runtime.onMessage.addListener((message, _sender, sendResponse) => {
  if (message.type === 'GENERATE_REPLY') {
    generateReply(message.payload)
      .then((data) => sendResponse({ success: true, data }))
      .catch((error) => sendResponse({ success: false, error: error.message }));
    return true;
  }

  if (message.type === 'GET_BACKEND_URL') {
    getBackendUrl().then((url) => sendResponse({ backendUrl: url }));
    return true;
  }
});
