/*
 * PixelForge frontend (plain JavaScript, no frameworks).
 *
 * Talks to the Java backend (com.pixelforge.api.ApiServer):
 *   GET  /api/treatments               -> catalog
 *   POST /api/process?treatments=...   -> body = image bytes
 *   GET  /api/orders                   -> history
 *
 * Classes in this file:
 *   ApiClient      - HTTP calls to the backend
 *   Pipeline       - the ordered list of treatments the user picked (the model)
 *   CatalogView    - list of available treatments with their parameters
 *   PipelineView   - the ordered list, with drag & drop
 *   ResultView     - before/after, nested decorator boxes, cost breakdown
 *   OrdersView     - order history
 *   App            - creates everything and connects the pieces
 */

const API_URL = 'http://localhost:8080/api';

/* Mirrors TreatmentType.BASE and PipelineBuilder in the backend (only used for the estimate). */
const BASE_COST = 500;
const BASE_SECONDS = 1;
const DISCOUNT_THRESHOLD = 4;
const DISCOUNT_PERCENT = 10;
const MAX_IMAGE_BYTES = 5 * 1024 * 1024;

const money = (value) => (value < 0 ? '-$' : '$') + Math.abs(value).toLocaleString('es-CO');


/* ======================================================================
 * ApiClient
 * ====================================================================== */
class ApiClient {

    constructor(baseUrl) {
        this.baseUrl = baseUrl;
    }

    async getTreatments() {
        return this.#request('/treatments');
    }

    async getOrders() {
        return this.#request('/orders');
    }

    /** @param {File} file  @param {string[]} treatments e.g. ['RESIZE', 'WATERMARK:MyStore'] */
    async process(file, treatments) {
        const query = '?treatments=' + encodeURIComponent(treatments.join(','));
        return this.#request('/process' + query, { method: 'POST', body: file });
    }

    async #request(path, options = {}) {
        const response = await fetch(this.baseUrl + path, options);
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.error || 'Error ' + response.status);
        }
        return data;
    }
}


/* ======================================================================
 * Pipeline (model): ordered treatments chosen by the user
 * ====================================================================== */
class Pipeline {

    constructor() {
        this.steps = [];      // [{ treatment, parameter, label }]
        this.listeners = [];
    }

    onChange(listener) {
        this.listeners.push(listener);
    }

    add(treatment, parameter, label) {
        this.steps.push({ treatment, parameter, label });
        this.#notify();
    }

    remove(index) {
        this.steps.splice(index, 1);
        this.#notify();
    }

    move(fromIndex, toIndex) {
        const [step] = this.steps.splice(fromIndex, 1);
        this.steps.splice(toIndex, 0, step);
        this.#notify();
    }

    /** Format the backend expects: "TYPE" or "TYPE:parameter". */
    toRequestList() {
        return this.steps.map(step =>
            step.parameter ? `${step.treatment.id}:${step.parameter}` : step.treatment.id);
    }

    hasDiscount() {
        return this.steps.length >= DISCOUNT_THRESHOLD;
    }

    estimatedCost() {
        const subtotal = this.steps.reduce((sum, step) => sum + step.treatment.cost, BASE_COST);
        return this.hasDiscount() ? subtotal - Math.floor(subtotal * DISCOUNT_PERCENT / 100) : subtotal;
    }

    estimatedSeconds() {
        return this.steps.reduce((sum, step) => sum + step.treatment.seconds, BASE_SECONDS);
    }

    #notify() {
        this.listeners.forEach(listener => listener(this));
    }
}


/* ======================================================================
 * CatalogView: one card per treatment, with its parameter input if needed
 * ====================================================================== */
class CatalogView {

    constructor(container, pipeline) {
        this.container = container;
        this.pipeline = pipeline;
    }

