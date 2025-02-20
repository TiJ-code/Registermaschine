// Sample logic to "run" the code and update registers and console
document.getElementById('run-code').addEventListener('click', function() {
    window.adder.print();
});

// Function to update the registers table dynamically
function updateRegisters(registers) {
    const tableBody = document.getElementById("registers-table").getElementsByTagName("tbody")[0];
    
    // Clear existing table rows
    tableBody.innerHTML = '';

    // Add rows for each register
    registers.forEach(register => {
        const row = document.createElement("tr");

        const registerCell = document.createElement("td");
        registerCell.textContent = register.name;
        row.appendChild(registerCell);

        const valueCell = document.createElement("td");
        valueCell.textContent = register.value;
        row.appendChild(valueCell);

        tableBody.appendChild(row);
    });
}
