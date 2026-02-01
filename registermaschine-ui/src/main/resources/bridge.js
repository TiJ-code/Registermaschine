/**
 * JASM IDE Bridge
 * Connects the HTML View to the Java Core logic
 */

const editor = document.getElementById('code-editor');
const lineNumbers = document.getElementById('line-numbers');
const instructionList = document.getElementById('instruction-list');
const registerList = document.getElementById('register-list');

// --- 1. EDITOR LOGIC ---

/**
 * Triggered on every keystroke.
 * Sends raw text to Java and replaces HTML with styled tokens.
 */
function onCodeInput() {
    const rawText = editor.innerText;

    // Sync line numbers
    const lines = rawText.split('\n').length;
    lineNumbers.innerHTML = Array.from({length: lines}, (_, i) => i + 1).join('<br>');

    // 1. Save Caret Position
    const offset = getCaretCharacterOffsetWithin(editor);

    // 2. Get Tokens from Java (Logic is in Java, Style is in CSS)
    // Expects format: [ ["mnemonic", "LOAD"], ["register", "R1"], ["number", "10"] ]
    const tokens = java.getTokens(rawText);

    let htmlBuilder = "";
    tokens.forEach(token => {
        const type = token[0]; // e.g., 'mnemonic'
        const text = token[1]; // e.g., 'LOAD'
        htmlBuilder += `<span class="token-${type}">${escapeHtml(text)}</span>`;
    });

    editor.innerHTML = htmlBuilder;

    // 3. Restore Caret Position
    setCurrentCursorPosition(editor, offset);
}

// Helper to prevent HTML injection
function escapeHtml(text) {
    return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

// --- 2. DYNAMIC UI GENERATION (Called by Java on Startup) ---

/**
 * Populates the Sidebar registers
 */
function initializeRegisters(count) {
    registerList.innerHTML = '';
    for (let i = 0; i < count; i++) {
        const isAccu = (i === 0);
        const name = isAccu ? "ACCU" : `R${i}`;
        const className = isAccu ? "reg-card accumulator" : "reg-card";

        registerList.innerHTML += `
            <div class="${className}" id="reg-${i}">
                <span class="reg-name">${name}</span>
                <span class="reg-value" id="val-${i}">0</span>
            </div>`;
    }
}

/**
 * Populates the Instruction documentation from the Registry
 */
function initializeDocs(instructions) {
    // instructions: [ {name: "ADD", desc: "Adds value to ACCU"}, ... ]
    instructionList.innerHTML = Array.from(instructions).map(ins => {
        console.log(ins);
    return `
        <div class="instr-item">
            <span class="instr-name">${ins.get("name")}</span>
            <span class="instr-desc">${ins.get("description")}</span>
        </div>
    `;}).join('');
}

// --- 3. EXECUTION HOOKS (Called by Java during runtime) ---

function updateRegisterUI(index, value) {
    const card = document.getElementById(`reg-${index}`);
    const valSpan = document.getElementById(`val-${index}`);

    valSpan.innerText = value;
    card.classList.add('updated');
    setTimeout(() => card.classList.remove('updated'), 600);
}

function highlightLine(pc) {
    // Remove previous highlights
    document.querySelectorAll('.executing-line').forEach(el => el.classList.remove('executing-line'));

    // In contenteditable, lines are usually separated by <span> or text nodes.
    // A robust way is to wrap the specific line in a class.
    // (Simplified logic: assuming each line is a child of the editor)
    const lines = editor.childNodes;
    if (lines[pc]) {
        lines[pc].classList.add('executing-line');
    }
}

function toggleDocs() {
    const content = document.getElementById('instruction-list');
    const arrow = document.getElementById('docs-arrow');
    content.classList.toggle('hidden');
    arrow.innerText = content.classList.contains('hidden') ? "▲" : "▼";
}

// --- 4. CARET MANAGEMENT (The "Secret Sauce") ---

function getCaretCharacterOffsetWithin(element) {
    let caretOffset = 0;
    const doc = element.ownerDocument || element.document;
    const win = doc.defaultView || doc.parentWindow;
    const sel = win.getSelection();
    if (sel.rangeCount > 0) {
        const range = win.getSelection().getRangeAt(0);
        const preCaretRange = range.cloneRange();
        preCaretRange.selectNodeContents(element);
        preCaretRange.setEnd(range.endContainer, range.endOffset);
        caretOffset = preCaretRange.toString().length;
    }
    return caretOffset;
}

function setCurrentCursorPosition(element, offset) {
    if (offset < 0) return;
    const selection = window.getSelection();
    const range = createRange(element, { count: offset });
    if (range) {
        range.collapse(false);
        selection.removeAllRanges();
        selection.addRange(range);
    }
}

function createRange(node, chars, range) {
    if (!range) {
        range = document.createRange();
        range.selectNode(node);
        range.setStart(node, 0);
    }
    if (chars.count === 0) {
        range.setEnd(node, chars.count);
    } else if (node && chars.count > 0) {
        if (node.nodeType === Node.TEXT_NODE) {
            if (node.textContent.length < chars.count) {
                chars.count -= node.textContent.length;
            } else {
                range.setEnd(node, chars.count);
                chars.count = 0;
            }
        } else {
            for (let lp = 0; lp < node.childNodes.length; lp++) {
                range = createRange(node.childNodes[lp], chars, range);
                if (chars.count === 0) break;
            }
        }
    }
    return range;
}