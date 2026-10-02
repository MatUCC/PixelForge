const API = (window.PIXELFORGE_API_URL || (location.protocol === 'file:' ? 'http://localhost:8080' : ''))
  .replace(/\/$/, '');

const BASE_COST = 500;
const BASE_SECONDS = 1;
const DISCOUNT_THRESHOLD = 4;
const MAX_BYTES = 5 * 1024 * 1024;

const LABELS = {
  REMOVE_BACKGROUND: { icon: '✂️', name: 'Remove background', hint: 'Makes the background transparent' },
  RESIZE: { icon: '📐', name: 'Resize', hint: 'Fits the photo into 1080×1080' },
  COLOR_FILTER: { icon: '🎨', name: 'Color filter', hint: 'Grayscale, brightness or contrast' },
  WATERMARK: { icon: '💧', name: 'Watermark', hint: 'Writes your store name' },
  BORDER: { icon: '🖼️', name: 'Border', hint: 'Colored frame' },
  COMPRESSION: { icon: '🗜️', name: 'Web compression', hint: 'Light JPEG · always last' },
};
const label = (t) => LABELS[t.id] || { icon: '✨', name: t.name, hint: '' };

const FILTERS = { GRAYSCALE: 'Grayscale', BRIGHTEN: 'Brighten', HIGH_CONTRAST: 'High contrast' };
const STYLE_DEFAULT_TYPES = ['COLOR_FILTER', 'BORDER'];

const state = {
  catalog: [],
  presets: [],
  styles: [],
  pipeline: [],
  file: null,
};

const $ = (id) => document.getElementById(id);
const money = (n) => (n < 0 ? '-$' : '$') + Math.abs(n).toLocaleString('en-US');

async function api(path, options) {
  const res = await fetch(API + path, options);
  const data = await res.json().catch(() => ({ error: 'Invalid server response' }));
  if (!res.ok) throw new Error(data.error || `Error ${res.status}`);
  return data;
}

function storeName() {
  return $('storeName').value.trim() || 'My Store';
}

