document.getElementById('calculateBtn').addEventListener('click', calculateBankers);

let numProcesses = 0;
let numResources = 0;
let allocation = [];
let max = [];
let available = [];
let need = [];
let outputLog = [];

function log(message) {
    outputLog.push(message);
    document.getElementById('outputContent').textContent = outputLog.join('\n');
    
    // Auto-scroll to bottom
    const terminal = document.getElementById('terminal');
    terminal.scrollTop = terminal.scrollHeight;
}

function parseMatrix(text, rows, cols, name) {
    const lines = text.trim().split('\n').filter(l => l.trim().length > 0);
    if (lines.length !== rows) {
        throw new Error(`${name} should have ${rows} rows (found ${lines.length}).`);
    }
    const matrix = [];
    for (let i = 0; i < rows; i++) {
        const rowNums = lines[i].trim().split(/\s+/).map(Number);
        if (rowNums.length !== cols || rowNums.some(isNaN)) {
            throw new Error(`Row ${i + 1} of ${name} should have ${cols} valid numbers.`);
        }
        matrix.push(rowNums);
    }
    return matrix;
}

function parseArray(text, cols, name) {
    const nums = text.trim().split(/\s+/).map(Number);
    if (nums.length !== cols || nums.some(isNaN)) {
        throw new Error(`${name} should have ${cols} valid numbers.`);
    }
    return nums;
}

function calculateNeed() {
    log("=== Need Matrix ===");
    for (let i = 0; i < numProcesses; i++) {
        let rowNeed = [];
        for (let j = 0; j < numResources; j++) {
            rowNeed.push(max[i][j] - allocation[i][j]);
        }
        need.push(rowNeed);
        log(`P${i}: ` + rowNeed.join("\t"));
    }
    log("===================\n");
}

function isSafe(process, work) {
    for (let i = 0; i < numResources; i++) {
        if (need[process][i] > work[i]) {
            return false;
        }
    }
    return true;
}

function getIndent(depth) {
    return "  ".repeat(depth);
}

function exploreStateSpaceTree(work, finish, currentSequence, depth) {
    if (currentSequence.length === numProcesses) {
        log(getIndent(depth) + "✅ Found Safe Sequence: " + currentSequence.join(" -> "));
        return;
    }

    let foundPath = false;
    for (let i = 0; i < numProcesses; i++) {
        if (!finish[i] && isSafe(i, work)) {
            log(getIndent(depth) + "-> Try Process P" + i);

            // Simulate allocation
            let newWork = [...work];
            for (let j = 0; j < numResources; j++) {
                newWork[j] += allocation[i][j];
            }
            
            finish[i] = true;
            currentSequence.push(`P${i}`);

            // Recurse deeper
            exploreStateSpaceTree(newWork, finish, currentSequence, depth + 1);

            // Backtrack
            finish[i] = false;
            currentSequence.pop();
            
            log(getIndent(depth) + "<- Backtrack from P" + i);
            foundPath = true;
        }
    }
    
    if (!foundPath && currentSequence.length > 0 && currentSequence.length < numProcesses) {
        log(getIndent(depth) + "❌ Dead end reached (Unsafe state from here).");
    }
}

function calculateBankers() {
    outputLog = [];
    
    // Add simple button click effect
    const btn = document.getElementById('calculateBtn');
    const originalText = btn.textContent;
    btn.textContent = "Calculating...";
    
    setTimeout(() => {
        try {
            log("> Initializing Bankers Algorithm Calculation...\n");
            
            // Auto-detect dimensions from Allocation Matrix
            const allocText = document.getElementById('allocation').value.trim();
            const allocLines = allocText.split('\n').filter(l => l.trim().length > 0);
            if (allocLines.length === 0) {
                throw new Error("Allocation Matrix is empty.");
            }
            
            numProcesses = allocLines.length;
            numResources = allocLines[0].trim().split(/\s+/).length;
            
            if (numResources === 0 || isNaN(parseInt(allocLines[0].trim().split(/\s+/)[0]))) {
                throw new Error("Allocation Matrix has invalid formatting.");
            }
            
            log(`> Auto-detected ${numProcesses} Processes and ${numResources} Resources.\n`);

            allocation = parseMatrix(document.getElementById('allocation').value, numProcesses, numResources, "Allocation Matrix");
            max = parseMatrix(document.getElementById('max').value, numProcesses, numResources, "Max Matrix");
            available = parseArray(document.getElementById('available').value, numResources, "Available Resources");
            need = [];

            calculateNeed();
            
            log("> Starting State Space Tree Traversal for Safe Sequences:");
            let finish = Array(numProcesses).fill(false);
            let work = [...available];
            let currentSequence = [];
            
            exploreStateSpaceTree(work, finish, currentSequence, 0);
            
            log("\n> Execution completed.");
            
        } catch (error) {
            log("\n❌ ERROR: " + error.message);
            log("\nPlease check your inputs and make sure they match the dimensions specified.");
        }
        
        btn.textContent = originalText;
    }, 100); // Small timeout to allow UI update
}
