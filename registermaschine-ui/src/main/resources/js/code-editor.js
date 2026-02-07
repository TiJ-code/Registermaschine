class CodeEditor {
    constructor(element, keywords) {
        this.element = element;
        this.element.addEventListener("input", () => this.highlight());
        this.updateKeywords(keywords)
        this.highlight();
    }

    updateKeywords(keywordArray) {
        this.keywords = keywordArray;
        this.tokenRules = [
            {cls: "token-comment",  re: /;.*$/gm},
            {cls: "token-label",    re: /\b[A-Za-z_][A-Za-z0-9_]*:/g},
            {cls: "token-address",  re: /@0x[A-Fa-f0-9]+\b/g}, // Matches @0x format
            {cls: "token-mnemonic", re: new RegExp("\\b(" + keywordArray.join("|") + ")\\b", "gi")},
            {cls: "token-register", re: /\b[Rr][0-9]+\b/g},
            {cls: "token-number",   re: /#\d+\b/g} // Strict Java convention: Must start with #
        ];
    }

    highlight() {
        const el = this.element;

        let text = el.innerText;
        let html = this.escapeHtml(text);
        for (const {cls, re} of this.tokenRules) {
            html = html.replace(re, m => `<span class="${cls}">${m}</span>`);
        }
        el.innerHTML = html.replace(/\n/g, "<br>");
        this.placeCaretAtEnd(el);
    }

    escapeHtml(text) {
        return text.replace(/[&<>"']/g, t => ({
            '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'
        }[t]));
    }

    placeCaretAtEnd(el) {
        const range = document.createRange();
        range.selectNodeContents(el);
        range.collapse(false);
        const sel = window.getSelection();
        sel.removeAllRanges();
        sel.addRange(range);
    }
}

const editor = new CodeEditor(document.getElementById('editor'), ["INP","OUT","ADD","SUB","MOV","JMP","JEZ","JNZ","HLT"])