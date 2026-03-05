/**
 * vehicles.js — Vehicle marker overlay module for pzmap viewer
 * 
 * Fetches player claimed vehicles from the Spring Boot API and renders
 * them as dot markers on the OpenSeadragon map.
 * 
 * Supports:
 *   - Loading all vehicles for the session user (or a specific userId via query param)
 *   - Emphasizing a specific vehicle (via query param vehicle_id)
 *   - Mouseover tooltips with vehicle info
 *   - Zooming to a specific vehicle on load
 */
import { g } from "./globals.js";

let vehiclesData = [];
let vehiclesVisible = false;
let overlayElements = [];
let tooltipEl = null;
let mapWidth = 0;
let emphasizedId = null;

/** Convert pixel coordinate to OpenSeadragon viewport point */
function pixelToViewport(px, py) {
    return new OpenSeadragon.Point(px / mapWidth, py / mapWidth);
}

function escapeHtml(s) {
    return String(s).replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));
}

/** Create or return the persistent tooltip element */
function ensureTooltip() {
    if (tooltipEl) return;
    tooltipEl = document.createElement('div');
    tooltipEl.id = 'vehicle-tooltip';
    tooltipEl.style.cssText = `
        position: fixed; z-index: 99999; display: none; max-width: 320px;
        background: rgba(10,17,26,0.96); border: 1px solid #344860;
        border-radius: 12px; padding: 12px 14px; color: #e6eef9;
        box-shadow: 0 10px 30px rgba(0,0,0,0.5); font-size: 12px;
        line-height: 1.5; pointer-events: none; font-family: system-ui, sans-serif;
    `;
    document.body.appendChild(tooltipEl);
}

