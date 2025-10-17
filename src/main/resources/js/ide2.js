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
    const toBin = val => val.toString(2).padStart(16, '0');

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
        // Grab HTML and convert block tags to \n, then strip tags and split
        let html = codeEditor.innerHTML || '';

        // Replace CRLF
        html = html.replace(/\r\n/g, '\n');

        // Replace <div> and <p> boundaries with a single marker newline
        // Many browsers create <div><br></div> for empty lines — handle those too.
        html = html.replace(/<div[^>]*>/gi, '\n');
        html = html.replace(/<\/div>/gi, '');
        html = html.replace(/<p[^>]*>/gi, '\n');
        html = html.replace(/<\/p>/gi, '');

        // Replace <br> with newline
        html = html.replace(/<br\s*\/?>/gi, '\n');

        // Remove remaining tags but keep their text content (strip tags)
        // Create a temporary element to leverage browser's decoding of entities
        const tmp = document.createElement('div');
        tmp.innerHTML = html;
        let text = tmp.textContent || tmp.innerText || '';

        // Normalize different newline counts and trim only trailing extra newline that browsers sometimes add
        // But keep intentional empty lines
        // Remove leading single newline inserted by replacement if present and editor was empty
        if (text.startsWith('\n') && codeEditor.textContent.trim().length === 0) {
            text = text.replace(/^\n/, '');
        }

        // Split into lines (even empty ones preserved)
        const arr = text.split('\n');

        // If the editor ends with a line break, text.split will create trailing empty string which is a valid blank line.
        // Cap array length at MAX_LINES
        if (arr.length > MAX_LINES) {
            arr.length = MAX_LINES;
        }

        // Guarantee at least one line
        if (arr.length === 0) return [''];

        return arr;
    }

    // Rebuild the visible line numbers and gutter (clamped to MAX_LINES)
    function updateLineNumbers() {
        const linesArr = getEditorLinesArray();
        const lines = Math.max(1, Math.min(linesArr.length, MAX_LINES));

        // Build line numbers HTML
        let html = '';
        for (let i = 0; i < lines; i++) {
            const label = "0x" + i.toString(16).toUpperCase().padStart(2, '0');
            html += `<div class="line-number" data-line="${i}">${label}</div>`;
        }
        lineNumbers.innerHTML = html;

        // Build gutter (breakpoint dots)
        gutter.innerHTML = Array.from({ length: lines }, (_, i) =>
            `<div class="breakpoint${breakpoints.has(i) ? ' active' : ''}" data-line="${i}"></div>`
        ).join('');
    }

    // Make Enter insert a simple newline character rather than <div>.. to avoid structural branching
    codeEditor.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            // Use insertText if available to avoid execCommand deprecation issues
            const inserted = '\n';
            if (document.queryCommandSupported && document.queryCommandSupported('insertText')) {
                document.execCommand('insertText', false, inserted);
            } else if (navigator.clipboard === undefined) {
                // fallback
                document.execCommand('insertHTML', false, '\n');
            } else {
                // insert using range
                const sel = window.getSelection();
                if (!sel.rangeCount) return;
                const range = sel.getRangeAt(0);
                range.deleteContents();
                const textNode = document.createTextNode(inserted);
                range.insertNode(textNode);
                // move caret after inserted node
                range.setStartAfter(textNode);
                range.collapse(true);
                sel.removeAllRanges();
                sel.addRange(range);
            }
            e.preventDefault();
            requestAnimationFrame(updateLineNumbers);
        }
    });

    // Keep line numbers in sync with visible editor scroll
    codeEditor.addEventListener('scroll', () => {
        lineNumbers.scrollTop = codeEditor.scrollTop;
        gutter.scrollTop = codeEditor.scrollTop;
    });

    // Update when typing/pasting/mutations occur
    codeEditor.addEventListener('input', () => requestAnimationFrame(updateLineNumbers));
    codeEditor.addEventListener('keyup', (e) => {
        if (['Enter', 'Backspace', 'Delete'].includes(e.key)) requestAnimationFrame(updateLineNumbers);
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
    codeEditor.textContent = "; Sample program\nLDK 10\nSTA $01\nADD $01\nOUT $00\nHLT";
    updateRegistersDisplay();
    updateLineNumbers();
});