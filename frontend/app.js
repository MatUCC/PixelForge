// PixelForge frontend: vanilla JS, sin dependencias.
// Habla con el API Java (ver README, sección "API para el frontend").

const API = (window.PIXELFORGE_API_URL || (location.protocol === 'file:' ? 'http://localhost:8080' : ''))
  .replace(/\/$/, '');

const BASE_COST = 500;          // TreatmentType.BASE
const BASE_SECONDS = 1;
const DISCOUNT_THRESHOLD = 4;   // PipelineBuilder.DISCOUNT_THRESHOLD
const MAX_BYTES = 5 * 1024 * 1024;

// Nombres en español para mostrar (el API devuelve los nombres en inglés).
const LABELS = {
  REMOVE_BACKGROUND: { icon: '✂️', name: 'Quitar fondo', hint: 'Vuelve transparente el fondo' },
  RESIZE: { icon: '📐', name: 'Redimensionar', hint: 'Ajusta a 1080×1080' },
  COLOR_FILTER: { icon: '🎨', name: 'Filtro de color', hint: 'Grises, brillo o contraste' },
  WATERMARK: { icon: '💧', name: 'Marca de agua', hint: 'Escribe el nombre de tu tienda' },
  BORDER: { icon: '🖼️', name: 'Borde', hint: 'Marco de color' },
  COMPRESSION: { icon: '🗜️', name: 'Compresión web', hint: 'JPEG liviano · siempre de último' },
};
const label = (t) => LABELS[t.id] || { icon: '✨', name: t.name, hint: '' };

const FILTERS = { GRAYSCALE: 'Escala de grises', BRIGHTEN: 'Más brillo', HIGH_CONTRAST: 'Alto contraste' };

const state = {
  catalog: [],     // [{id, name, cost, seconds, parameterHint}]
  pipeline: [],    // [{id, param}]
  file: null,
};

const $ = (id) => document.getElementById(id);
const money = (n) => (n < 0 ? '-$' : '$') + Math.abs(n).toLocaleString('es-CO');

// ---------- API ----------

async function api(path, options) {
  const res = await fetch(API + path, options);
  const data = await res.json().catch(() => ({ error: 'Respuesta inválida del servidor' }));
  if (!res.ok) throw new Error(data.error || `Error ${res.status}`);
  return data;
}

// ---------- Foto ----------

function setFile(file) {
  showError('');
  if (!file) return;
  if (!['image/png', 'image/jpeg'].includes(file.type)) return showError('Solo se aceptan imágenes JPG o PNG.');
  if (file.size > MAX_BYTES) return showError('La imagen supera los 5 MB.');
  state.file = file;
  const preview = $('preview');
  preview.src = URL.createObjectURL(file);
  preview.hidden = false;
  $('dropText').textContent = file.name + ' · clic para cambiarla';
  $('dropIcon').hidden = true;
  render();
}

function setupDropzone() {
  const zone = $('dropzone');
  $('photo').addEventListener('change', (e) => setFile(e.target.files[0]));
  ['dragenter', 'dragover'].forEach((ev) => zone.addEventListener(ev, (e) => {
    e.preventDefault();
    zone.classList.add('drag');
  }));
  ['dragleave', 'drop'].forEach((ev) => zone.addEventListener(ev, (e) => {
    e.preventDefault();
    zone.classList.remove('drag');
  }));
  zone.addEventListener('drop', (e) => setFile(e.dataTransfer.files[0]));
}

// ---------- Catálogo y pipeline ----------

function defaultParam(id) {
  if (id === 'COLOR_FILTER') return 'GRAYSCALE';
  if (id === 'WATERMARK') return 'Mi Tienda';
  if (id === 'BORDER') return '000000';
  return null;
}

function renderCatalog() {
  const box = $('catalog');
  box.innerHTML = '';
  state.catalog.forEach((t) => {
    const l = label(t);
    const btn = document.createElement('button');
    btn.className = 'treatment';
    btn.title = l.hint;
    btn.innerHTML = `<span class="t-icon">${l.icon}</span>`
      + `<span class="t-body"><strong>${l.name}</strong><small>${l.hint}</small></span>`
      + `<span class="t-price">${money(t.cost)}<small>${t.seconds} s</small></span>`;
    btn.addEventListener('click', () => {
      state.pipeline.push({ id: t.id, param: defaultParam(t.id) });
      render();
    });
    box.appendChild(btn);
  });
}

function paramEditor(item) {
  let input;
  if (item.id === 'COLOR_FILTER') {
    input = document.createElement('select');
    Object.entries(FILTERS).forEach(([value, label]) => input.add(new Option(label, value, false, value === item.param)));
  } else if (item.id === 'BORDER') {
    input = document.createElement('input');
    input.type = 'color';
    input.value = '#' + item.param;
  } else if (item.id === 'WATERMARK') {
    input = document.createElement('input');
    input.type = 'text';
    input.maxLength = 40;
    input.value = item.param;
    input.placeholder = 'Texto';
  } else {
    return null;
  }
  input.addEventListener('input', () => {
    item.param = item.id === 'BORDER' ? input.value.slice(1).toUpperCase() : input.value;
    renderEstimate();
  });
  return input;
}

function move(index, delta) {
  const target = index + delta;
  if (target < 0 || target >= state.pipeline.length) return;
  [state.pipeline[index], state.pipeline[target]] = [state.pipeline[target], state.pipeline[index]];
  render();
}

