/**
 * JASM IDE Bridge
 * Connects the HTML View to the Java Core logic
 */

let registerCount = 0;
const instructionList = document.getElementById('instruction-list');
const registerList = document.getElementById('register-list');

// JAVA BRIDGE
function println(text) {
    window.java.println(String(text));
}

// JAVA BRIDGE
function initialiseRegisters(count) {
    registerCount = count;
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

// JAVA BRIDGE
function stopProgram() {
    window.java.stopProgram();
    programFinished();
}

// JAVA BRIDGE
function programFinished() {
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

// JAVA BRIDGE
function updateOutput(value) {
    const outputEl = document.getElementById('io-output');
    outputEl.innerText = value;

    outputEl.classList.add('updated');
    setTimeout(() => outputEl.classList.remove('updated'), 600);
}

// shit for the docs
const io_input = document.getElementById('io-input');
const io_input_submit = document.getElementById("io-input-submit");

// JAVA BRIDGE
function submitInput() {
    const value = parseInt(document.getElementById("io-input").value);
    window.java.provideInput(value);
    io_input.classList.add("disabled");
    io_input_submit.classList.add("disabled");
}

// JAVA BRIDGE
function onInputRequested() {
    io_input.classList.remove("disabled");
    io_input_submit.classList.remove("disabled");
    setSidebarSection('io-container', 'io-arrow', true);
}