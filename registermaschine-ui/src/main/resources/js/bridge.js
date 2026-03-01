/**
 * JASM IDE Bridge
 * Connects the HTML View to the Java Core logic
 */
let registerCount = 0, useDebug = false;
const instructionList = document.getElementById('instruction-list');
const registerList = document.getElementById('register-list');

const runButton = document.getElementById("run-button");
const stopButton = document.getElementById("stop-button");

// shit for the docs
const io_input = document.getElementById('io-input');
const io_input_submit = document.getElementById("io-input-submit");

// JAVA BRIDGE
function println(text) {
    window.java.println(String(text));
}

// JAVA BRIDGE
function initialiseRegisters(count) {
    registerCount = count;

    const cards = [];
    for (let i = 0; i < count; i++) {
        cards.push(`
            <div class="reg-card" id="reg-${i}">
                <span class="reg-name">R${i}</span>
                <span class="reg-value" id="val-${i}">0</span>
            </div>`
        );
    }

    registerList.innerHTML = cards.join('');
}

// JAVA BRIDGE
function initialiseDocs(instructions) {
    // instructions: [ {name: "ADD", desc: "Adds value to ACCU"}, ... ]
    const insArray = Array.isArray(instructions) ? instructions : Array.from(instructions);

    const cards = [];
    for (let i = 0; i < insArray.length; i++) {
        cards.push(`
            <div class="instr-item">
                <span class="instr-name">${insArray[i].name().toUpperCase()}</span>
                <span class="instr-desc">${insArray[i].description()}</span>
            </div>
        `);
    }
    instructionList.innerHTML = cards.join('');
}

// JAVA BRIDGE
function initialiseKeywords(sentKeywords) {
    editor.updateKeywords(sentKeywords);
}

// JAVA BRIDGE
function runProgram() {
    if (!editor.input.value.trim()) {
        toast("Nothing to Compile", "No source code in editor", AlertTypes.WARNING);
        return;
    }

    editor.generateLineMap();

    for (let i = 0; i < registerCount; i++) {
        const valSpan = document.getElementById(`val-${i}`);
        const card = document.getElementById(`reg-${i}`);

        if (valSpan) valSpan.innerText = "0";
        if (card) card.classList.remove('updated');
    }

    runButton.classList.add("disabled");
    stopButton.classList.remove("disabled");

    editor.setEditable(false);
    window.java.runProgram(editor.code.trim(), useDebug);
}

// JAVA BRIDGE
function stopProgram() {
    window.java.stopProgram();
    programFinished();
}

// JAVA BRIDGE
function programFinished() {
    runButton.classList.remove("disabled");
    stopButton.classList.add("disabled");
    editor.setEditable(true);
    editor.setExecutionLine(null);
}

// JAVA BRIDGE
function updateRegister(index, value) {
    const valSpan = document.getElementById(`val-${index}`);
    if (!valSpan) return;

    valSpan.innerText = value;

    const card = valSpan.parentElement;
    card.classList.remove('updated');
    void card.offsetWidth;
    card.classList.add('updated');
}

// JAVA BRIDGE
function updateOutput(value) {
    const outputEl = document.getElementById('io-output');
    outputEl.innerText = value;

    setSidebarSection('io-container', 'io-arrow', true);

    outputEl.classList.add('updated');
    setTimeout(() => outputEl.classList.remove('updated'), 600);
}

// JAVA BRIDGE
function submitInput() {
    const value = parseInt(io_input.value);
    if (isNaN(value)) return;

    window.java.provideInput(value);
    io_input.classList.add("disabled");
    io_input_submit.classList.add("disabled");
}

// JAVA BRIDGE
function onInputRequested() {
    io_input.value = '';
    io_input.classList.remove("disabled");
    io_input_submit.classList.remove("disabled");
    setSidebarSection('io-container', 'io-arrow', true);
}

// FROM JAVA
function loadCode(loadedCode) {
    editor.code = loadedCode;
    editor.markClean();
}

let globalCurrentFileName = "";
const filenameDisplay = document.getElementById("filename-display");
// FROM JAVA
function setFileName(fileName) {
    globalCurrentFileName = fileName;
    filenameDisplay.innerText = globalCurrentFileName;
}

// FROM JAVA
function onFileActionConfirmed() {
    editor.markClean();
}

// FROM JAVA
function toggleBugButton(active) {
    document.getElementById("submit-bug-button").disabled = !active;
}

// TO JAVA
function sendBugReport(title, description) {
    window.java?.reportBug(title, description);
}

// FROM JAVA
function toast(title, message, type) {
    showAlert(title, message, type);
}

// FROM JAVA
function updateExecutionState(pc) {
    if (useDebug && editor.lineMap) {
        const actualLine = editor.lineMap[pc];

        if (actualLine !== undefined) {
            editor.setExecutionLine(actualLine);
        }
    }
}