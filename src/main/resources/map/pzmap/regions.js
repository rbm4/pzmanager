/**
 * regions.js — Region overlay module for pzmap viewer
 * 
 * Fetches regions from the Spring Boot API and renders them as
 * OpenSeadragon overlays on top of the map.
 * 
 * Coordinates from the API are in PZ game-square units which map 1:1
 * to pzmap pixel coordinates (sqr=1). OpenSeadragon viewport coords
 * are normalised by dividing by the base image width.
 */
import { g } from "./globals.js";

let regionsData = [];
let regionsVisible = true;
let overlayElements = [];
let tooltipEl = null;
let mapWidth = 0;
let mapHeight = 0;

/** Determine the API base URL (relative to the host, not the pzmap folder) */
function getApiBase() {
    // When served from /pzmap/pzmap.html, we need to reach /map-regions/api/regions
    // Use an absolute path to avoid relative-path issues
    return '/map-regions/api/regions';
}

/** Convert a pixel coordinate to an OpenSeadragon viewport point */
function pixelToViewport(px, py) {
    return new OpenSeadragon.Point(px / mapWidth, py / mapWidth);
}

/** Determine region color scheme based on categories and permanent flag */
function regionColor(rg) {
    const cats = (rg.categories || '').toLowerCase();
    if (cats.includes('pvp')) {
        return { border: 'rgba(231,76,60,0.75)', fill: 'rgba(231,76,60,0.12)', label: '#e74c3c', type: 'PVP' };
    }
    if (rg.permanent) {
        return { border: 'rgba(80,200,120,0.65)', fill: 'rgba(80,200,120,0.12)', label: '#50c878', type: 'Permanente' };
    }
    return { border: 'rgba(200,130,255,0.65)', fill: 'rgba(200,130,255,0.12)', label: '#c882ff', type: 'PVE' };
}

function escapeHtml(s) {
    return String(s).replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));
}

/** Create the persistent tooltip element */
function ensureTooltip() {
    if (tooltipEl) return;
    tooltipEl = document.createElement('div');
    tooltipEl.id = 'region-tooltip';
    tooltipEl.style.cssText = `
        position: fixed; z-index: 99999; display: none; max-width: 360px;
        background: rgba(10,17,26,0.96); border: 1px solid #203044;
        border-radius: 12px; padding: 12px 14px; color: #e6eef9;
        box-shadow: 0 10px 30px rgba(0,0,0,0.5); font-size: 12px;
        line-height: 1.4; pointer-events: none; font-family: system-ui, sans-serif;
    `;
    document.body.appendChild(tooltipEl);
}

function showTooltip(rg, mouseX, mouseY) {
    ensureTooltip();
    const c = regionColor(rg);
    const cats = (rg.categories || '').toLowerCase();
    const isPvp = cats.includes('pvp');
    let propsHtml = '';
    if (rg.properties) {
        for (const [key, val] of Object.entries(rg.properties)) {
            propsHtml += `<br><span style="color:#888">${escapeHtml(key)}:</span> ${escapeHtml(String(val))}`;
        }
    }
    tooltipEl.innerHTML = `
        <b style="font-size:14px">${escapeHtml(rg.name || rg.code)}</b>
        ${isPvp ? ' <span style="color:#e74c3c">☠️ PVP</span>' : ` <span style="color:${c.label}">${c.type}</span>`}
        <br><span style="color:#888">Código:</span> ${escapeHtml(rg.code || '')}
        ${rg.categories ? '<br><span style="color:#888">Categorias:</span> ' + escapeHtml(rg.categories) : ''}
        <br><span style="color:#888">Coordenadas:</span> (${rg.x1}, ${rg.y1}) → (${rg.x2}, ${rg.y2})
        ${rg.permanent ? '<br><span style="color:#50c878"><b>⚡ Permanente</b></span>' : ''}
        ${propsHtml}
    `;
    tooltipEl.style.display = 'block';

    const pad = 10;
    const rect = tooltipEl.getBoundingClientRect();
    let x = mouseX + 16, y = mouseY + 16;
    if (x + rect.width > window.innerWidth - pad) x = window.innerWidth - pad - rect.width;
    if (y + rect.height > window.innerHeight - pad) y = window.innerHeight - pad - rect.height;
    x = Math.max(pad, x);
    y = Math.max(pad, y);
    tooltipEl.style.left = x + 'px';
    tooltipEl.style.top = y + 'px';
}

function hideTooltip() {
    if (tooltipEl) tooltipEl.style.display = 'none';
}

