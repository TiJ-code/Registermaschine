const KEYCODE_TAB = 9;
const KEYCODE_ENTER = 13;
const KEYCODE_BACKSPACE = 8;

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
        this.input.addEventListener("input", () => {
            this.render();
            this.checkDirtyStatus();
        });

        // Handle Tab & Enter
        this.input.addEventListener("keydown", (e) => {
            if (e.ctrlKey && !e.shiftKey && e.keyCode === KEYCODE_BACKSPACE) {
                e.preventDefault();
                this.handleCtrlBackspace();
                return;
            }

            if (e.keyCode === KEYCODE_TAB) {
                e.preventDefault();
                this.insertTextAtCaret("    ");
                return;
            }

            if (e.keyCode === KEYCODE_ENTER) {
                e.preventDefault();
                this.insertTextAtCaret("\n");
                this.ensureCaretVisible();
            }
        });

        this.render();
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

        const linesCount = text.split('\n').length;

        // Update Line Numbers
        if (this.currentLineCount !== linesCount) {
            this.lineNumberContainer.innerHTML = '<div></div>'.repeat(linesCount);
            this.currentLineCount = linesCount;
        }

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
        this.checkDirtyStatus();
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

    handleCtrlBackspace() {
        const start = this.input.selectionStart;
        const end = this.input.selectionEnd;
        const text = this.code;

        if (start !== end) {
            this.input.value = text.substring(0, start) + text.substring(end);
            this.input.selectionStart = this.input.selectionEnd = start;
            this.render();
            return;
        }

        const beforeCaret = text.substring(0, start);
        const lastNewLine = beforeCaret.lastIndexOf('\n');
        const lineStart = lastNewLine + 1;
        const textInLineBeforeCaret = text.substring(lineStart, start);

        const trimmedLineFragment = textInLineBeforeCaret.trimEnd();
        const lastSpaceInLine = trimmedLineFragment.lastIndexOf(' ');

        let deleteTo;

        if (textInLineBeforeCaret.trim() === "") {
            deleteTo = lineStart;
        } else if (lastSpaceInLine === -1) {
            deleteTo = lineStart;
        } else {
            deleteTo = lineStart + lastSpaceInLine;
        }

        this.input.value = text.substring(0, deleteTo) + text.substring(start);
        this.input.selectionStart = this.input.selectionEnd = deleteTo;

        this.render();
    }
}

const editor = new CodeEditor(
    document.getElementById('editor-input'),
    document.getElementById('editor-highlight'),
    ["INP","OUT","ADD","SUB","MOV","JMP","JEZ","JNZ","HLT"]
);