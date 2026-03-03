const FileActions = Object.freeze({
    NEW: 'new',
    SAVE: 'save',
    SAVE_AS: 'saveAs',
    LOAD: 'load'
})

function fileAction(fileAction) {
    const currentCode = editor.input.value;
    switch (fileAction) {
        case FileActions.NEW:
            window.java?.newDocument(currentCode);
            break;
        case FileActions.SAVE:
            window.java?.saveFile(currentCode);
            break;
        case FileActions.SAVE_AS:
            window.java?.saveAsFile(currentCode);
            break;
        case FileActions.LOAD:
            window.java?.loadFile();
            break;
        default:
            window.java?.println("Error: Unknown File Action \"" + fileAction + "\"");
            break;
    }
}

function updateFilename(isDirty) {
    const indicator = isDirty ? "*" : "";
    filenameDisplay.innerText = `${globalCurrentFileName}${indicator}`;
}
editor.onDirtyCallback = updateFilename;