/** Create an overlay div for a single region */
function createRegionOverlay(rg) {
    const c = regionColor(rg);
    const el = document.createElement('div');
    el.className = 'pzmap-region-overlay';
    el.style.cssText = `
        background: ${c.fill};
        border: 2px solid ${c.border};
        box-sizing: border-box;
        pointer-events: auto;
        cursor: pointer;
        transition: background 0.15s;
    `;
    el.dataset.regionId = rg.id;

    // Hover effects
    el.addEventListener('mouseenter', (e) => {
        el.style.background = c.fill.replace(/[\d.]+\)$/, '0.25)');
        el.style.borderWidth = '3px';
        showTooltip(rg, e.clientX, e.clientY);
    });
    el.addEventListener('mousemove', (e) => {
        showTooltip(rg, e.clientX, e.clientY);
    });
    el.addEventListener('mouseleave', () => {
        el.style.background = c.fill;
        el.style.borderWidth = '2px';
        hideTooltip();
    });

    // Add name label inside
    const label = document.createElement('div');
    const cats = (rg.categories || '').toLowerCase();
    const isPvp = cats.includes('pvp');
    label.textContent = (rg.name || rg.code) + (isPvp ? ' ☠️' : '');
    label.style.cssText = `
        position: absolute; top: 2px; left: 4px;
        color: ${c.label}; font-size: 11px; font-weight: bold;
        font-family: system-ui, sans-serif;
        text-shadow: 1px 1px 2px rgba(0,0,0,0.8);
        white-space: nowrap; pointer-events: none;
        overflow: hidden; text-overflow: ellipsis;
        max-width: calc(100% - 8px);
    `;
    el.appendChild(label);

    return el;
}

/** Adds all region overlays to the OpenSeadragon viewer */
function renderRegions() {
    clearRegions();
    if (!g.viewer || !mapWidth || !regionsVisible) return;

    for (const rg of regionsData) {
        const x1 = Math.min(rg.x1, rg.x2);
        const y1 = Math.min(rg.y1, rg.y2);
        const x2 = Math.max(rg.x1, rg.x2);
        const y2 = Math.max(rg.y1, rg.y2);

        const el = createRegionOverlay(rg);
        const topLeft = pixelToViewport(x1, y1);
        const bottomRight = pixelToViewport(x2, y2);
        const rect = new OpenSeadragon.Rect(
            topLeft.x, topLeft.y,
            bottomRight.x - topLeft.x,
            bottomRight.y - topLeft.y
        );

        g.viewer.addOverlay({
            element: el,
            location: rect,
            placement: OpenSeadragon.Placement.TOP_LEFT
        });
        overlayElements.push(el);
    }
}

/** Remove all region overlays */
function clearRegions() {
    if (!g.viewer) return;
    for (const el of overlayElements) {
        try { g.viewer.removeOverlay(el); } catch (e) { /* ignore */ }
    }
    overlayElements = [];
    hideTooltip();
}

/** Fetch regions from the API */
export async function loadRegions() {
    try {
        const resp = await fetch(getApiBase());
        if (!resp.ok) return;
        regionsData = await resp.json();
    } catch (e) {
        console.warn('[regions] Failed to load regions:', e);
    }
}

/** Initialize the regions module after the viewer and base map are ready */
export function init() {
    if (!g.base_map) return;
    mapWidth = g.base_map.w || 19968;
    mapHeight = g.base_map.h || 16128;

    // Load regions and render once the viewer has loaded a tile
    loadRegions().then(() => {
        if (g.viewer && regionsData.length > 0) {
            renderRegions();
        }
    });
}

/** Toggle region overlay visibility */
export function toggle() {
    regionsVisible = !regionsVisible;
    if (regionsVisible) {
        renderRegions();
    } else {
        clearRegions();
    }
    return regionsVisible;
}

/** Check if regions are currently visible */
export function isVisible() {
    return regionsVisible;
}

/** Navigate to a specific region by zooming to its bounds */
export function goToRegion(x1, y1, x2, y2, padding = 0.2) {
    if (!g.viewer || !mapWidth) return;
    const rx1 = Math.min(x1, x2);
    const ry1 = Math.min(y1, y2);
    const rx2 = Math.max(x1, x2);
    const ry2 = Math.max(y1, y2);
    const w = rx2 - rx1;
    const h = ry2 - ry1;

    // Add padding
    const padX = w * padding;
    const padY = h * padding;

    const topLeft = pixelToViewport(rx1 - padX, ry1 - padY);
    const bottomRight = pixelToViewport(rx2 + padX, ry2 + padY);
    const rect = new OpenSeadragon.Rect(
        topLeft.x, topLeft.y,
        bottomRight.x - topLeft.x,
        bottomRight.y - topLeft.y
    );
    g.viewer.viewport.fitBounds(rect);
}

/** Get all loaded regions data */
export function getRegions() {
    return regionsData;
}
