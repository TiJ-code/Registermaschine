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
function runProgram() {
    if (!editor.input.value.trim()) {
        println("empty source code");
        return;
    }

    editor.setEditable(false);
    window.java.runProgram(editor.input.value.trim());
}

function stopProgram() {
    window.java.stopProgram();
    editor.setEditable(true);
}

// JAVA BRIDGE
function updateRegister(index, value) {
    const card = document.getElementById(`reg-${index}`);
    const valSpan = document.getElementById(`val-${index}`);

    valSpan.innerText = value;
    card.classList.add('updated');
    setTimeout(() => card.classList.remove('updated'), 600);
}

function updateRegistersBatch(updates) {
    Object.entries(updates).forEach(([value, index]) => {
        const valSpan = document.getElementById(`val-${index}`);
        if (valSpan) {
            valSpan.innerText = value;
        }
    });
}


// JAVA BRIDGE
function updateOutput(value) {
    const outputEl = document.getElementById('io-output');
    outputEl.innerText = value;

    outputEl.classList.add('updated');
    setTimeout(() => outputEl.classList.remove('updated'), 600);
}