function renderPipeline() {
  const list = $('pipeline');
  list.innerHTML = '';
  state.pipeline.forEach((item, i) => {
    const t = state.catalog.find((c) => c.id === item.id);
    const li = document.createElement('li');
    li.innerHTML = `<span class="step">${i + 1}</span><span class="name">${label(t).icon} ${label(t).name}</span>`;
    const editor = paramEditor(item);
    if (editor) li.appendChild(editor);

    const actions = document.createElement('span');
    actions.className = 'actions';
    [['↑', () => move(i, -1), 'Subir'], ['↓', () => move(i, 1), 'Bajar'],
     ['✕', () => { state.pipeline.splice(i, 1); render(); }, 'Quitar']].forEach(([text, fn, title]) => {
      const b = document.createElement('button');
      b.textContent = text;
      b.title = title;
      b.addEventListener('click', fn);
      actions.appendChild(b);
    });
    li.appendChild(actions);
    list.appendChild(li);
  });
  $('emptyPipeline').hidden = state.pipeline.length > 0;
}

function validationMessage() {
  const p = state.pipeline;
  if (p.slice(0, -1).some((x) => x.id === 'COMPRESSION')) {
    return 'La compresión debe ir de último (el JPEG pierde la transparencia).';
  }
  if (p.some((x) => x.id === 'WATERMARK' && !(x.param || '').trim())) {
    return 'La marca de agua necesita un texto.';
  }
  return '';
}

function renderEstimate() {
  const chosen = state.pipeline.map((p) => state.catalog.find((c) => c.id === p.id));
  let cost = BASE_COST + chosen.reduce((s, t) => s + t.cost, 0);
  const seconds = BASE_SECONDS + chosen.reduce((s, t) => s + t.seconds, 0);
  const discount = chosen.length >= DISCOUNT_THRESHOLD;
  if (discount) cost -= Math.trunc(cost * 10 / 100); // igual que DiscountDecorator
  $('estCost').textContent = money(cost);
  $('estTime').textContent = seconds + ' s';
  $('estDiscount').hidden = !discount;

  const warning = validationMessage();
  $('warning').textContent = warning;
  $('warning').hidden = !warning;
  $('processBtn').disabled = !state.file || state.pipeline.length === 0 || !!warning;
}

function render() {
  renderPipeline();
  renderEstimate();
}

// ---------- Procesar ----------

function treatmentsParam() {
  // El API separa por comas, así que se quitan del texto de la marca de agua.
  return state.pipeline
    .map((p) => (p.param ? `${p.id}:${String(p.param).replace(/,/g, ' ').trim()}` : p.id))
    .join(',');
}

async function processImage() {
  showError('');
  const btn = $('processBtn');
  btn.disabled = true;
  btn.textContent = 'Procesando…';
  try {
    const data = await api('/api/process?treatments=' + encodeURIComponent(treatmentsParam()), {
      method: 'POST',
      headers: { 'Content-Type': 'application/octet-stream' },
      body: state.file,
    });
    showResult(data);
    loadOrders();
  } catch (e) {
    showError(e.message);
  } finally {
    btn.textContent = 'Procesar imagen';
    renderEstimate();
  }
}

function showResult({ order, imageFormat, imageBase64 }) {
  const src = `data:image/${imageFormat === 'jpg' ? 'jpeg' : imageFormat};base64,${imageBase64}`;
  $('resultImg').src = src;
  $('downloadLink').href = src;
  $('downloadLink').download = `pixelforge-${order.id}.${imageFormat}`;

  // Capas anidadas: layers viene de adentro hacia afuera, así que la última es la más externa.
  let inner = null;
  order.layers.forEach((layer) => {
    const box = document.createElement('div');
    if (layer.cost < 0) box.className = 'discount';
    const label = document.createElement('div');
    label.className = 'label';
    label.textContent = layer.name;
    box.appendChild(label);
    if (inner) box.appendChild(inner);
    inner = box;
  });
  $('layersNested').replaceChildren(inner);

  $('layersTable').innerHTML = '';
  order.layers.forEach((l) => {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td></td><td class="num ${l.cost < 0 ? 'negative' : ''}">${money(l.cost)}</td><td class="num">${l.seconds} s</td>`;
    tr.firstChild.textContent = l.name;
    $('layersTable').appendChild(tr);
  });
  $('totalCost').textContent = money(order.totalCost);
  $('totalTime').textContent = order.totalSeconds + ' s';

  $('resultCard').hidden = false;
  $('resultCard').scrollIntoView({ behavior: 'smooth' });
}

// ---------- Historial ----------

async function loadOrders() {
  try {
    const orders = await api('/api/orders');
    const body = $('ordersTable');
    body.innerHTML = '';
    if (orders.length === 0) {
      body.innerHTML = '<tr><td colspan="5" class="muted">Sin pedidos todavía.</td></tr>';
      return;
    }
    orders.slice().reverse().forEach((o) => {
      const tr = document.createElement('tr');
      tr.innerHTML = `<td class="num">${o.id}</td><td class="num">${new Date(o.createdAt).toLocaleString('es-CO')}</td>`
        + `<td></td><td class="num">${money(o.totalCost)}</td><td class="num">${o.totalSeconds} s</td>`;
      tr.children[2].textContent = o.description;
      body.appendChild(tr);
    });
  } catch (e) {
    $('ordersTable').innerHTML = '<tr><td colspan="5" class="error"></td></tr>';
    $('ordersTable').querySelector('td').textContent = 'No se pudo cargar el historial: ' + e.message;
  }
}

function showError(message) {
  $('error').textContent = message;
  $('error').hidden = !message;
}

// ---------- Inicio ----------

async function init() {
  setupDropzone();
  $('processBtn').addEventListener('click', processImage);
  $('refreshOrders').addEventListener('click', loadOrders);
  try {
    state.catalog = await api('/api/treatments');
    renderCatalog();
  } catch (e) {
    $('catalog').innerHTML = '<p class="error">No se pudo conectar con el backend. ¿Está corriendo el ApiServer?</p>';
  }
  render();
  loadOrders();
}

init();