    render(treatments) {
        this.container.innerHTML = '';
        treatments.forEach(treatment => this.container.appendChild(this.#createCard(treatment)));
    }

    #createCard(treatment) {
        const card = document.createElement('div');
        card.className = 'catalog-item';
        card.innerHTML = `
            <div>
                <div class="name">${treatment.name}</div>
                <div class="meta">${money(treatment.cost)} · ${treatment.seconds} s</div>
            </div>
            <button type="button">+ Agregar</button>`;

        const paramInput = this.#createParameterInput(treatment);
        if (paramInput) {
            card.appendChild(paramInput.element);
        }

        card.querySelector('button').addEventListener('click', () => {
            const parameter = paramInput ? paramInput.read() : null;
            if (paramInput && !parameter) {
                return; // read() already showed the problem
            }
            const label = parameter ? `${treatment.name} (${parameter})` : treatment.name;
            this.pipeline.add(treatment, parameter, label);
        });
        return card;
    }

    /** Returns { element, read() } or null when the treatment has no parameter. */
    #createParameterInput(treatment) {
        switch (treatment.id) {
            case 'COLOR_FILTER': {
                const select = document.createElement('select');
                select.innerHTML = `
                    <option value="GRAYSCALE">Blanco y negro</option>
                    <option value="BRIGHTEN">Más brillo</option>
                    <option value="HIGH_CONTRAST">Alto contraste</option>`;
                return { element: select, read: () => select.value };
            }
            case 'WATERMARK': {
                const input = document.createElement('input');
                input.type = 'text';
                input.placeholder = 'Nombre de la tienda';
                input.value = 'Mi Tienda';
                return {
                    element: input,
                    read: () => {
                        const text = input.value.trim();
                        if (!text || text.includes(',')) { // commas separate treatments in the request
                            alert('Escribe un texto sin comas para la marca de agua.');
                            return null;
                        }
                        return text;
                    }
                };
            }
            case 'BORDER': {
                const row = document.createElement('div');
                row.className = 'param-row';
                row.innerHTML = `<input type="color" value="#e8590c"><span class="meta">Color del marco</span>`;
                const color = row.querySelector('input');
                return { element: row, read: () => color.value.replace('#', '').toUpperCase() };
            }
            default:
                if (!treatment.parameterHint) {
                    return null;
                }
                // Generic fallback: a new treatment with a parameter works without changing this file.
                const input = document.createElement('input');
                input.type = 'text';
                input.placeholder = treatment.parameterHint;
                return { element: input, read: () => input.value.trim() || null };
        }
    }
}


/* ======================================================================
 * PipelineView: shows the chosen order and lets the user drag to reorder
 * ====================================================================== */
class PipelineView {

    constructor(list, emptyMessage, pipeline) {
        this.list = list;
        this.emptyMessage = emptyMessage;
        this.pipeline = pipeline;
        this.draggedIndex = null;
        pipeline.onChange(() => this.render());
    }

    render() {
        this.list.innerHTML = '';
        this.emptyMessage.hidden = this.pipeline.steps.length > 0;

        this.pipeline.steps.forEach((step, index) => {
            const item = document.createElement('li');
            item.draggable = true;
            item.innerHTML = `
                <span class="order">${index + 1}</span>
                <span class="label">${this.#escape(step.label)}<br><small>${money(step.treatment.cost)} · ${step.treatment.seconds} s</small></span>
                <button class="remove" type="button" title="Quitar">×</button>`;

            item.querySelector('.remove').addEventListener('click', () => this.pipeline.remove(index));
            this.#enableDragAndDrop(item, index);
            this.list.appendChild(item);
        });
    }

    #enableDragAndDrop(item, index) {
        item.addEventListener('dragstart', () => {
            this.draggedIndex = index;
            item.classList.add('dragging');
        });
        item.addEventListener('dragend', () => item.classList.remove('dragging'));
        item.addEventListener('dragover', (event) => {
            event.preventDefault();
            item.classList.add('drop-target');
        });
        item.addEventListener('dragleave', () => item.classList.remove('drop-target'));
        item.addEventListener('drop', (event) => {
            event.preventDefault();
            if (this.draggedIndex !== null && this.draggedIndex !== index) {
                this.pipeline.move(this.draggedIndex, index);
            }
            this.draggedIndex = null;
        });
    }

    #escape(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}


/* ======================================================================
 * ResultView: before/after images, nested decorator boxes and cost table
 * ====================================================================== */
class ResultView {

