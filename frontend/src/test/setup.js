import '@testing-library/jest-dom';

// ── localStorage mock ─────────────────────────────────────────────────────
// jsdom v28 no longer provides a working in-memory localStorage by default.
// This polyfill restores the expected behaviour for all test files.
const createLocalStorageMock = () => {
  let store = {};
  return {
    getItem:    (key)        => (key in store ? store[key] : null),
    setItem:    (key, value) => { store[key] = String(value); },
    removeItem: (key)        => { delete store[key]; },
    clear:      ()           => { store = {}; },
    get length()             { return Object.keys(store).length; },
    key:        (i)          => Object.keys(store)[i] ?? null,
  };
};

Object.defineProperty(globalThis, 'localStorage', {
  value:        createLocalStorageMock(),
  writable:     true,
  configurable: true,
});

// ── window.location mock ──────────────────────────────────────────────────
// jsdom makes window.location non-configurable in some versions.
// Provide a simple writable stub so tests can assert on href changes.
Object.defineProperty(window, 'location', {
  value:        { href: '' },
  writable:     true,
  configurable: true,
});
