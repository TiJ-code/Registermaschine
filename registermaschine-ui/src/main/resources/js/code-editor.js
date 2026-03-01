const KEYCODE_TAB = 9;
const KEYCODE_ENTER = 13;

class CodeEditor {
    constructor(inputElement, highlightElement, keywords) {
        this.container = document.getElementById('editor-stack');
        this.input = inputElement;
        this.highlightArea = highlightElement;
        this.lineNumberContainer = document.getElementById('line-numbers');

        this.lastSavedContent = "";
        this.isDirty = false;
        this.onDirtyChange = null;

        this.updateKeywords(keywords);

        // Sync scrolling
        this.input.addEventListener("scroll", () => {
            this.highlightArea.scrollTop = this.input.scrollTop;
            this.highlightArea.scrollLeft = this.input.scrollLeft;
            this.lineNumberContainer.scrollTop = this.input.scrollTop;
        });

        // Handle typing
        const renderingFunc = () => {
            this.render();
            this.checkDirtyStatus();
        };
        this.input.addEventListener("input", renderingFunc);
        this.input.addEventListener("keyup", renderingFunc);

        // Handle Tab & Enter
        this.input.addEventListener("keydown", (e) => {
            if (e.keyCode === KEYCODE_TAB) {
                e.preventDefault();
                const start = this.input.selectionStart;
                const end = this.input.selectionEnd;

                // Set textarea value to: text before caret + tab + text after caret
                this.input.value = this.input.value.substring(0, start) +
                    "    " + this.input.value.substring(end);

                // Put caret at right position again
                this.input.selectionStart = this.input.selectionEnd = start + 4;
                renderingFunc();
            }

            if (e.keyCode === KEYCODE_ENTER) {
                e.preventDefault();
                this.insertTextAtCaret("\n");
                this.ensureCaretVisible();
                renderingFunc();
            }
        });

        renderingFunc();
    }

    updateKeywords(keywordArray) {
        this.tokenRules = [
            {cls: "token-comment",  re: /;.*$/gm},
            {cls: "token-label",    re: /\b[A-Za-z_][A-Za-z0-9_]*:/g},
            {cls: "token-address",  re: /@0x[A-Fa-f0-9]+\b/g},
            {cls: "token-mnemonic", re: new RegExp("\\b(" + keywordArray.join("|") + ")\\b", "gi")},
            {cls: "token-register", re: /\b[Rr][0-9]+\b/g},
            {cls: "token-number",   re: /#\d+\b/g}
        ];
    }

    render() {
        let text = this.input.value;

        // Update Line Numbers
        const lines = text.split('\n').length;
        let numbersHtml = "";
        for (let i = 1; i <= lines; i++) numbersHtml += `<div>${i}</div>`;
        this.lineNumberContainer.innerHTML = numbersHtml;

        // Syntax Highlighting
        let html = this.escapeHtml(text);
        for (const {cls, re} of this.tokenRules) {
            html = html.replace(re, m => `<span class="${cls}">${m}</span>`);
        }

        // Add a trailing space fix for trailing newlines
        this.highlightArea.innerHTML = html + (text.endsWith('\n') ? "\n " : "");
    }

    escapeHtml(text) {
        return text.replace(/[&<>"']/g, t => ({
            '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'
        }[t]));
    }

    insertTextAtCaret(text) {
        const start = this.input.selectionStart;
        const end = this.input.selectionEnd;
        const originalValue = this.input.value;

        this.input.value = originalValue.substring(0, start) + text + originalValue.substring(end);
        this.input.selectionStart = this.input.selectionEnd = start + text.length;

        this.render();
    }

    setEditable(editable) {
        if (this.input.readOnly !== !editable) {
            this.input.readOnly = !editable;

            if (!editable) {
                this.container.classList.add('editor-locked');
                this.lineNumberContainer.classList.add('editor-locked');
            } else {
                this.container.classList.remove('editor-locked');
                this.lineNumberContainer.classList.remove('editor-locked');
            }
        }
    }

    ensureCaretVisible() {
        const textarea = this.input;
        const lineHeight = parseFloat(getComputedStyle(textarea).lineHeight);

        const textBeforeCaret = textarea.value.substring(0, textarea.selectionStart);
        const caretLine = textBeforeCaret.split('\n').length - 1;

        const caretTop = caretLine * lineHeight;
        const caretBottom = caretTop + lineHeight;

        const scrollTop = textarea.scrollTop;
        const scrollBottom = scrollTop + textarea.clientHeight;

        if (caretTop < scrollTop + lineHeight) {
            textarea.scrollTop = caretTop - lineHeight;
        }
        else if (caretBottom > scrollBottom - lineHeight) {
            textarea.scrollTop = caretBottom - textarea.clientHeight + lineHeight;
        }

        this.highlightArea.scrollTop = textarea.scrollTop;
        this.lineNumberContainer.scrollTop = textarea.scrollTop;
    }

    checkDirtyStatus() {
        const currentContent = this.code;
        const currentlyDirty = currentContent !== this.lastSavedContent;

        if (currentlyDirty !== this.isDirty) {
            this.isDirty = currentlyDirty;
            if (this.onDirtyChange) {
                this.onDirtyChange(this.isDirty);
            }
        }
    }

    markClean() {
        this.lastSavedContent = this.code;
        this.isDirty = false;
        if (this.onDirtyChange) {
            this.onDirtyChange(false);
        }
    }

    set onDirtyCallback(callback) {
        this.onDirtyChange = callback;
    }

    set code(newCode) {
        this.input.value = newCode;
        this.render();
    }

    get code() {
        return this.input.value || "";
    }
}

const editor = new CodeEditor(
    document.getElementById('editor-input'),
    document.getElementById('editor-highlight'),
    ["INP","OUT","ADD","SUB","MOV","JMP","JEZ","JNZ","HLT"]
);