    /** Display name prefix (from the backend) -> Java class, to show which object each layer is. */
    static JAVA_CLASSES = [
        ['Base image', 'BaseImage'],
        ['Remove background', 'RemoveBackgroundDecorator'],
        ['Resize', 'ResizeDecorator'],
        ['Color filter', 'ColorFilterDecorator'],
        ['Watermark', 'WatermarkDecorator'],
        ['Border', 'BorderDecorator'],
        ['Web compression', 'CompressionDecorator'],
        ['Discount', 'DiscountDecorator'],
    ];

    static COLORS = ['#1d2230', '#e8590c', '#1971c2', '#2b8a3e', '#9c36b5', '#c2255c', '#0c8599', '#e67700'];

    constructor(elements) {
        this.el = elements;
    }

    showOriginal(file) {
        this.#showImage(this.el.beforeImg, URL.createObjectURL(file));
    }

    showResult(data) {
        const src = `data:image/${data.imageFormat};base64,${data.imageBase64}`;
        this.#showImage(this.el.afterImg, src);

        this.el.downloadLink.href = src;
        this.el.downloadLink.download = `pixelforge-order-${data.order.id}.${data.imageFormat}`;
        this.el.downloadLink.hidden = false;

        this.#renderLayers(data.order.layers);
        this.#renderBreakdown(data.order);
    }

    /**
     * layers come from the backend from the innermost (BaseImage) to the outermost.
     * We build the boxes the same way the decorators were built: each new box wraps the previous one.
     */
    #renderLayers(layers) {
        let inner = null;
        layers.forEach((layer, index) => {
            const box = document.createElement('div');
            box.className = 'layer-box' + (index === 0 ? ' core' : '');
            box.style.setProperty('--layer-color', ResultView.COLORS[index % ResultView.COLORS.length]);

            const title = document.createElement('div');
            title.className = 'layer-title';
            title.innerHTML = `<span></span><code>${ResultView.#javaClassFor(layer.name)}</code>`;
            title.querySelector('span').textContent = `${index + 1}. ${layer.name}`;
            box.appendChild(title);

            if (inner) {
                box.appendChild(inner);
            }
            inner = box;
        });

        this.el.layersView.innerHTML = '';
        this.el.layersView.appendChild(inner);
    }

    #renderBreakdown(order) {
        this.el.breakdownBody.innerHTML = '';
        order.layers.forEach((layer, index) => {
            const row = document.createElement('tr');
            row.innerHTML = `<td>${index + 1}</td><td></td>
                <td class="${layer.cost < 0 ? 'negative' : ''}">${money(layer.cost)}</td>
                <td>${layer.seconds} s</td>`;
            row.children[1].textContent = layer.name;
            this.el.breakdownBody.appendChild(row);
        });
        this.el.totalCost.textContent = money(order.totalCost);
        this.el.totalTime.textContent = order.totalSeconds + ' s';
    }

    #showImage(img, src) {
        img.src = src;
        img.classList.add('loaded');
    }

    static #javaClassFor(layerName) {
        const match = ResultView.JAVA_CLASSES.find(([prefix]) => layerName.startsWith(prefix));
        return match ? match[1] : 'TreatmentDecorator';
    }
}


/* ======================================================================
 * OrdersView: order history
 * ====================================================================== */
class OrdersView {

    constructor(list) {
        this.list = list;
    }

    render(orders) {
        this.list.innerHTML = '';
        if (orders.length === 0) {
            this.list.innerHTML = '<li class="empty">Sin pedidos todavía.</li>';
            return;
        }
        [...orders].reverse().forEach(order => {      // newest first
            const item = document.createElement('li');
            item.className = 'order';
            item.innerHTML = `
                <div class="order-head"><span>Pedido #${order.id}</span><span>${money(order.totalCost)}</span></div>
                <div class="order-desc"></div>`;
            item.querySelector('.order-desc').textContent =
                `${order.layers.length} capas · ${order.totalSeconds} s — ${order.description}`;
            this.list.appendChild(item);
        });
    }
}


/* ======================================================================
 * App: wires everything together
 * ====================================================================== */
class App {

