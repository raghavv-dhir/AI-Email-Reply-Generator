const DEFAULT_BACKEND_URL = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', () => {
  const backendUrlInput = document.getElementById('backendUrl');
  const saveBtn = document.getElementById('saveBtn');
  const statusEl = document.getElementById('status');

  chrome.storage.sync.get({ backendUrl: DEFAULT_BACKEND_URL }, (items) => {
    backendUrlInput.value = items.backendUrl;
  });

  saveBtn.addEventListener('click', () => {
    const url = backendUrlInput.value.trim().replace(/\/$/, '');

    if (!url || !/^https?:\/\/.+/.test(url)) {
      showStatus('Please enter a valid URL (http:// or https://)', 'error');
      return;
    }

    chrome.storage.sync.set({ backendUrl: url }, () => {
      showStatus('Settings saved successfully!', 'success');
    });
  });

  function showStatus(message, type) {
    statusEl.textContent = message;
    statusEl.className = `status ${type}`;
    if (type === 'success') {
      setTimeout(() => {
        statusEl.className = 'status';
      }, 2500);
    }
  }
});