function showTooltip(v, mouseX, mouseY) {
    ensureTooltip();
    const isEmphasized = v.id === emphasizedId;
    tooltipEl.innerHTML = `
        <b style="font-size:14px; color:${isEmphasized ? '#ffb74d' : '#4facfe'}">${escapeHtml(v.vehicleName || v.scriptName || 'Veículo')}</b>
        ${v.preservedForMigration ? ' <span style="color:#ffb74d">⏳ Aguardando Migração</span>' : ''}
        <br><span style="color:#888">Hash:</span> ${escapeHtml(v.vehicleHash || '')}
        <br><span style="color:#888">Dono:</span> ${escapeHtml(v.ownerName || 'Desconhecido')}
        <br><span style="color:#888">Script:</span> ${escapeHtml(v.scriptName || '')}
        <br><span style="color:#888">Posição:</span> (${Math.round(v.x || 0)}, ${Math.round(v.y || 0)})
        <br><span style="color:#888">Itens:</span> ${v.itemCount || 0}
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

/** Create a marker element for a single vehicle */
function createVehicleMarker(v) {
    const isEmphasized = v.id === emphasizedId;
    const el = document.createElement('div');
    el.className = 'pzmap-vehicle-marker';

    const size = isEmphasized ? 18 : 12;
    const borderSize = isEmphasized ? 3 : 2;
    const bgColor = isEmphasized ? '#ffb74d' : '#4facfe';
    const borderColor = isEmphasized ? '#ff9800' : '#1976d2';

    el.style.cssText = `
        width: ${size}px; height: ${size}px;
        border-radius: 50%;
        background: ${bgColor};
        border: ${borderSize}px solid ${borderColor};
        box-shadow: 0 0 ${isEmphasized ? '12' : '6'}px ${isEmphasized ? 'rgba(255,152,0,0.7)' : 'rgba(79,172,254,0.5)'};
        pointer-events: auto;
        cursor: pointer;
        transition: transform 0.15s, box-shadow 0.15s;
        ${isEmphasized ? 'animation: vehiclePulse 2s ease-in-out infinite;' : ''}
    `;

    el.addEventListener('mouseenter', (e) => {
        el.style.transform = 'scale(1.5)';
        el.style.boxShadow = `0 0 16px ${isEmphasized ? 'rgba(255,152,0,0.9)' : 'rgba(79,172,254,0.8)'}`;
        showTooltip(v, e.clientX, e.clientY);
    });
    el.addEventListener('mousemove', (e) => {
        showTooltip(v, e.clientX, e.clientY);
    });
    el.addEventListener('mouseleave', () => {
        el.style.transform = 'scale(1)';
        el.style.boxShadow = `0 0 ${isEmphasized ? '12' : '6'}px ${isEmphasized ? 'rgba(255,152,0,0.7)' : 'rgba(79,172,254,0.5)'}`;
        hideTooltip();
    });

    return el;
}

/** Add pulsing animation style if not yet added */
function ensurePulseStyle() {
    if (!document.getElementById('vehicle-pulse-style')) {
        const style = document.createElement('style');
        style.id = 'vehicle-pulse-style';
        style.textContent = `
            @keyframes vehiclePulse {
                0%, 100% { box-shadow: 0 0 12px rgba(255,152,0,0.7); }
                50% { box-shadow: 0 0 24px rgba(255,152,0,0.95); }
            }
        `;
        document.head.appendChild(style);
    }
}

/** Render all vehicle markers on the map */
function renderVehicles() {
    clearVehicles();
    if (!g.viewer || !mapWidth || !vehiclesVisible) return;
    ensurePulseStyle();

    for (const v of vehiclesData) {
        if (v.x == null || v.y == null) continue;

        const el = createVehicleMarker(v);
        const pt = pixelToViewport(v.x, v.y);

        g.viewer.addOverlay({
            element: el,
            location: pt,
            placement: OpenSeadragon.Placement.CENTER
        });
        overlayElements.push(el);
    }
}

/** Remove all vehicle overlays */
function clearVehicles() {
    if (!g.viewer) return;
    for (const el of overlayElements) {
        try { g.viewer.removeOverlay(el); } catch (e) { /* ignore */ }
    }
    overlayElements = [];
    hideTooltip();
}

/** Fetch vehicles from the API */
export async function loadVehicles(userId) {
    try {
        let url = '/claimed-cars/api/vehicles';
        if (userId) url += '?userId=' + encodeURIComponent(userId);
        const resp = await fetch(url);
        if (!resp.ok) return;
        vehiclesData = await resp.json();
    } catch (e) {
        console.warn('[vehicles] Failed to load vehicles:', e);
    }
}

/** Initialize the vehicles module after the viewer and base map are ready */
export function init(opts) {
    if (!g.base_map) return;
    mapWidth = g.base_map.w || 19968;

    // Check query params for user filter and emphasis
    const userId = opts?.userId || (g.query_string ? g.query_string.vehicle_user : null);
    const vehicleId = opts?.vehicleId || (g.query_string ? g.query_string.vehicle_id : null);
    if (vehicleId) emphasizedId = parseInt(vehicleId, 10);

    loadVehicles(userId).then(() => {
        if (g.viewer && vehiclesData.length > 0) {
            vehiclesVisible = true;
            renderVehicles();
            updateVehiclesButton();

            // Auto-zoom to the emphasized vehicle, or first vehicle
            const target = vehicleId
                ? vehiclesData.find(v => v.id === emphasizedId)
                : null;
            const zoomTo = target || vehiclesData[0];
            if (zoomTo && zoomTo.x != null && zoomTo.y != null) {
                goToVehicle(zoomTo.x, zoomTo.y);
            }
        }
    });
}

/** Pan and zoom to a vehicle coordinate */
export function goToVehicle(x, y, zoom) {
    if (!g.viewer || !mapWidth) return;
    const pt = pixelToViewport(x, y);
    const zoomLevel = zoom || 0.015;
    const rect = new OpenSeadragon.Rect(
        pt.x - zoomLevel / 2,
        pt.y - zoomLevel / 2,
        zoomLevel,
        zoomLevel
    );
    g.viewer.viewport.fitBounds(rect);
}

/** Toggle vehicle overlay visibility */
export function toggle() {
    vehiclesVisible = !vehiclesVisible;
    if (vehiclesVisible) {
        renderVehicles();
    } else {
        clearVehicles();
    }
    return vehiclesVisible;
}

/** Check if vehicles are currently visible */
export function isVisible() {
    return vehiclesVisible;
}

/** Update the vehicles button active state */
function updateVehiclesButton() {
    const btn = document.getElementById('vehicles_btn');
    if (!btn) return;
    if (vehiclesVisible) {
        btn.classList.add('active');
    } else {
        btn.classList.remove('active');
    }
}

/** Get all loaded vehicles data */
export function getVehicles() {
    return vehiclesData;
}

/** Set which vehicle ID should be emphasized */
export function setEmphasized(id) {
    emphasizedId = id;
}
