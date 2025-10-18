document.addEventListener('DOMContentLoaded', () => {
    const codeEditor = document.getElementById('codeEditor');
    const runBtn = document.getElementById('runBtn');
    const debugBtn = document.getElementById('debugBtn');
    const stopBtn = document.getElementById('stopBtn');
    const statusMessage = document.getElementById('statusMessage');
    const executionTime = document.getElementById('executionTime');
    const lineNumbers = document.querySelector('.line-numbers');
    const gutter = document.querySelector('.gutter');

    let isRunning = false;
    let breakpoints = new Set();
    let registers = { acc: 0, r: new Array(8).fill(0) };

    const MAX_LINES = 256;

    const toHex = val => "0x" + val.toString(16).toUpperCase().padStart(4, '0');
    const toBin = val => {
        const binary = val.toString(2).padStart(16, '0');
        return binary.replace(/(\d{4})(?=\d)/g, '$1 ');
    };

    function updateRegistersDisplay() {
        const accVal = document.querySelector('#accumulator .register-value');
        const accBin = document.querySelector('#accumulator .register-binary');
        if (accVal) accVal.textContent = toHex(registers.acc);
        if (accBin) accBin.textContent = toBin(registers.acc);

        for (let i = 0; i < 8; i++) {
            const valEl = document.getElementById(`reg0${i}`);
            const binEl = document.getElementById(`reg0${i}Bin`);
            if (valEl) valEl.textContent = toHex(registers.r[i]);
            if (binEl) binEl.textContent = toBin(registers.r[i]);
        }
    }

    // normalize innerHTML to plain-lines reliably (handles <div>, <br>, nested tags)
    function getEditorLinesArray() {
        // Create a temporary div to parse the HTML structure
        const temp = document.createElement('div');
        temp.innerHTML = codeEditor.innerHTML;

        // Replace <br> elements with newlines and get text content
        const text = temp.innerText || '';

        // Normalize line endings and split into lines
        let lines = text.replace(/\r\n?/g, '\n').split('\n');

        // Ensure we have at least one line
        if (lines.length === 0) return [''];

        // Cap at MAX_LINES
        if (lines.length > MAX_LINES) {
            lines.length = MAX_LINES;
        }

        return lines;
    }

    // Rebuild the visible line numbers and gutter (clamped to MAX_LINES)
    function updateLineNumbers() {
        // Get all text nodes and br elements to count lines
        const walker = document.createTreeWalker(
            codeEditor,
            NodeFilter.SHOW_TEXT | NodeFilter.SHOW_ELEMENT,
            {
                acceptNode: function(node) {
                    // Accept text nodes and br elements
                    if (node.nodeType === Node.TEXT_NODE && node.nodeValue.trim() !== '') {
                        return NodeFilter.FILTER_ACCEPT;
                    }
                    if (node.nodeName === 'BR') {
                        return NodeFilter.FILTER_ACCEPT;
                    }
                    return NodeFilter.FILTER_SKIP;
                }
            }
        );

        let lineCount = 1; // Start with 1 line (empty editor has 1 line)
        while (walker.nextNode()) {
            if (walker.currentNode.nodeName === 'BR') {
                lineCount++;
            }
        }

        // Ensure we have at least one line and cap at MAX_LINES
        const lines = Math.min(Math.max(1, lineCount), MAX_LINES);

        // Build line numbers HTML
        let html = '';
        for (let i = 0; i < lines; i++) {
            const label = "0x" + i.toString(16).toUpperCase().padStart(2, '0');
            const lineClass = 'line-number' + (i >= lineCount - 1 ? ' empty-line' : '');
            html += `<div class="${lineClass}" data-line="${i}">${label}</div>`;
        }
        lineNumbers.innerHTML = html;

        // Build gutter (breakpoint dots)
        gutter.innerHTML = Array.from({ length: lines }, (_, i) =>
            `<div class="breakpoint${breakpoints.has(i) ? ' active' : ''}" data-line="${i}"></div>`
        ).join('');
    }

    // Make Enter insert a simple newline character rather than <div>.. to avoid structural branching
    codeEditor.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            const selection = window.getSelection();
            if (!selection.rangeCount) return;

            const range = selection.getRangeAt(0);
            const br = document.createElement('br');
            const textNode = document.createTextNode('\u00A0'); // Non-breaking space to ensure line height

            // Insert new line
            range.deleteContents();
            range.insertNode(br);
            range.setStartAfter(br);
            range.insertNode(textNode);
            range.setStart(textNode, 0)
            range.collapse(true);

            // Update selection
            selection.removeAllRanges();
            selection.addRange(range);
        }
    });

    // Keep line numbers in sync with visible editor scroll
    codeEditor.addEventListener('scroll', () => {
        lineNumbers.scrollTop = codeEditor.scrollTop;
        gutter.scrollTop = codeEditor.scrollTop;
    });

    // Update when typing/pasting/mutations occur
    codeEditor.addEventListener('input', () => {
        requestAnimationFrame(() => {
            updateLineNumbers();
            // Check if we're at the end of the content
            const selection = window.getSelection();
            if (selection.rangeCount > 0) {
                const range = selection.getRangeAt(0);
                const cursorIsAtEnd = range.endOffset === range.endContainer.length &&
                    range.endOffset === range.startOffset;

                if (cursorIsAtEnd) {
                    codeEditor.scrollTop = codeEditor.scrollHeight;
                }
            }
        });
    });
    codeEditor.addEventListener('keyup', (e) => {
        if (['Backspace', 'Delete'].includes(e.key)) requestAnimationFrame(updateLineNumbers);
    });
    codeEditor.addEventListener('paste', () => requestAnimationFrame(updateLineNumbers));

    // Observe deeper DOM mutations (covers cases where browser modifies structure)
    const observer = new MutationObserver(() => requestAnimationFrame(updateLineNumbers));
    observer.observe(codeEditor, { childList: true, characterData: true, subtree: true });

    // Breakpoint toggling
    function toggleBreakpoint(line) {
        if (breakpoints.has(line)) breakpoints.delete(line);
        else breakpoints.add(line);
        updateLineNumbers();
    }

    gutter.addEventListener('click', (e) => {
        const line = e.target && e.target.dataset && e.target.dataset.line;
        if (line !== undefined) toggleBreakpoint(parseInt(line, 10));
    });

    // Run / Stop simulation
    runBtn.addEventListener('click', () => {
        if (isRunning) return;
        isRunning = true;
        runBtn.disabled = true;
        debugBtn.disabled = true;
        stopBtn.disabled = false;
        statusMessage.textContent = "Running...";
        const start = performance.now();

        setTimeout(() => {
            registers.acc = Math.floor(Math.random() * 65535);
            for (let i = 0; i < 8; i++) registers.r[i] = Math.floor(Math.random() * 65535);
            updateRegistersDisplay();
            statusMessage.textContent = "Execution completed";
            executionTime.textContent = `${Math.round(performance.now() - start)}ms`;
            isRunning = false;
            runBtn.disabled = false;
            debugBtn.disabled = false;
            stopBtn.disabled = true;
        }, 1200);
    });

    stopBtn.addEventListener('click', () => {
        if (!isRunning) return;
        isRunning = false;
        statusMessage.textContent = "Execution stopped";
        stopBtn.disabled = true;
        runBtn.disabled = false;
        debugBtn.disabled = false;
    });

    // Initialize editor content and UI
    codeEditor.textContent = "";
    updateRegistersDisplay();
    updateLineNumbers();
});