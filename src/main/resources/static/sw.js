/*
 * Finance Tracker Progressive Web App (PWA) Service Worker
 * Version: 2.0.0
 * Strategy:
 *   - Navigation: Network-First with Cached Dashboard & Offline Fallback
 *   - Dashboard Shell: Precached ('/', '/dashboard') and dynamically refreshed
 *   - Read-Only Chart APIs (/api/charts/*): Network-First with cache fallback
 *   - Static Assets: Stale-While-Revalidate
 *   - Mutating Requests (POST/PUT/DELETE): Network-Only (Never Cached)
 *   - Exports & Console: Network-Only
 */

const CACHE_NAME = 'finance-tracker-v2';

const PRECACHE_ASSETS = [
    '/',
    '/dashboard',
    '/offline.html',
    '/manifest.json',
    '/css/styles.css',
    '/js/charts.js',
    '/js/app.js',
    '/icons/icon.svg',
    '/icons/icon-192x192.png',
    '/icons/icon-512x512.png',
    '/icons/icon-maskable-512x512.png',
    'https://cdn.jsdelivr.net/npm/chart.js',
    'https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap'
];

// 1. Install event: Pre-cache static shell, dashboard & offline fallback
self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then((cache) => {
                // Use Promise.allSettled to ensure individual external failures don't block install
                return Promise.allSettled(
                    PRECACHE_ASSETS.map(asset => {
                        return cache.add(asset).catch(err => {
                            console.warn('[PWA ServiceWorker] Pre-cache skip for:', asset, err);
                        });
                    })
                );
            })
            .then(() => self.skipWaiting())
    );
});

// 2. Activate event: Clean up old cache versions and claim clients immediately
self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys()
            .then((cacheNames) => {
                return Promise.all(
                    cacheNames
                        .filter((cacheName) => cacheName !== CACHE_NAME)
                        .map((cacheName) => {
                            console.log('[PWA ServiceWorker] Purging outdated cache:', cacheName);
                            return caches.delete(cacheName);
                        })
                );
            })
            .then(() => self.clients.claim())
    );
});

// 3. Fetch event: Intelligent offline-ready routing
self.addEventListener('fetch', (event) => {
    const request = event.request;
    const url = new URL(request.url);

    // Rule A: Never intercept or cache non-GET requests (POST, PUT, DELETE)
    if (request.method !== 'GET') {
        return;
    }

    // Rule B: Never cache chrome-extension or unsupported protocols
    if (!url.protocol.startsWith('http')) {
        return;
    }

    // Rule C: Never cache sensitive reports export or H2 console
    if (url.pathname.startsWith('/reports/export') || url.pathname.startsWith('/h2-console')) {
        event.respondWith(fetch(request));
        return;
    }

    // Rule D: Read-only Chart APIs -> Network-First with Cache Fallback for offline charts
    if (url.pathname.startsWith('/api/charts/')) {
        event.respondWith(
            fetch(request)
                .then((networkResponse) => {
                    if (networkResponse && networkResponse.status === 200) {
                        const responseClone = networkResponse.clone();
                        caches.open(CACHE_NAME).then((cache) => cache.put(request, responseClone));
                    }
                    return networkResponse;
                })
                .catch(async () => {
                    const cachedChart = await caches.match(request);
                    if (cachedChart) {
                        return cachedChart;
                    }
                    // Return graceful empty JSON fallback so Chart.js handles it without error
                    return new Response(JSON.stringify({ labels: [], incomeData: [], expenseData: [], data: [] }), {
                        headers: { 'Content-Type': 'application/json' }
                    });
                })
        );
        return;
    }

    // Rule E: HTML Page Navigations -> Network-First, then Cached Dashboard/Page, then Offline Fallback
    const isNavigation = request.mode === 'navigate' ||
        (request.headers.get('accept') && request.headers.get('accept').includes('text/html'));

    if (isNavigation) {
        event.respondWith(
            fetch(request)
                .then((networkResponse) => {
                    if (networkResponse && networkResponse.status === 200) {
                        const responseClone = networkResponse.clone();
                        caches.open(CACHE_NAME).then((cache) => {
                            cache.put(request, responseClone);
                            if (url.pathname === '/' || url.pathname === '/dashboard') {
                                cache.put('/', responseClone.clone());
                                cache.put('/dashboard', responseClone.clone());
                            }
                        });
                    }
                    return networkResponse;
                })
                .catch(async () => {
                    // 1. Try exact requested page from cache
                    const cachedPage = await caches.match(request);
                    if (cachedPage) {
                        return cachedPage;
                    }

                    // 2. If navigating to root or dashboard, serve cached dashboard
                    if (url.pathname === '/' || url.pathname === '/dashboard') {
                        const cachedDashboard = (await caches.match('/')) || (await caches.match('/dashboard'));
                        if (cachedDashboard) {
                            return cachedDashboard;
                        }
                    }

                    // 3. Fall back to /offline.html
                    const offlinePage = await caches.match('/offline.html');
                    if (offlinePage) {
                        return offlinePage;
                    }

                    // 4. Last resort: any cached page
                    return (await caches.match('/')) || (await caches.match('/dashboard'));
                })
        );
        return;
    }

    // Rule F: Static Assets (CSS, JS, Images, Fonts, Manifest) -> Stale-While-Revalidate
    const isStaticAsset = (
        url.pathname.startsWith('/css/') ||
        url.pathname.startsWith('/js/') ||
        url.pathname.startsWith('/icons/') ||
        url.pathname === '/manifest.json' ||
        url.hostname.includes('cdn.jsdelivr.net') ||
        url.hostname.includes('fonts.googleapis.com') ||
        url.hostname.includes('fonts.gstatic.com')
    );

    if (isStaticAsset) {
        event.respondWith(
            caches.open(CACHE_NAME).then((cache) => {
                return cache.match(request).then((cachedResponse) => {
                    const fetchPromise = fetch(request)
                        .then((networkResponse) => {
                            if (networkResponse && networkResponse.status === 200) {
                                cache.put(request, networkResponse.clone());
                            }
                            return networkResponse;
                        })
                        .catch(() => cachedResponse);

                    return cachedResponse || fetchPromise;
                });
            })
        );
        return;
    }

    // Default: Network fetch with cache match fallback
    event.respondWith(
        fetch(request).catch(() => caches.match(request))
    );
});

