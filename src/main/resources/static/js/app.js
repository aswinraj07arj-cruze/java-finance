/**
 * Finance Tracker PWA Application Controller
 * Handles Service Worker registration, native installation prompt,
 * network status monitoring, and offline form protection.
 */

(function () {
    'use strict';

    let deferredPrompt = null;
    const installBtn = document.getElementById('pwa-install-btn');
    const offlineIndicator = document.getElementById('offline-indicator');

    // 1. Check if application is already running in standalone PWA mode
    const isStandalone = window.matchMedia('(display-mode: standalone)').matches ||
                         window.navigator.standalone === true;

    // 2. Service Worker Registration
    if ('serviceWorker' in navigator) {
        window.addEventListener('load', () => {
            navigator.serviceWorker.register('/sw.js', { scope: '/' })
                .then((registration) => {
                    // Check for worker updates
                    registration.onupdatefound = () => {
                        const installingWorker = registration.installing;
                        if (installingWorker) {
                            installingWorker.onstatechange = () => {
                                if (installingWorker.state === 'installed' && navigator.serviceWorker.controller) {
                                    showUpdateToast();
                                }
                            };
                        }
                    };
                })
                .catch((error) => {
                    console.error('[PWA] Service Worker registration failed:', error);
                });
        });
    }

    // 3. Native PWA Install Prompt Handling
    window.addEventListener('beforeinstallprompt', (e) => {
        // Prevent default mini-infobar on mobile Chrome
        e.preventDefault();
        deferredPrompt = e;

        // Show install button if available and not already installed
        if (installBtn && !isStandalone) {
            installBtn.style.display = 'inline-flex';
            installBtn.onclick = handleInstallClick;
        }
    });

    function handleInstallClick() {
        if (!deferredPrompt) return;

        deferredPrompt.prompt();
        deferredPrompt.userChoice.then((choiceResult) => {
            if (choiceResult.outcome === 'accepted') {
                console.log('[PWA] User accepted the installation prompt');
            } else {
                console.log('[PWA] User dismissed the installation prompt');
            }
            deferredPrompt = null;
            if (installBtn) {
                installBtn.style.display = 'none';
            }
        });
    }

    // 4. Listen for successful app installation
    window.addEventListener('appinstalled', () => {
        console.log('[PWA] Finance Tracker was successfully installed');
        deferredPrompt = null;
        if (installBtn) {
            installBtn.style.display = 'none';
        }
    });

    // 5. Real-Time Online / Server Connectivity Detection
    let isServerReachable = true;

    function setOfflineState(isOffline, reason) {
        isServerReachable = !isOffline;
        if (offlineIndicator) {
            offlineIndicator.style.display = isOffline ? 'inline-flex' : 'none';
            if (isOffline && reason) {
                offlineIndicator.innerHTML = '<span class="offline-dot"></span> ' + reason;
            }
        }
    }

    function checkConnectivity() {
        if (!navigator.onLine) {
            setOfflineState(true, 'Offline');
            return;
        }

        fetch('/manifest.json', { method: 'HEAD', cache: 'no-store' })
            .then((res) => {
                if (res.ok) {
                    setOfflineState(false);
                } else {
                    setOfflineState(true, 'Server Offline');
                }
            })
            .catch(() => {
                setOfflineState(true, 'Server Offline');
            });
    }

    window.addEventListener('online', () => {
        checkConnectivity();
        showConnectionToast('Network restored. Reconnected!', 'success');
    });

    window.addEventListener('offline', () => {
        setOfflineState(true, 'Offline');
        showConnectionToast('You are currently offline. Showing cached dashboard.', 'warning');
    });

    // Check connectivity on load and periodically every 15 seconds
    checkConnectivity();
    setInterval(checkConnectivity, 15000);

    // 6. Prevent form submission when offline or server is unreachable
    document.addEventListener('submit', (e) => {
        if ((!navigator.onLine || !isServerReachable) && e.target && e.target.tagName === 'FORM') {
            const method = (e.target.getAttribute('method') || 'GET').toUpperCase();
            if (method === 'POST') {
                e.preventDefault();
                alert('Cannot record changes while offline or when the server is stopped. Please ensure the server is running to save entries.');
            }
        }
    });

    // 7. Toast notification helpers
    function showConnectionToast(message, type) {
        const existing = document.getElementById('pwa-toast');
        if (existing) existing.remove();

        const toast = document.createElement('div');
        toast.id = 'pwa-toast';
        toast.className = 'pwa-toast ' + (type === 'warning' ? 'pwa-toast-warning' : 'pwa-toast-success');
        toast.innerHTML = `
            <span>${message}</span>
            <button onclick="this.parentElement.remove()" style="background:none; border:none; color:inherit; font-size:1.1rem; cursor:pointer; margin-left:0.5rem;">&times;</button>
        `;
        document.body.appendChild(toast);

        setTimeout(() => {
            if (toast.parentElement) toast.remove();
        }, 4000);
    }

    function showUpdateToast() {
        const toast = document.createElement('div');
        toast.id = 'pwa-update-toast';
        toast.className = 'pwa-toast pwa-toast-info';
        toast.innerHTML = `
            <span>A new version of Finance Tracker is available!</span>
            <button onclick="window.location.reload()" class="btn btn-sm btn-primary" style="margin-left:0.75rem; padding:0.25rem 0.6rem;">Update</button>
        `;
        document.body.appendChild(toast);
    }
})();
