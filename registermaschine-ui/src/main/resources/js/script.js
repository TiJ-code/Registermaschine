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