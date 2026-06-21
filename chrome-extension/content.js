const TONES = [
  { label: 'Professional', value: 'PROFESSIONAL' },
  { label: 'Friendly', value: 'FRIENDLY' },
  { label: 'Casual', value: 'CASUAL' },
  { label: 'Formal', value: 'FORMAL' },
  { label: 'Concise', value: 'CONCISE' },
  { label: 'Detailed', value: 'DETAILED' },
  { label: 'Persuasive', value: 'PERSUASIVE' },
  { label: 'Apologetic', value: 'APOLOGETIC' },
  { label: 'Thank You', value: 'THANK_YOU' },
  { label: 'Follow Up', value: 'FOLLOW_UP' }
];

const INJECTED_ATTR = 'data-ai-reply-injected';
const EDITOR_SELECTORS = ['[role="textbox"][g_editable="true"]', '[role="textbox"][contenteditable="true"]', '.Am.Al.editable', '[contenteditable="true"]'];
const TOOLBAR_SELECTORS = ['.gU.Up', '.btC', '.aZ6', '[role="toolbar"]', '.G6', '.aDh', '.gqf', '.aKb'];

let lastGeneratedReply = null;
let lastTone = null;

// Inject styles for the AI Reply button and dropdown to make it larger, cleaner and left-aligned
function injectAiStyles() {
  if (document.getElementById('ai-reply-styles')) return;
  const style = document.createElement('style');
  style.id = 'ai-reply-styles';
  style.textContent = `
    .ai-reply-container {
      position: relative;
      display: inline-block;
      margin-right: 12px;
      vertical-align: middle;
    }
    .ai-reply-btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      padding: 0 16px;
      height: 36px;
      font-family: 'Google Sans', Roboto, Arial, sans-serif;
      font-size: 14px;
      font-weight: 500;
      color: #0b57d0;
      background: #e8f0fe;
      border-radius: 18px;
      border: none;
      cursor: pointer;
      transition: all 0.15s ease;
      min-width: 104px;
    }
    .ai-reply-btn:hover {
      background: #d3e3fd;
      box-shadow: 0 1px 2px rgba(0,0,0,0.1);
    }
    .ai-reply-btn:active {
      background: #c2d7fa;
      box-shadow: none;
    }
    .ai-reply-btn.loading {
      opacity: 0.7;
      pointer-events: none;
      cursor: not-allowed;
    }

    .ai-reply-dropdown {
      display: none;
      position: absolute;
      top: calc(100% + 8px);
      left: 0;
      z-index: 100000;
      background: #ffffff;
      border-radius: 12px;
      padding: 8px;
      box-shadow: 0 4px 16px rgba(0,0,0,0.15), 0 1px 3px rgba(0,0,0,0.1);
      border: 1px solid #e0e0e0;
      min-width: 180px;
      max-height: 350px;
      overflow-y: auto;
      font-family: 'Google Sans', Roboto, Arial, sans-serif;
    }
    .ai-reply-dropdown.open {
      display: block;
      animation: ai-fade-down 0.2s ease-out forwards;
    }

    @keyframes ai-fade-down {
      from { opacity: 0; transform: translateY(-10px); }
      to { opacity: 1; transform: translateY(0); }
    }

    .ai-reply-tone-item {
      display: block;
      width: 100%;
      padding: 10px 14px;
      text-align: left;
      border-radius: 8px;
      background: transparent;
      border: none;
      cursor: pointer;
      color: #202124 !important;
      font-size: 13px;
      font-weight: 500;
      transition: background 0.15s ease;
    }
    .ai-reply-tone-item:hover {
      background: #f1f3f4;
    }

    .ai-reply-actions {
      display: flex;
      gap: 8px;
      margin-top: 8px;
      padding-top: 8px;
      border-top: 1px solid #e8eaed;
    }
    .ai-reply-action-btn {
      flex: 1;
      padding: 8px;
      border-radius: 6px;
      border: none;
      background: #f8f9fa;
      cursor: pointer;
      font-weight: 500;
      color: #1a73e8 !important;
      font-size: 13px;
      transition: all 0.15s ease;
    }
    .ai-reply-action-btn:hover {
      background: #e8f0fe;
    }

    .ai-reply-notification {
      position: absolute;
      top: calc(100% + 8px);
      left: 0;
      white-space: nowrap;
      padding: 8px 12px;
      border-radius: 8px;
      font-size: 12px;
      font-weight: 500;
      z-index: 100000;
      box-shadow: 0 2px 6px rgba(0,0,0,0.15);
    }
    .ai-reply-notification-success { background:#e6f4ea; color:#137333; }
    .ai-reply-notification-error { background:#fce8e6; color:#b91c1c; }
  `;
  document.head?.appendChild(style);
}
function insertReplyIntoEditor(composeWindow, replyText) {
  const editor = composeWindow.querySelector('[role="textbox"][g_editable="true"], .Am.Al.editable, [contenteditable="true"]');
  if (!editor) {
    throw new Error('Could not find compose editor');
  }

  editor.focus();

  const htmlContent = replyText
    .split('\n')
    .map((line) => line.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;'))
    .join('<br>');

  document.execCommand('selectAll', false, null);
  document.execCommand('insertHTML', false, htmlContent);

  editor.dispatchEvent(new Event('input', { bubbles: true }));
}

function setLoading(button, isLoading) {
  if (isLoading) {
    button.classList.add('loading');
    button.disabled = true;
  } else {
    button.classList.remove('loading');
    button.disabled = false;
  }
}

function closeAllDropdowns() {
  document.querySelectorAll('.ai-reply-dropdown.open').forEach((dropdown) => {
    dropdown.classList.remove('open');
  });
  document.querySelectorAll('.ai-reply-btn[aria-expanded="true"]').forEach((btn) => {
    btn.setAttribute('aria-expanded', 'false');
  });
}

function showNotification(composeWindow, message, type) {
  hideNotification(composeWindow);

  const notification = document.createElement('div');
  notification.className = `ai-reply-notification ai-reply-notification-${type}`;
  notification.textContent = message;

  const container = composeWindow.querySelector('.ai-reply-container') || composeWindow;
  container.appendChild(notification);

  if (type === 'success') {
    setTimeout(() => hideNotification(composeWindow), 3000);
  }
}

function hideNotification(composeWindow) {
  composeWindow.querySelectorAll('.ai-reply-notification').forEach((el) => el.remove());
}

// Safe wrapper to send messages to the background script and handle cases
// where the extension context is unavailable or the background errors.
function sendMessageToBackground(message, timeoutMs = 8000) {
  return new Promise((resolve, reject) => {
    try {
      if (!window.chrome || !chrome.runtime || !chrome.runtime.sendMessage) {
        return reject(new Error('Extension runtime unavailable'));
      }

      let settled = false;
      const timer = setTimeout(() => {
        if (settled) return;
        settled = true;
        reject(new Error('Extension did not respond (timeout)'));
      }, timeoutMs);

      chrome.runtime.sendMessage(message, (response) => {
        if (settled) return;
        settled = true;
        clearTimeout(timer);
        if (chrome.runtime.lastError) {
          return reject(new Error(chrome.runtime.lastError.message || 'Extension messaging error'));
        }
        resolve(response);
      });
    } catch (e) {
      reject(e);
    }
  });
}

function init() {
  injectAiStyles();
  observeGmail();
  scanForComposeWindows();
  setInterval(scanForComposeWindows, 2500);
  document.addEventListener('click', (event) => {
    if (!event.target.closest('.ai-reply-container')) {
      closeAllDropdowns();
    }
  });
}

init();

function observeGmail() {
  const observer = new MutationObserver(() => {
    scanForComposeWindows();
  });

  observer.observe(document.body, {
    childList: true,
    subtree: true
  });
}

function scanForComposeWindows() {
  findComposeWindows().forEach((composeWindow) => {
    if (composeWindow.getAttribute(INJECTED_ATTR) === 'true') {
      return;
    }
    const toolbar = findToolbar(composeWindow);
    if (toolbar && !toolbar.querySelector('.ai-reply-container')) {
      injectAiReplyButton(composeWindow, toolbar);
      composeWindow.setAttribute(INJECTED_ATTR, 'true');
    }
  });
}

function findComposeWindows() {
  const windows = new Set();
  const editors = document.querySelectorAll(EDITOR_SELECTORS.join(', '));
  
  editors.forEach(editor => {
    let container = editor.closest('[role="dialog"]');
    
    if (!container) {
      // For inline replies, find the closest ancestor that contains a toolbar
      let curr = editor.parentElement;
      while (curr && curr !== document.body) {
        if (TOOLBAR_SELECTORS.some(sel => curr.querySelector(sel))) {
          container = curr;
          break;
        }
        curr = curr.parentElement;
      }
    }
    
    if (container) {
      windows.add(container);
    } else {
      windows.add(editor.parentElement);
    }
  });

  return Array.from(windows);
}

function findToolbar(composeWindow) {
  for (const selector of TOOLBAR_SELECTORS) {
    const toolbar = composeWindow.querySelector(selector);
    if (toolbar) {
      return toolbar;
    }
  }

  const editor = composeWindow.querySelector(EDITOR_SELECTORS.join(', '));
  if (editor) {
    const dialogToolbar = editor.closest('[role="dialog"]')?.querySelector('[role="toolbar"]');
    if (dialogToolbar) {
      return dialogToolbar;
    }
    return editor.parentElement || composeWindow;
  }

  return composeWindow;
}

function injectAiReplyButton(composeWindow, toolbar) {
  const container = document.createElement('div');
  container.className = 'ai-reply-container';

  const button = document.createElement('button');
  button.type = 'button';
  button.className = 'ai-reply-btn';
  button.innerHTML = '✨ AI Reply';
  button.setAttribute('aria-haspopup', 'true');
  button.setAttribute('aria-expanded', 'false');

  const dropdown = createToneDropdown(composeWindow, button);
  container.appendChild(button);
  container.appendChild(dropdown);
  toolbar.insertBefore(container, toolbar.firstChild);

  button.addEventListener('click', (event) => {
    event.stopPropagation();
    const isOpen = dropdown.classList.contains('open');
    closeAllDropdowns();
    if (!isOpen) {
      dropdown.classList.add('open');
      button.setAttribute('aria-expanded', 'true');
    }
  });

  document.addEventListener('click', (event) => {
    if (!container.contains(event.target)) {
      dropdown.classList.remove('open');
      button.setAttribute('aria-expanded', 'false');
    }
  });
}

function createToneDropdown(composeWindow, button) {
  const dropdown = document.createElement('div');
  dropdown.className = 'ai-reply-dropdown';
  dropdown.setAttribute('role', 'menu');

  TONES.forEach((tone) => {
    const item = document.createElement('div');
    item.className = 'ai-reply-tone-item';
    item.textContent = tone.label;
    item.setAttribute('role', 'menuitem');
    item.dataset.tone = tone.value;

    item.addEventListener('click', async (event) => {
      event.stopPropagation();
      dropdown.classList.remove('open');
      button.setAttribute('aria-expanded', 'false');
      await handleToneSelection(composeWindow, tone.value, button);
    });

    dropdown.appendChild(item);
  });

  addActionButtons(dropdown, composeWindow, button);
  return dropdown;
}

function addActionButtons(dropdown, composeWindow, button) {
  const actions = document.createElement('div');
  actions.className = 'ai-reply-actions';

  const regenerateBtn = document.createElement('div');
  regenerateBtn.className = 'ai-reply-action-btn';
  regenerateBtn.textContent = 'Regenerate';
  regenerateBtn.addEventListener('click', async (event) => {
    event.stopPropagation();
    if (lastTone) {
      await handleToneSelection(composeWindow, lastTone, button);
    }
  });

  const copyBtn = document.createElement('div');
  copyBtn.className = 'ai-reply-action-btn';
  copyBtn.textContent = 'Copy';
  copyBtn.addEventListener('click', async (event) => {
    event.stopPropagation();
    if (lastGeneratedReply) {
      await navigator.clipboard.writeText(lastGeneratedReply);
      showNotification(composeWindow, 'Copied to clipboard!', 'success');
    }
  });

  actions.appendChild(regenerateBtn);
  actions.appendChild(copyBtn);
  dropdown.appendChild(actions);
}

async function handleToneSelection(composeWindow, tone, button) {
  const emailData = extractEmailData(composeWindow);
  lastTone = tone;

  setLoading(button, true);
  hideNotification(composeWindow);

  try {
    const response = await sendMessageToBackground({
      type: 'GENERATE_REPLY',
      payload: {
        emailContent: emailData.emailContent,
        subject: emailData.subject,
        senderName: emailData.senderName,
        tone
      }
    });

    if (!response?.success) {
      throw new Error(response?.error || 'Generation failed');
    }

    lastGeneratedReply = response.data.generatedReply;
    insertReplyIntoEditor(composeWindow, lastGeneratedReply);
    showNotification(composeWindow, 'Reply generated successfully!', 'success');
  } catch (error) {
    showNotification(composeWindow, error.message || 'Failed to generate reply', 'error');
  } finally {
    setLoading(button, false);
  }
}

function extractEmailData(composeWindow) {
  const subject = extractSubject(composeWindow);
  const senderName = extractSenderName();
  const threadContent = extractThreadContent();
  const latestMessage = extractLatestMessage();

  const emailContent = [threadContent, latestMessage]
    .filter(Boolean)
    .join('\n\n---\n\n')
    .trim() || 'No email content available';

  return { emailContent, subject, senderName };
}

function extractSubject(composeWindow) {
  const subjectInput = composeWindow.querySelector('input[name="subjectbox"]')
    || document.querySelector('input[name="subjectbox"]')
    || document.querySelector('[data-thread-perm-id] h2');

  if (subjectInput?.value) {
    return subjectInput.value.trim();
  }
  if (subjectInput?.textContent) {
    return subjectInput.textContent.trim();
  }
  return '';
}

function extractSenderName() {
  const senderEl = document.querySelector('.gD[email], .go[email], .gD .gb, span[email]');
  if (senderEl) {
    return (senderEl.getAttribute('name') || senderEl.textContent || '').trim();
  }
  return '';
}

function extractThreadContent() {
  const messageBodies = document.querySelectorAll('.a3s.aiL, .ii.gt');
  if (messageBodies.length === 0) {
    return '';
  }

  const parts = [];
  messageBodies.forEach((body, index) => {
    const text = body.innerText?.trim();
    if (text) {
      parts.push(`Message ${index + 1}:\n${text}`);
    }
  });

  return parts.join('\n\n');
}

function extractLatestMessage() {
  const bodies = document.querySelectorAll('.a3s.aiL, .ii.gt');
  if (bodies.length === 0) {
    return '';
  }
  return bodies[bodies.length - 1].innerText?.trim() || '';
}


