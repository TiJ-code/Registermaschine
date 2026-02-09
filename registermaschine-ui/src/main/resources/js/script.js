const __globalDocsElements = new Map();

function toggleDocs() {
    const id_instruction_container = 'instruction-container';
    const id_instruction_arrow = 'docs-arrow';
    const class_instructions_visible = 'docs-visible';
    const class_arrow_rotation = 'rotated';

    let content, arrow;

    if (__globalDocsElements.has(id_instruction_container))
        content = __globalDocsElements.get(id_instruction_container);
    else {
        content = document.getElementById(id_instruction_container);
        if (!content) return;
        __globalDocsElements.set(id_instruction_container, content);
    }

    if (__globalDocsElements.has(id_instruction_arrow))
        arrow = __globalDocsElements.get(id_instruction_arrow);
    else {
        arrow = document.getElementById(id_instruction_arrow);
        if (!arrow) return;
        __globalDocsElements.set(id_instruction_arrow, arrow);
    }

    content.classList.toggle(class_instructions_visible);
    arrow.classList.toggle(class_arrow_rotation);
}

function toggleDebug(e) {
    useDebug = e.checked;
}