const __globalSidebarElements = new Map();

function toggleSidebarSection(containerId, arrowId) {
    const class_collapsed = "collapsed";
    const class_arrow_rotation = "rotated";

    let container, arrow;

    if (__globalSidebarElements.has(containerId))
        container = __globalSidebarElements.get(containerId)
    else {
        container = document.getElementById(containerId);
        if (!container) return;
        __globalSidebarElements.set(containerId, container);
    }

    if (__globalSidebarElements.has(arrowId))
        arrow = __globalSidebarElements.get(arrowId)
    else {
        arrow = document.getElementById(arrowId);
        if (!arrow) return;
        __globalSidebarElements.set(arrowId, arrow);
    }

    if (container.classList.contains(class_collapsed))
        container.style.maxHeight = container.scrollHeight + "px";
    else
        container.style.maxHeight = "0px";
    container.classList.toggle(class_collapsed);

    arrow.classList.toggle(class_arrow_rotation);
}

const ioInput = document.getElementById('io-input');

ioInput.addEventListener('input', (e) => {
    let value = e.target.value;

    value = value.replace(/[^0-9-]/g, '');
    if (value.includes('-')) {
        value = '-' + value.replace(/-/g, '');
    }

    e.target.value = value;
});