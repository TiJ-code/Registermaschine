// Initialize registers and state
document.addEventListener('DOMContentLoaded', function() {
    // DOM Elements
    const codeEditor = document.getElementById('codeEditor');
    const runBtn = document.getElementById('runBtn');
    const debugBtn = document.getElementById('debugBtn');
    const stopBtn = document.getElementById('stopBtn');
    
    // State
    let breakpoints = new Set();
    let isRunning = false;
    let isDebugging = false;
    let currentLine = -1;
    
    // Initialize registers with 8 registers (0-7) and accumulator
    const registers = {
        acc: 0,
        r: new Array(8).fill(0)
    };

    let executionStartTime = 0;
    
    // Format number as 4-digit hex with 0x prefix
    function toHex(value) {
        return '0x' + Math.abs(value).toString(16).toUpperCase().padStart(4, '0');
    }
    
    // Format number as 16-bit binary with spaces every 4 bits
    function toBinary(value) {
        return (value >>> 0).toString(2).padStart(16, '0')
            .replace(/(\d{4})(?=\d)/g, '$1 ');
    }
    
    // Update register display
    function updateRegistersDisplay() {
        // Update accumulator
        const accValue = document.querySelector('#accumulator .register-value');
        const accBinary = document.querySelector('#accumulator .register-binary');
        if (accValue) accValue.textContent = toHex(registers.acc);
        if (accBinary) accBinary.textContent = toBinary(registers.acc);
        
        // Update registers 0-7
        for (let i = 0; i < 8; i++) {
            const regValue = document.getElementById(`reg${i.toString().padStart(2, '0')}`);
            const regBinary = document.getElementById(`reg${i.toString().padStart(2, '0')}Bin`);
            
            if (regValue) regValue.textContent = toHex(registers.r[i]);
            if (regBinary) regBinary.textContent = toBinary(registers.r[i]);
        }
    }
    
    // Initialize line numbers in hex (3 digits max, 0x000-0xFFF)
    function updateLineNumbers() {
        const lineNumbers = document.querySelector('.line-numbers');
        const gutter = document.querySelector('.gutter');
        if (!lineNumbers || !gutter) return;
        
        const code = codeEditor ? codeEditor.textContent || '' : '';
        const lines = code.split('\n');
        
        // Store current scroll position
        const lineNumbersScroll = lineNumbers.scrollTop;
        
        let lineNumbersHTML = '';
        let gutterHTML = '';
        
        // Limit to 4096 lines (0x000-0xFFF)
        const maxLines = Math.max(1, Math.min(lines.length, 0x1000));
        
        for (let i = 0; i < maxLines; i++) {
            const hexNum = i.toString(16).toUpperCase().padStart(3, '0');
            const hasBreakpoint = breakpoints.has(i);
            
            lineNumbersHTML += `<div class="line-number" data-line="${i}">${hexNum}</div>`;
            gutterHTML += `<div class="breakpoint${hasBreakpoint ? ' active' : ''}" data-line="${i}"></div>`;
        }
        
        // Update line numbers and gutter
        lineNumbers.innerHTML = lineNumbersHTML;
        gutter.innerHTML = gutterHTML;
        
        // Restore scroll position
        lineNumbers.scrollTop = lineNumbersScroll;
        
        // Update cursor position
        updateCursorPosition();
        
        lineNumbers.innerHTML = lineNumbersHTML;
        gutter.innerHTML = gutterHTML;
        
        // Add click handlers to line numbers and breakpoints
        document.querySelectorAll('.line-number').forEach(el => {
            el.addEventListener('click', (e) => {
                const line = parseInt(e.target.getAttribute('data-line'));
                toggleBreakpoint(line);
            });
        });
        
        document.querySelectorAll('.breakpoint').forEach(el => {
            el.addEventListener('click', (e) => {
                e.stopPropagation();
                const line = parseInt(e.target.getAttribute('data-line'));
                toggleBreakpoint(line);
            });
        });
    }

    // Update register display
    function updateRegister(reg, value) {
        const row = document.querySelector(`tr[data-register="${reg}"]`);
        if (row) {
            row.classList.add('updated');
            row.cells[1].textContent = value;
            row.cells[2].textContent = '0x' + value.toString(16).toUpperCase().padStart(4, '0');
            row.cells[3].textContent = value.toString(2).padStart(16, '0');
            
            setTimeout(() => row.classList.remove('updated'), 500);
        }
    }

    // Initialize registers table
    function initRegisters() {
        if (!registersBody) return;
        
        registersBody.innerHTML = '';
        for (let i = 0; i < 16; i++) {
            const reg = `$${i.toString(16).toUpperCase().padStart(2, '0')}`;
            const row = document.createElement('tr');
            row.setAttribute('data-register', reg);
            row.innerHTML = `
                <td>${reg}</td>
                <td>0</td>
                <td>0x0000</td>
                <td>0000000000000000</td>
            `;
            registersBody.appendChild(row);
        }
    }

    // Update cursor position
    function updateCursorPosition() {
        if (!codeEditor) return;
        
        const selection = window.getSelection();
        if (!selection.rangeCount) return;
        
        const range = selection.getRangeAt(0);
        const preCaretRange = range.cloneRange();
        preCaretRange.selectNodeContents(codeEditor);
        preCaretRange.setEnd(range.startContainer, range.startOffset);
        const text = preCaretRange.toString();
        const line = (text.match(/\n/g) || []).length + 1;
        const col = text.length - (text.lastIndexOf('\n') + 1);
        
        // Highlight current line
        const lineNumberElements = document.querySelectorAll('.line-number');
        lineNumberElements.forEach((el, i) => {
            el.classList.toggle('active', i + 1 === line);
        });
        
        // Update status bar with HEX values
        if (currentLineEl) currentLineEl.textContent = (line - 1).toString(16).toUpperCase().padStart(4, '0');
        if (currentColEl) currentColEl.textContent = col.toString(16).toUpperCase().padStart(2, '0');
    }
    
    // Sync scrolling between editor and line numbers
    function syncScroll() {
        const lineNumbers = document.querySelector('.line-numbers');
        if (lineNumbers) {
            lineNumbers.scrollTop = codeEditor.scrollTop;
        }
        const gutter = document.querySelector('.gutter');
        if (gutter) {
            gutter.scrollTop = codeEditor.scrollTop;
        }
    }

    // Set status message
    function setStatus(message, type = 'info') {
        if (!statusMessageEl) return;
        statusMessageEl.textContent = message;
        statusMessageEl.className = '';
        statusMessageEl.classList.add(type);
    }

    // Toggle instructions panel
    function toggleInstructionsPanel() {
        if (!instructionsPanel) return;
        
        isInstructionsCollapsed = !isInstructionsCollapsed;
        instructionsPanel.classList.toggle('collapsed', isInstructionsCollapsed);
        
        const icon = toggleInstructionsBtn?.querySelector('i');
        if (icon) {
            icon.className = isInstructionsCollapsed ? 'fas fa-chevron-left' : 'fas fa-chevron-right';
        }
        
        // Save the state to localStorage
        localStorage.setItem('instructionsCollapsed', isInstructionsCollapsed);
    }

    // Run code
    function runCode() {
        if (isRunning) return;
        isRunning = true;
        stopBtn.disabled = false;
        runBtn.disabled = true;
        debugBtn.disabled = true;
        
        executionStartTime = performance.now();
        setStatus('Running...', 'info');
        
        // Simulate code execution (replace with actual execution logic)
        setTimeout(() => {
            // Simulate some register updates
            for (let i = 0; i < 10; i++) {
                setTimeout(() => {
                    const regIndex = Math.floor(Math.random() * 16);
                    const value = Math.floor(Math.random() * 65536);
                    updateRegister(`$${regIndex.toString(16).toUpperCase().padStart(2, '0')}`, value);
                }, i * 100);
            }
            
            const executionTime = performance.now() - executionStartTime;
            executionTimeEl.textContent = `${Math.round(executionTime)}ms`;
            
            isRunning = false;
            stopBtn.disabled = true;
            runBtn.disabled = false;
            debugBtn.disabled = false;
            
            setStatus('Execution completed', 'success');
        }, 1500);
    }

    // Debug code
    function debugCode() {
        if (isRunning) return;
        
        isRunning = true;
        stopBtn.disabled = false;
        runBtn.disabled = true;
        debugBtn.disabled = true;
        
        executionStartTime = performance.now();
        setStatus('Debugging...', 'info');
        
        // Simulate debugging (replace with actual debugging logic)
        let step = 0;
        const maxSteps = 10;
        
        const debugStep = () => {
            if (step >= maxSteps || !isRunning) {
                const executionTime = performance.now() - executionStartTime;
                executionTimeEl.textContent = `${Math.round(executionTime)}ms`;
                
                isRunning = false;
                stopBtn.disabled = true;
                runBtn.disabled = false;
                debugBtn.disabled = false;
                
                setStatus('Debugging completed', 'success');
                return;
            }
            
            // Simulate stepping through code
            const regIndex = Math.floor(Math.random() * 16);
            const value = Math.floor(Math.random() * 65536);
            updateRegister(`$${regIndex.toString(16).toUpperCase().padStart(2, '0')}`, value);
            
            step++;
            setTimeout(debugStep, 500);
        };
        
        debugStep();
    }

    // Stop execution
    function stopExecution() {
        isRunning = false;
        stopBtn.disabled = true;
        runBtn.disabled = false;
        debugBtn.disabled = false;
        
        const executionTime = performance.now() - executionStartTime;
        executionTimeEl.textContent = `${Math.round(executionTime)}ms`;
        
        setStatus('Execution stopped', 'warning');
    }

    // Initialize
    updateLineNumbers();
    updateRegistersDisplay();
    setupEventListeners();
    
    // Add input event listener to update line numbers in real-time
    if (codeEditor) {
        codeEditor.addEventListener('input', updateLineNumbers);
        codeEditor.addEventListener('scroll', syncScroll);
    }
    
    // Update line numbers when editor content changes
    if (codeEditor) {
        codeEditor.addEventListener('input', updateLineNumbers);
        codeEditor.addEventListener('keydown', handleKeyDown);
    }
    
    function setupEventListeners() {
        // Run button
        if (runBtn) {
            runBtn.addEventListener('click', runCode);
        }
        
        // Debug button
        if (debugBtn) {
            debugBtn.addEventListener('click', startDebugging);
        }
        
        // Stop button
        if (stopBtn) {
            stopBtn.addEventListener('click', stopExecution);
        }
        
        // Add keyboard shortcuts
        document.addEventListener('keydown', function(e) {
            if (e.key === 'F5' && !e.shiftKey && !e.ctrlKey && !e.altKey) {
                e.preventDefault();
                runCode();
            } else if (e.key === 'F6' && !e.shiftKey && !e.ctrlKey && !e.altKey) {
                e.preventDefault();
                startDebugging();
            } else if (e.key === 'F5' && e.shiftKey && !e.ctrlKey && !e.altKey) {
                e.preventDefault();
                stopExecution();
            }
        });
    }
    
    function handleKeyDown(e) {
        // Allow Tab key to insert spaces
        if (e.key === 'Tab') {
            e.preventDefault();
            const start = codeEditor.selectionStart;
            const end = codeEditor.selectionEnd;
            
            // Insert 4 spaces
            codeEditor.value = codeEditor.value.substring(0, start) + '    ' + codeEditor.value.substring(end);
            
            // Move cursor to after the inserted spaces
            codeEditor.selectionStart = codeEditor.selectionEnd = start + 4;
        }
    }
    
    function toggleBreakpoint(lineNumber) {
        if (breakpoints.has(lineNumber)) {
            breakpoints.delete(lineNumber);
        } else {
            breakpoints.add(lineNumber);
        }
        // Update the specific breakpoint display without refreshing everything
        const breakpointEl = document.querySelector(`.breakpoint[data-line="${lineNumber}"]`);
        if (breakpointEl) {
            breakpointEl.classList.toggle('active');
        }
        console.log('Breakpoints:', Array.from(breakpoints).sort((a, b) => a - b));
    }
    
    function updateBreakpointsDisplay() {
        const gutter = document.querySelector('.gutter');
        if (!gutter) return;
        
        const lineCount = codeEditor.value.split('\n').length;
        let html = '';
        
        for (let i = 0; i < lineCount; i++) {
            const hasBreakpoint = breakpoints.has(i);
            html += `<div class="breakpoint${hasBreakpoint ? ' active' : ''}" data-line="${i}"></div>`;
        }
        
        gutter.innerHTML = html;
        
        // Add click handlers to breakpoints
        document.querySelectorAll('.breakpoint').forEach(bp => {
            bp.addEventListener('click', (e) => {
                e.stopPropagation();
                const line = parseInt(e.target.getAttribute('data-line'));
                toggleBreakpoint(line);
            });
        });
    }
    
    function runCode() {
        if (isRunning) return;
        
        isRunning = true;
        isDebugging = false;
        currentLine = -1;
        
        // Update UI
        runBtn.disabled = true;
        debugBtn.disabled = true;
        stopBtn.disabled = false;
        
        // TODO: Implement actual code execution
        console.log('Running code...');
    }
    
    function startDebugging() {
        if (isRunning) return;
        
        isRunning = true;
        isDebugging = true;
        currentLine = -1;
        
        // Update UI
        runBtn.disabled = true;
        debugBtn.disabled = true;
        stopBtn.disabled = false;
        
        // TODO: Implement actual debugging
        console.log('Starting debug session...');
    }
    
    function stopExecution() {
        if (!isRunning) return;
        
        isRunning = false;
        isDebugging = false;
        currentLine = -1;
        
        // Update UI
        runBtn.disabled = false;
        debugBtn.disabled = false;
        stopBtn.disabled = true;
        
        // TODO: Stop any running execution
        console.log('Execution stopped');
    }
    
    function updateCurrentLine(line) {
        // Remove highlight from previous line
        if (currentLine >= 0) {
            const prevLine = document.querySelector(`.line-number[data-line="${currentLine}"]`);
            if (prevLine) prevLine.classList.remove('current-line');
        }
        
        currentLine = line;
        
        // Highlight current line
        if (currentLine >= 0) {
            const currentLineEl = document.querySelector(`.line-number[data-line="${currentLine}"]`);
            if (currentLineEl) {
                currentLineEl.classList.add('current-line');
                // Scroll to line if not visible
                currentLineEl.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
            }
        }
    }
    
    // Clear any saved collapsed state
    localStorage.removeItem('instructionsCollapsed');
    
    // Instructions panel is now always visible
    if (instructionsPanel) {
        instructionsPanel.classList.remove('collapsed');
    }
    
    // Initialize
    initRegisters();
    updateLineNumbers();
    updateRegistersDisplay();
    updateCursorPosition();
    
    // Add some sample code if editor exists
    if (codeEditor) {
        codeEditor.value = `; Sample Registermaschine Code
; This is a comment

; Initialize registers
LDK 10      ; Load constant 10 into accumulator
STA $01     ; Store accumulator in register $01
ADD $01     ; Add value from register $01 to accumulator
OUT $00     ; Output value from accumulator
HLT 99      ; Halt execution
`;
        // Trigger input event to update line numbers
        const event = new Event('input');
        codeEditor.dispatchEvent(event);
    }

    // Set initial status
    setStatus('Ready', 'info');
});