    constructor() {
        this.api = new ApiClient(API_URL);
        this.pipeline = new Pipeline();
        this.file = null;

        const $ = (id) => document.getElementById(id);
        this.el = {
            status: $('server-status'),
            dropZone: $('drop-zone'),
            dropText: $('drop-text'),
            fileInput: $('file-input'),
            estimateCost: $('estimate-cost'),
            estimateTime: $('estimate-time'),
            discountNote: $('discount-note'),
            processBtn: $('process-btn'),
            errorBox: $('error-box'),
        };

        this.catalogView = new CatalogView($('catalog'), this.pipeline);
        this.pipelineView = new PipelineView($('pipeline'), $('pipeline-empty'), this.pipeline);
        this.resultView = new ResultView({
            beforeImg: $('before-img'),
            afterImg: $('after-img'),
            downloadLink: $('download-link'),
            layersView: $('layers-view'),
            breakdownBody: $('breakdown-body'),
            totalCost: $('total-cost'),
            totalTime: $('total-time'),
        });
        this.ordersView = new OrdersView($('orders'));

        this.pipeline.onChange(() => this.#updateSummary());
        this.#bindUpload();
        this.el.processBtn.addEventListener('click', () => this.#process());
        $('refresh-orders').addEventListener('click', () => this.#loadOrders());
    }

    async start() {
        try {
            this.catalogView.render(await this.api.getTreatments());
            this.#setStatus('Servidor conectado', 'status-ok');
            await this.#loadOrders();
        } catch (error) {
            this.#setStatus('Servidor apagado', 'status-down');
            this.#showError('No se pudo conectar con el backend en ' + API_URL +
                '. Ejecuta ApiServer y recarga la página.');
        }
        this.#updateSummary();
    }

    #bindUpload() {
        const zone = this.el.dropZone;
        this.el.fileInput.addEventListener('change', () => this.#selectFile(this.el.fileInput.files[0]));
        zone.addEventListener('dragover', (event) => { event.preventDefault(); zone.classList.add('dragging'); });
        zone.addEventListener('dragleave', () => zone.classList.remove('dragging'));
        zone.addEventListener('drop', (event) => {
            event.preventDefault();
            zone.classList.remove('dragging');
            this.#selectFile(event.dataTransfer.files[0]);
        });
    }

    #selectFile(file) {
        if (!file) {
            return;
        }
        if (!['image/png', 'image/jpeg'].includes(file.type)) {
            this.#showError('Solo se aceptan imágenes JPG o PNG.');
            return;
        }
        if (file.size > MAX_IMAGE_BYTES) {
            this.#showError('La imagen supera los 5 MB.');
            return;
        }
        this.file = file;
        this.el.dropZone.classList.add('has-file');
        this.el.dropText.innerHTML = '';
        this.el.dropText.append(file.name, document.createElement('br'));
        this.el.dropText.insertAdjacentHTML('beforeend', `<small>${(file.size / 1024).toFixed(0)} KB · clic para cambiar</small>`);
        this.resultView.showOriginal(file);
        this.#hideError();
        this.#updateSummary();
    }

    async #process() {
        this.#hideError();
        this.el.processBtn.disabled = true;
        this.el.processBtn.textContent = 'Procesando…';
        try {
            const data = await this.api.process(this.file, this.pipeline.toRequestList());
            this.resultView.showResult(data);
            await this.#loadOrders();
        } catch (error) {
            this.#showError(error.message); // e.g. "COMPRESSION must be the last treatment"
        } finally {
            this.el.processBtn.textContent = 'Procesar imagen';
            this.#updateSummary();
        }
    }

    async #loadOrders() {
        try {
            this.ordersView.render(await this.api.getOrders());
        } catch (error) {
            /* status badge already shows the server is down */
        }
    }

    #updateSummary() {
        this.el.estimateCost.textContent = money(this.pipeline.estimatedCost());
        this.el.estimateTime.textContent = this.pipeline.estimatedSeconds() + ' s';
        this.el.discountNote.hidden = !this.pipeline.hasDiscount();
        this.el.processBtn.disabled = !this.file || this.pipeline.steps.length === 0;
    }

    #setStatus(text, cssClass) {
        this.el.status.textContent = text;
        this.el.status.className = 'status ' + cssClass;
    }

    #showError(message) {
        this.el.errorBox.textContent = message;
        this.el.errorBox.hidden = false;
    }

    #hideError() {
        this.el.errorBox.hidden = true;
    }
}

new App().start();
