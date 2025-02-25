const debugSpeeds = [0.1, 0.2, 0.5, 1.0]

function runCode(button) {
    let containsRun = button.textContent.includes("Run");

    if (window.java) {
        if (containsRun) {
            window.java.runCode(getCurrentDebugSpeed().toString());
        } else {
            window.java.endExecution();
        }
    }

    toggleRunCodeButton(button, containsRun);
}

function toggleRunCodeButton(button, running) {
    if (!button) {
        button = document.getElementsByClassName("ide-run-button")[0];
    }

    let text = "";
    if (running) {
        text = "< End >";
        button.classList.add("ide-running-code");
    } else {
        text = "> Run <";
        button.classList.remove("ide-running-code");
    }
    button.innerHTML = text;
}

function cycleDebugSpeed(button) {
    const currentSpeed = parseFloat(button.innerText.substring(0, 3));
    const nextSpeedIndex = (debugSpeeds.indexOf(currentSpeed) + 1) % debugSpeeds.length;
    button.innerHTML = `${debugSpeeds[nextSpeedIndex].toFixed(1)}s`;
}

function getCurrentDebugSpeed() {
    return parseFloat(document.getElementsByClassName("ide-exec-speed")[0].innerText.substring(0, 3)).toFixed(1);
}

function sendDebugMode(input) {
    const value = input.checked;
    if (window.java) {
        window.java.setDebugMode(value);
    }
}

function updateRegister(address, value) {
    let htmlId = "";
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

function setInitialMaxJumpDepth(maxDepth) {
    document.getElementsByClassName("ide-jump-depth")[0].innerHTML = `Jump-Depth: 0 / ${maxDepth}`;
}

function updateJumpDepth(currentDepth) {
    const element = document.getElementsByClassName("ide-jump-depth")[0];
    const lastPart = element.innerHTML.split(": ")[1].split(" / ");
    element.innerHTML = `Jump-Depth: ${currentDepth} / ${lastPart[1]}`;
}

function toggleSendButton(toDisabled) {
    const textElement = document.getElementsByClassName("ide-ip-text")[0];
    const inputElement = document.getElementsByClassName("ide-input-text")[0];
    if (toDisabled) {
        textElement.classList.add("ide-text-disabled");
    } else {
        textElement.classList.remove("ide-text-disabled");
    }
    textElement.disabled = toDisabled;
    inputElement.disabled = toDisabled;
}

function send(inputButton) {
    if (inputButton.disabled) return;

    const inputField = document.getElementsByClassName("ide-input-text")[0];
    if (window.java) {
        window.java.sendInput(inputField.value);
    }
    inputField.value = "";
}

function log(text) {
    logColour(text, "log-info");
}

function logError(text) {
    logColour(text, "log-error");
}

function logColour(text, colour) {
    const statusElement = document.getElementById("status-output");
    statusElement.innerHTML = text;

    statusElement.classList.remove("log-info");
    statusElement.classList.remove("log-error");
    statusElement.classList.add(colour);

    statusElement.classList.add('blink');
    setTimeout(() => {
        statusElement.classList.remove('blink');
    }, 500);
}

function outputValue(value) {
    const element = document.getElementById("output-text");
    element.innerHTML = value;
    element.classList.add('blink');

    setTimeout(() => {
        element.classList.remove('blink');
    }, 500);
}

function loadFile() {
    if (window.java) {
        window.java.loadFile();
    }
}

function saveFile() {
    if (window.java) {
        window.java.saveFile();
    }
}

function displayLoadedFile(filename) {
    document.getElementsByClassName("file-name")[0].innerHTML = filename;
}

function markLoadedFileAsEdited() {
    const element = document.getElementsByClassName("file-name")[0];
    if (!element.innerHTML.endsWith("*")) {
        element.innerHTML = `${element.innerHTML}*`;
    }
}

function markLoadedFileAsUnedited() {
    const element = document.getElementsByClassName("file-name")[0];
    if (element.innerHTML.endsWith("*")) {
        element.innerHTML = element.innerHTML.substring(0, element.innerHTML.length - 1);
    }
}