function updateJumpDepth(currentDepth) {
    let element = document.getElementById("jump-status-value");
    let parts = element.innerHTML.split(" / ");
    element.innerHTML = `${currentDepth} / ${parts[1]}`;
}

function setInitialMaxJumpDepth(maxDepth) {
    document.getElementById("jump-status-value").innerHTML = `0 / ${maxDepth}`;
}

function updateRegister(address, value) {
    let htmlId = "";  // Use let instead of const
    if (address === 0) {
        htmlId = "accu";
    } else {
        let hexStr = address.toString(16).toUpperCase().padStart(2, '0');
        htmlId = "r" + hexStr;
    }
    let element = document.getElementById(htmlId);
    element.innerHTML = value;

    element.classList.add('blink');

    setTimeout(() => {
        element.classList.remove('blink');
    }, 500);
}

function toggleRunCodeButton(button, isDisabled) {
    if (button == null) {
        button = document.getElementById("run-code");
    }

    if (isDisabled) {
        button.innerHTML = "Beenden";
        button.classList.add("running");
    } else {
        button.innerHTML = "Ausführen";
        button.classList.remove("running");
    }
}

function toggleSendButton(isDisabled) {
    document.getElementById("send-input").disabled = isDisabled;
}

function outputValue(value) {
    let element = document.getElementById("output-text");
    element.innerHTML = value;

    element.classList.add('blink');
    setTimeout(() => {
        element.classList.remove('blink');
    }, 500);
}

function runCode(button) {
    if (window.java && typeof window.java.runCode === 'function') {
        if (button.innerHTML === "Ausführen") {
            window.java.runCode();
            toggleRunCodeButton(button, true);
        } else {
            window.java.endExecution();
            toggleRunCodeButton(button, false);
        }
    } else {
        console.error("Java interface not available.");
    }
}

function send() {
    let inputField = document.getElementById("input-field");
    let value = inputField.value;
    inputField.value = "";
    inputField.placeholder = value;
    if (window.java && typeof window.java.sendInput === 'function') {
        window.java.sendInput(value);
    } else {
        console.error("Java app interface not available.");
    }
}

function toggleDebugMode(element) {
    let debug = element.innerHTML === "ON";
    if (debug) {
        element.innerHTML = "OFF";
        element.classList.remove("debug-enabled");
        element.classList.add("debug-disabled");
    } else {
        element.innerHTML = "ON";
        element.classList.add("debug-enabled");
        element.classList.remove("debug-disabled");
    }
    if (window.java) { 
        window.java.setDebugMode(!debug);
    }
}

// Standard log function (neutral info)
function log(text) {
    logColor(text, "log-info");
}

// Error log function (for critical issues)
function logError(text) {
    logColor(text, "log-error");
}

// Update log display with optional animation
function displayLog(text, colour) {
    const statusElement = document.getElementById("status-text-value");
    
    // Clear previous log classes
    statusElement.className = "";
    
    // Apply new log content and style
    statusElement.innerHTML = text;
    if (colour) {
        statusElement.classList.add(colour);
    }

    // Optional fade-in effect for smoother appearance
    statusElement.style.opacity = 0;
    setTimeout(() => statusElement.style.opacity = 1, 50);
}

function displayMachineCode(text) {
    document.getElementById("text-area").innerHTML = text;
}