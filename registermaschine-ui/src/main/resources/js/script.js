const UI = {
    docsContainer: document.getElementById('instruction-container'),
    docsArrow: document.getElementById('docs-arrow'),
    ioOutput: document.getElementById('io-output')
}

function toggleDocs() {
    if (!UI.docsContainer) return;

    UI.docsContainer.classList.toggle('docs-visible');
    UI.docsArrow.classList.toggle('rotated');
}

function toggleDebug(e) {
    useDebug = e.checked;
}

function submitBugReport() {
    const titleElement = document.getElementById("bug-title");
    const titleValue = titleElement.value;

    if (!titleValue) {
        titleElement.classList.add('empty');
        return;
    } else {
        titleElement.classList.remove('empty');
    }

    const descriptionElement = document.getElementById("bug-description");
    const descriptionValue = descriptionElement.value;

    if (!descriptionValue) {
        descriptionElement.classList.add('empty');
        return
    } else {
        descriptionElement.classList.remove('empty');
    }

    sendBugReport(titleValue, descriptionValue);
}

function toggleModal(modalId, active) {
    const modalElement = document.getElementById(modalId);
    if (active)
        modalElement.classList.add('active');
    else
        modalElement.classList.remove('active');
}