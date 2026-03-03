const class_sidebar_collapsed = "collapsed";
const class_sidebar_arrow_rotation = "rotated";

function toggleSidebarSection(containerId, arrowId) {
    const container = document.getElementById(containerId);
    const arrow = document.getElementById(arrowId);

    if (container.classList.contains(class_sidebar_collapsed))
        container.style.maxHeight = container.scrollHeight + "px";
    else
        container.style.maxHeight = "0px";

    container.classList.toggle(class_sidebar_collapsed);
    arrow.classList.toggle(class_sidebar_arrow_rotation);
}

function setSidebarSection(containerId, arrowId, visible) {
    const container = document.getElementById(containerId);
    const arrow = document.getElementById(arrowId);

    if (visible) {
        container.classList.toggle(class_sidebar_collapsed, false);
        arrow.classList.toggle(class_sidebar_arrow_rotation, false);
    } else {
        container.classList.toggle(class_sidebar_collapsed, true);
        arrow.classList.toggle(class_sidebar_arrow_rotation, true);
    }
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