function setFile(file) {
  showError('');
  if (!file) return;
  if (!['image/png', 'image/jpeg'].includes(file.type)) return showError('Only JPG or PNG images are accepted.');
  if (file.size > MAX_BYTES) return showError('The image is larger than 5 MB.');
  state.file = file;
  const preview = $('preview');
  preview.src = URL.createObjectURL(file);
  preview.hidden = false;
  $('dropText').textContent = file.name + ' · click to change it';
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

function defaultParam(id) {
  if (id === 'COLOR_FILTER') return 'GRAYSCALE';
  if (id === 'WATERMARK') return storeName();
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

function renderPresets() {
  const box = $('presets');
  box.innerHTML = '';
  state.presets.forEach((preset) => {
    const btn = document.createElement('button');
    btn.className = 'preset';
    const title = document.createElement('strong');
    title.textContent = '📦 ' + preset.name;
    const description = document.createElement('small');
    description.textContent = preset.description;
    btn.append(title, description);
    btn.addEventListener('click', () => applyPreset(preset.id));
    box.appendChild(btn);
  });
}

async function applyPreset(id) {
  showError('');
  try {
    const preset = await api(`/api/presets/apply?id=${encodeURIComponent(id)}&store=${encodeURIComponent(storeName())}`);
    state.pipeline = preset.treatments.map((t) => ({ id: t.id, param: t.parameter === null ? '' : t.parameter }));
    render();
  } catch (e) {
    showError(e.message);
  }
}

function renderStyles() {
  const select = $('styleSelect');
  select.innerHTML = '';
  state.styles.forEach((s) => select.add(new Option(s.name, s.id)));
}

function paramEditor(item) {
  if (item.param === '' && STYLE_DEFAULT_TYPES.includes(item.id)) {
    const text = document.createElement('span');
    text.className = 'style-default';
    text.textContent = 'uses the style default';
    return text;
  }
  let input;
  if (item.id === 'COLOR_FILTER') {
    input = document.createElement('select');
    Object.entries(FILTERS).forEach(([value, text]) => input.add(new Option(text, value, false, value === item.param)));
  } else if (item.id === 'BORDER') {
    input = document.createElement('input');
    input.type = 'color';
    input.value = '#' + item.param;
  } else if (item.id === 'WATERMARK') {
    input = document.createElement('input');
    input.type = 'text';
    input.maxLength = 40;
    input.value = item.param;
    input.placeholder = 'Text';
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
    [['↑', () => move(i, -1), 'Move up'], ['↓', () => move(i, 1), 'Move down'],
     ['✕', () => { state.pipeline.splice(i, 1); render(); }, 'Remove']].forEach(([text, fn, title]) => {
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
    return 'Compression must be the last treatment (JPEG loses transparency).';
  }
  if (p.some((x) => x.id === 'WATERMARK' && !(x.param || '').trim())) {
    return 'The watermark needs a text.';
  }
  return '';
}

function renderEstimate() {
  const chosen = state.pipeline.map((p) => state.catalog.find((c) => c.id === p.id));
  let cost = BASE_COST + chosen.reduce((s, t) => s + t.cost, 0);
  const seconds = BASE_SECONDS + chosen.reduce((s, t) => s + t.seconds, 0);
  const discount = chosen.length >= DISCOUNT_THRESHOLD;
  if (discount) cost -= Math.trunc(cost * 10 / 100);
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

function treatmentsParam() {
  return state.pipeline
    .map((p) => (p.param ? `${p.id}:${String(p.param).replace(/,/g, ' ').trim()}` : p.id))
    .join(',');
}

async function processImage() {
  showError('');
  const btn = $('processBtn');
  btn.disabled = true;
  btn.textContent = 'Processing…';
  try {
    const query = `treatments=${encodeURIComponent(treatmentsParam())}&style=${encodeURIComponent($('styleSelect').value)}`;
    const data = await api('/api/process?' + query, {
      method: 'POST',
      headers: { 'Content-Type': 'application/octet-stream' },
      body: state.file,
    });
    showResult(data);
    loadOrders();
  } catch (e) {
    showError(e.message);
  } finally {
    btn.textContent = 'Process image';
    renderEstimate();
  }
}

function showResult({ order, imageFormat, imageBase64 }) {
  const src = `data:image/${imageFormat === 'jpg' ? 'jpeg' : imageFormat};base64,${imageBase64}`;
  $('resultImg').src = src;
  $('downloadLink').href = src;
  $('downloadLink').download = `pixelforge-${order.id}.${imageFormat}`;

  let inner = null;
  order.layers.forEach((layer) => {
    const box = document.createElement('div');
    if (layer.cost < 0) box.className = 'discount';
    const title = document.createElement('div');
    title.className = 'label';
    title.textContent = layer.name;
    box.appendChild(title);
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

async function loadOrders() {
  try {
    const orders = await api('/api/orders');
    const body = $('ordersTable');
    body.innerHTML = '';
    if (orders.length === 0) {
      body.innerHTML = '<tr><td colspan="5" class="muted">No orders yet.</td></tr>';
      return;
    }
    orders.slice().reverse().forEach((o) => {
      const tr = document.createElement('tr');
      tr.innerHTML = `<td class="num">${o.id}</td><td class="num">${new Date(o.createdAt).toLocaleString('en-US')}</td>`
        + `<td></td><td class="num">${money(o.totalCost)}</td><td class="num">${o.totalSeconds} s</td>`;
      tr.children[2].textContent = o.description;
      body.appendChild(tr);
    });
  } catch (e) {
    $('ordersTable').innerHTML = '<tr><td colspan="5" class="error"></td></tr>';
    $('ordersTable').querySelector('td').textContent = 'The history could not be loaded: ' + e.message;
  }
}

function showError(message) {
  $('error').textContent = message;
  $('error').hidden = !message;
}

async function init() {
  setupDropzone();
  $('processBtn').addEventListener('click', processImage);
  $('refreshOrders').addEventListener('click', loadOrders);
  try {
    [state.catalog, state.presets, state.styles] = await Promise.all([
      api('/api/treatments'), api('/api/presets'), api('/api/styles'),
    ]);
    renderCatalog();
    renderPresets();
    renderStyles();
  } catch (e) {
    $('catalog').innerHTML = '<p class="error">Could not connect to the backend. Is the ApiServer running?</p>';
    $('presets').innerHTML = '';
  }
  render();
  loadOrders();
}

init();
