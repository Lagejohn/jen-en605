async function restartGame() {
    const responseDiv = document.getElementById("response");


    // Send a request to the backend to restart the game
    const response = await fetch('/api/game/restart', {
        method: 'POST'
    });

    // Process the response
    const data = await response.json();
    console.log(data);
    responseDiv.textContent = data.displayMessage; // Set initial instructions

    fetchPositions();

    // Show the player selection container
    document.getElementById('player-selection').style.display = 'block';
    document.getElementById('confirm-selection').style.display = 'block';
    document.getElementById('finish-selection').style.display = 'block';
    selectPlayers(JSON.parse(data.output));

}


async function fetchPositions() {
    try {

        const response = await fetch('/api/game/getBoardPositions', {
                method: 'GET'
            });
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const positions = await response.json();
        placeCharacters(JSON.parse(positions.output));
    } catch (error) {
        console.error("Failed to fetch positions:", error);
    }
}

function clearBoard() {
    removeCharacterFromCell(0, 4);
    removeCharacterFromCell(1, 1);
    removeCharacterFromCell(1, 2);
    removeCharacterFromCell(1, 3);
    removeCharacterFromCell(1, 4);
    removeCharacterFromCell(1, 5);
    removeCharacterFromCell(2, 0);
    removeCharacterFromCell(2, 1);
    removeCharacterFromCell(2, 3);
    removeCharacterFromCell(2, 5);
    removeCharacterFromCell(2, 6);
    removeCharacterFromCell(3, 1);
    removeCharacterFromCell(3, 2);
    removeCharacterFromCell(3, 3);
    removeCharacterFromCell(3, 4);
    removeCharacterFromCell(3, 5);
    removeCharacterFromCell(4, 0);
    removeCharacterFromCell(4, 1);
    removeCharacterFromCell(4, 3);
    removeCharacterFromCell(4, 5);
    removeCharacterFromCell(5, 1);
    removeCharacterFromCell(5, 2);
    removeCharacterFromCell(5, 3);
    removeCharacterFromCell(5, 4);
    removeCharacterFromCell(5, 5);
    removeCharacterFromCell(6, 2);
    removeCharacterFromCell(6, 4);

}

function removeCharacterFromCell(row, column) {
    // Select the cell
    const cell = document.querySelector(`.space[data-row="${row}"][data-col="${column}"]`);

    // Find the image inside the cell
    const characterImage = cell.querySelector('img'); // Adjust selector if needed

    // Remove the image if it exists
    if (characterImage) {
        cell.removeChild(characterImage);
    }
}

function placeCharacters(positions) {
    const board = document.querySelector('.board');

    clearBoard();

    Object.keys(positions).forEach(character => {

        const { row, column, imgPath } = positions[character];

        // Find the corresponding cell by row and column
        const space = document.querySelector(`.space[data-row="${row}"][data-col="${column}"]`);
        if (space) {

            // Add the character image to the cell
            const img = document.createElement('img');
            img.src = imgPath;
            space.appendChild(img);
        }

    });
}

function selectPlayers(players) {
    const container = document.getElementById('player-selection');
    container.innerHTML = '';
    players.forEach(player => {
            const playerButton = document.createElement('button');
            playerButton.className = 'player-option';
            playerButton.dataset.name = player;

            // Set the button text as the player's name
            playerButton.textContent = player;


            // Add event listener for selection
            playerButton.addEventListener('click', () => {
                // Remove selection from any previously selected player
                document.querySelectorAll('.player-option').forEach(option => {
                    option.classList.remove('selected');
                });
                // Mark this button as selected
                playerButton.classList.add('selected');
                // Enable confirm button
                document.getElementById('confirm-selection').disabled = false;
            });

            // Append the player button to the selection container
            container.appendChild(playerButton);
        });
}

// Confirm selection
document.getElementById('confirm-selection').addEventListener('click', () => {
    const selectedPlayer = document.querySelector('.player-option.selected');
    if (selectedPlayer) {
        const playerName = selectedPlayer.dataset.name;
        console.log(`Player selected: ${playerName}`);
        selectedPlayer.disabled = true;

        // Send command to backend API
        fetch('/api/game/command', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ command: playerName })
        })
        .then(response => response.json())
        .then(data => {
            console.log('Player selection response:', data);
            alert(`You have selected: ${playerName}`);

        })
        .catch(error => {
            console.error('Error selecting player:', error);
            alert('An error occurred while selecting your player. Please try again.');
        });

        document.getElementById('confirm-selection').disabled = true;
    }
});

async function finishSelection() {
    const response = await fetch('/api/game/command', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ command: "CONTINUE" })
        });
    playGame(response);
}

async function playGame(response) {
    fetchPositions(); // gather positions each round


    // hide player selection
    document.getElementById('player-selection').style.display = 'none';
    document.getElementById('confirm-selection').style.display = 'none';
    document.getElementById('finish-selection').style.display = 'none';
    document.getElementById('make-accusation').style.display = 'block';




    // process response
    const data = await response.json();
    console.log(data);

    const responseDiv = document.getElementById("response");
    responseDiv.textContent = data.displayMessage;

    userSelect(JSON.parse(data.output));


}

function userSelect(options) {

    // clear existing
    document.getElementById('player-selection').innerHTML = '';
    document.getElementById('choose').disabled = false;

    console.log(options);
    // display selection buttons
    document.getElementById('player-selection').style.display = 'block';
    document.getElementById('choose').style.display = 'block';

    const container = document.getElementById('player-selection');
    container.innerHTML = '';

    options.forEach(userOption => {
        const optionButton = document.createElement('button');
        optionButton.className = 'player-option';
        optionButton.dataset.name = userOption;

        // Set the button text as the player's name
        optionButton.textContent = userOption;


        // Add event listener for selection
        optionButton.addEventListener('click', () => {
            // Remove selection from any previously selected player
            document.querySelectorAll('.player-option').forEach(option => {
                option.classList.remove('selected');
            });
            // Mark this button as selected
            optionButton.classList.add('selected');
            // Enable confirm button
            document.getElementById('confirm-selection').disabled = false;
        });

        // Append the player button to the selection container
        container.appendChild(optionButton);
    });
}

// Choose button
async function choose() {

    const choice = document.querySelector('.player-option.selected');
    if (choice) {
        const choiceTitle = choice.dataset.name;
        console.log(`Selected choice: ${choiceTitle}`);
        choice.disabled = true;

        // Send command to backend API
        const response = await fetch('/api/game/command', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ command: choiceTitle })
        });
        playGame(response);
    }
}

async function makeAccusation() {

    const response = await fetch('/api/game/command', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ command: "MAKE_ACCUSATION" })
    });
    playGame(response);

}

