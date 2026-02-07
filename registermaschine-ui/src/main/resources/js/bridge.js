/**
 * JASM IDE Bridge
 * Connects the HTML View to the Java Core logic
 */

const lineNumbers = document.getElementById('line-numbers');
const instructionList = document.getElementById('instruction-list');
const registerList = document.getElementById('register-list');

// JAVA BRIDGE
function println(text) {
    window.java.println(String(text));
}

// JAVA BRIDGE
function initialiseRegisters(count) {
    println("initReg: start");
    registerList.innerHTML = '';
    for (let i = 0; i < count; i++) {
        const name = `R${i}`;
        const className = "reg-card";

        registerList.innerHTML += `
            <div class="${className}" id="reg-${i}">
                <span class="reg-name">${name}</span>
                <span class="reg-value" id="val-${i}">0</span>
            </div>`;
    }
    println("initReg: end");
}

// JAVA BRIDGE
function initialiseDocs(instructions) {
    // instructions: [ {name: "ADD", desc: "Adds value to ACCU"}, ... ]
    const insArray = Array.isArray(instructions) ? instructions : Array.from(instructions);

    instructionList.innerHTML = insArray.map(ins => {
        let name = ins.get("name").toUpperCase();
        let desc = ins.get("description");

    return `
        <div class="instr-item">
            <span class="instr-name">${name}</span>
            <span class="instr-desc">${desc}</span>
        </div>
    `;}).join('');
}

// JAVA BRIDGE
function initialiseKeywords(sentKeywords) {
    editor.updateKeywords(sentKeywords);
}

// JAVA BRIDGE
function runCode() {
    window.java.sendSourceCode("halloWelt");
    window.java.runProgram();
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
    println("toggleDocs");
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
                let child = node.childNodes[lp];
                range = createRange(child, chars, range);
                if (chars.count === 0) break;
            }
        }
    }
    return range;
}