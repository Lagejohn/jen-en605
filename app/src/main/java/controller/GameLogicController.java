package controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import static java.util.Map.entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import obj.*;
import org.springframework.stereotype.Service;

@Service
public class GameLogicController {
    // todo: take out for GUI implementation
    Scanner scan = new Scanner( System.in );

    PlayerName winner = null;
    public GameStage gamestage;
    List<PlayerName> activePlayers;
    List<BoardSlotLabel> availableMoves;
    Player currPlayer;
    BoardSlotLabel selectedMove;
    int turnNum;

    String displayMessage = "";

    ObjectMapper objectMapper = new ObjectMapper();

    PlayerName sugSuspect;
    Weapon sugWeapon;

    PlayerName accSuspect;
    Weapon accWeapon;
    BoardSlotLabel accRoom;

    public class GridSquare {
        public int row;
        public int column;
        public String imgPath;

        public GridSquare(int row, int column) {
            this.row = row;
            this.column = column;
        }

        public GridSquare setImgPath(String imgPath) {
            this.imgPath = imgPath;
            return this;
        }
    }

    public GridSquare getGridSquareForBoardSlot(GameLogicController.BoardSlotLabel boardSlot) {
        return gridMapping.get(boardSlot);
    }

    private Map<GameLogicController.BoardSlotLabel, GridSquare> gridMapping = Map.ofEntries(
            entry(GameLogicController.BoardSlotLabel.STARTING_SQUARE_1, new GridSquare(0,4)),
            entry(GameLogicController.BoardSlotLabel.STARTING_SQUARE_2, new GridSquare(2,6)),
            entry(GameLogicController.BoardSlotLabel.STARTING_SQUARE_3, new GridSquare(6,4)),
            entry(GameLogicController.BoardSlotLabel.STARTING_SQUARE_4, new GridSquare(6,2)),
            entry(GameLogicController.BoardSlotLabel.STARTING_SQUARE_5, new GridSquare(4,0)),
            entry(GameLogicController.BoardSlotLabel.STARTING_SQUARE_6, new GridSquare(2,0)),
            entry(GameLogicController.BoardSlotLabel.HALLWAY_1, new GridSquare(1,2)),
            entry(GameLogicController.BoardSlotLabel.HALLWAY_2, new GridSquare(1,4)),
            entry(GameLogicController.BoardSlotLabel.HALLWAY_3, new GridSquare(2,1)),
            entry(GameLogicController.BoardSlotLabel.HALLWAY_4, new GridSquare(2,3)),
            entry(GameLogicController.BoardSlotLabel.HALLWAY_5, new GridSquare(2,5)),
            entry(GameLogicController.BoardSlotLabel.HALLWAY_6, new GridSquare(3,2)),
            entry(GameLogicController.BoardSlotLabel.HALLWAY_7, new GridSquare(3,4)),
            entry(GameLogicController.BoardSlotLabel.HALLWAY_8, new GridSquare(4,1)),
            entry(GameLogicController.BoardSlotLabel.HALLWAY_9, new GridSquare(4,3)),
            entry(GameLogicController.BoardSlotLabel.HALLWAY_10, new GridSquare(4,5)),
            entry(GameLogicController.BoardSlotLabel.HALLWAY_11, new GridSquare(5,2)),
            entry(GameLogicController.BoardSlotLabel.HALLWAY_12, new GridSquare(5,4)),
            entry(GameLogicController.BoardSlotLabel.STUDY, new GridSquare(1,1)),
            entry(GameLogicController.BoardSlotLabel.HALL, new GridSquare(1,3)),
            entry(GameLogicController.BoardSlotLabel.LOUNGE, new GridSquare(1,5)),
            entry(GameLogicController.BoardSlotLabel.LIBRARY, new GridSquare(3,1)),
            entry(GameLogicController.BoardSlotLabel.BILLIARD_ROOM, new GridSquare(3,3)),
            entry(GameLogicController.BoardSlotLabel.DINING_ROOM, new GridSquare(3,5)),
            entry(GameLogicController.BoardSlotLabel.CONSERVATORY, new GridSquare(5,1)),
            entry(GameLogicController.BoardSlotLabel.BALLROOM, new GridSquare(5,3)),
            entry(GameLogicController.BoardSlotLabel.KITCHEN, new GridSquare(5,5))

    );

    /**
     * Primary method for handling input commands from the frontend client
     *
     * @param command String representing command submitted
     * @return Text to be sent back to the user based on consequences of their command
     */
    public GameResponse processCommand(String command) throws Exception {
        System.out.println("[processCommand] Current gamestage: "+gamestage);
        System.out.println("[processCommand] User command: '"+command+"'");
        StringBuilder text = new StringBuilder("User: " + command + "\n");
        switch(gamestage) {

            case ADDITIONAL_PLAYER_SELECTION -> {
                if(command.equals("CONTINUE")) {
                    System.out.println("[processCommand] User declined to add additional players");
                    text.append("No more players added, starting game!\n\n");

                    // initialize inactive players
                    for (PlayerName pName : availablePlayers) {
                        gameBoard.addPlayer(new Player(pName, startingPositions.get(pName), false));
                    }
                    System.out.printf("[processCommand] All inactive players initialized.\n");

                    shuffleCardsAndDeal();
                    activePlayers = gameBoard.getActivePlayers();
                    gamestage = GameStage.GAMEPLAY;

                    setupTurn();
                    return new GameResponse(new StringBuilder(objectMapper.writeValueAsString(availableMoves)),
                            new StringBuilder(displayMessage));
                } else {
                    try {
                        text = new StringBuilder(initializePlayer(command));
                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
                        text.append(command).append(" is not a valid player index. Please try again.\n");
                        break;
                    }
                }
            }

            case FIRST_PLAYER_SELECTION -> {
                try {
                    text = new StringBuilder(initializePlayer(command));
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    text.append(command).append(" is not a valid player index. Please try again.\n");
                    break;
                }
                gamestage = GameStage.ADDITIONAL_PLAYER_SELECTION;
                break;
            }

            case GAMEPLAY -> {
                if (command.equals("MAKE_ACCUSATION")) {
                    //winner = handleAccusation(currPlayer.getName());
                    gamestage = GameStage.ACCUSATION_PARSE_SUSPECT;
                    displayMessage = currPlayer.getName() + " is making a formal accusation! Let's begin by picking a suspect:";
                    return new GameResponse(new StringBuilder(objectMapper.writeValueAsString(new ArrayList<>(Arrays.asList(PlayerName.values())))),
                            new StringBuilder(displayMessage));
                }
                if (availableMoves.isEmpty()) {
                    System.out.printf("[processCommand] player %s declined to accuse and has no available moves, skipping...",currPlayer.getName());
                    turnNum++;
                    setupTurn();
                    return new GameResponse(new StringBuilder(objectMapper.writeValueAsString(availableMoves)),
                            new StringBuilder(displayMessage));
                }
                selectedMove = availableMoves.remove(availableMoves.indexOf(BoardSlotLabel.valueOf(command)));
                boolean isInRoom = handlePlayerMove(currPlayer.getName(),selectedMove);
                if(isInRoom) {
                    // Must make a suggestion
                    displayMessage = "Make a suggestion! Your suggestion room is " + selectedMove + ".\nChoose a suspect: ";
                    gamestage = GameStage.SUGGESTION_PARSE_SUSPECT;

                    return new GameResponse(new StringBuilder(objectMapper.writeValueAsString(new ArrayList<>(Arrays.asList(PlayerName.values())))),
                            new StringBuilder(displayMessage));
                } else {
                    turnNum ++;
                    setupTurn();
                    return new GameResponse(new StringBuilder(objectMapper.writeValueAsString(availableMoves)),
                            new StringBuilder(displayMessage));
                }

            }

            case SUGGESTION_PARSE_SUSPECT -> {
                sugSuspect = PlayerName.valueOf(command);
                displayMessage = "Next, select your weapon: ";
                gamestage = GameStage.SUGGESTION_PARSE_WEAPON;

                return new GameResponse(new StringBuilder(objectMapper.writeValueAsString(new ArrayList<>(Arrays.asList(Weapon.values())))),
                        new StringBuilder(displayMessage));
            }

            case SUGGESTION_PARSE_WEAPON -> {
                sugWeapon = Weapon.valueOf(command);

                Suggestion suggestion = makeSuggestion(sugSuspect, sugWeapon, selectedMove);
                displayMessage = "provide suggestion results here"; // todo merge with cards logic

                if (currPlayer.isMovedViaSuggestion()) {
                    availableMoves.remove(currPlayer.getPosition());
                }
                gamestage = GameStage.CONTINUE_BUTTON;
                return new GameResponse(new StringBuilder(objectMapper.writeValueAsString(new ArrayList<>(Arrays.asList("CONTINUE")))),
                        new StringBuilder(displayMessage));
            }

            case CONTINUE_BUTTON -> {
                gamestage = GameStage.GAMEPLAY;
                turnNum++;
                setupTurn();
                return new GameResponse(new StringBuilder(objectMapper.writeValueAsString(availableMoves)),
                        new StringBuilder(displayMessage));
            }

            case ACCUSATION_PARSE_SUSPECT -> {
                accSuspect = PlayerName.valueOf(command);
                displayMessage = "Next, select your weapon: ";
                text.append(new ArrayList<>(Arrays.asList(Weapon.values()))).append("\n");
                gamestage = GameStage.ACCUSATION_PARSE_WEAPON;
                return new GameResponse(new StringBuilder(objectMapper.writeValueAsString(new ArrayList<>(Arrays.asList(Weapon.values())))),
                        new StringBuilder(displayMessage));
            }

            case ACCUSATION_PARSE_WEAPON -> {
                accWeapon = Weapon.valueOf(command);

                displayMessage = "Finally, choose a room: ";
                gamestage = GameStage.ACCUSATION_PARSE_ROOM;
                return new GameResponse(new StringBuilder(objectMapper.writeValueAsString(rooms)),
                        new StringBuilder(displayMessage));
            }

            case ACCUSATION_PARSE_ROOM -> {
                accRoom = rooms.get(rooms.indexOf(BoardSlotLabel.valueOf(command)));

                Suggestion suggestion = makeSuggestion(accSuspect, accWeapon, accRoom);
                boolean correctSuspect = gameBoard.compareSuspect(suggestion.getSuspectCard().getSuspect());
                boolean correctWeapon = gameBoard.compareWeapon(suggestion.getWeaponCard().getWeapon());
                boolean correctRoom = gameBoard.compareRoom(suggestion.getRoomCard().getRoom());

                if (correctSuspect && correctWeapon && correctRoom) {
                    winner = currPlayer.getName();
                    gamestage = GameStage.ENDGAME;
                } else {
                    System.out.printf("False accusation! Player %s is eliminated.\n", currPlayer.getName());
                    gameBoard.removePlayer(currPlayer.getName());
                    System.out.printf("[processCommand] Player %s eliminated. Remaining players: %s", currPlayer.getName(), gameBoard.getActivePlayers()+"\n");

                    if(onlyOnePlayerRemaining()) {
                        winner = gameBoard.getActivePlayers().getFirst();
                        text.append(String.format("%s is the last remaining player! They win!", winner));
                        gamestage = GameStage.ENDGAME;
                        break;
                    }

                    turnNum++;
                    gamestage = GameStage.GAMEPLAY;
                    text.append(setupTurn());
                }
            }

            case ENDGAME -> {
                displayMessage = winner + " has already won! Press restart to play again.\n";
                return new GameResponse(new StringBuilder(), new StringBuilder(displayMessage));
            }
        }
        return new GameResponse(new StringBuilder(), new StringBuilder());
    }

    private boolean onlyOnePlayerRemaining() {
        return gameBoard.getActivePlayers().size() == 1;
    }

    /** Standard setup for a player's turn that does everything prior to the player submitting their move
     * @return text output that ought to be displayed at the start of a turn
     */
    private String setupTurn() {
        System.out.printf("[setupTurn] Setting up turn #%d\n", turnNum);

        currPlayer = gameBoard.getPlayers().get(activePlayers.get(turnNum % activePlayers.size()));
        boolean movedViaSuggestion = currPlayer.isMovedViaSuggestion();
        BoardSlotLabel currPosition = currPlayer.getPosition();
        System.out.printf("[setupTurn] Current player %s; current position: %s; moved by suggestion: %s\n", currPlayer.getName(), currPosition.name(), currPlayer.isMovedViaSuggestion());

        for (PlayerName pName : PlayerName.values()) {
            Player p = gameBoard.getPlayerByName(pName);
            p.getPosition();
        }
        availableMoves = gameBoard.getAvailableMoves(currPosition);

        // if player was moved by suggestion, they have the option to stay in current room and make suggestion
        if (movedViaSuggestion) availableMoves.add(currPosition);

        if (availableMoves.isEmpty()) {
            displayMessage = "Player has no available moves. Please make formal accusation or skip turn.";
        } else {
            displayMessage = currPlayer.getName() + "'s turn! Please select from the options below where you would like to move, or make your formal accusation.";
        }

        return ""; // todo take out
    }

    public GameResponse restartGame() {

        gameBoard = new Board(createLayout());
        winner = null;
        gamestage = GameStage.FIRST_PLAYER_SELECTION;
        turnNum = 0;

        // reset available players
        List<PlayerName> availablePlayers = new ArrayList<>(Arrays.asList(

                PlayerName.MISS_SCARLET,
                PlayerName.COLONEL_MUSTARD,
                PlayerName.MRS_WHITE,
                PlayerName.MR_GREEN,
                PlayerName.MRS_PEACOCK,
                PlayerName.PROFESSOR_PLUM

        )
        );

        //randomizeWeaponPlacements();
        randomizeWeaponPlacements();

        //generateWinningSuggestion();
        generateWinningSuggestion();

        //shuffleCardsAndDeal();


        //playGame();
        try {
            return new GameResponse(new StringBuilder(objectMapper.writeValueAsString(availablePlayers)),
                    new StringBuilder("Welcome to ClueLess! It is time to select players."));
        } catch (Exception e) {
            return GameResponse.returnErrorResponse(e.getMessage());
        }
    }

    public GameResponse getBoardPositions() {

        ObjectMapper objectMapper = new ObjectMapper();
        Map<PlayerName, GridSquare> positions = Map.ofEntries(
                entry(PlayerName.MISS_SCARLET, gameBoard.getPlayerByName(PlayerName.MISS_SCARLET) == null ?
                        getGridSquareForBoardSlot(BoardSlotLabel.STARTING_SQUARE_1).setImgPath("images/miss_scarlett.png") :
                        getGridSquareForBoardSlot(gameBoard.getPlayerByName(PlayerName.MISS_SCARLET).getPosition()).setImgPath("images/miss_scarlett.png")),
                entry(PlayerName.COLONEL_MUSTARD, gameBoard.getPlayerByName(PlayerName.COLONEL_MUSTARD) == null ?
                        getGridSquareForBoardSlot(BoardSlotLabel.STARTING_SQUARE_2).setImgPath("images/colonel_mustard.png") :
                        getGridSquareForBoardSlot(gameBoard.getPlayerByName(PlayerName.COLONEL_MUSTARD).getPosition()).setImgPath("images/colonel_mustard.png")),
                entry(PlayerName.MRS_WHITE, gameBoard.getPlayerByName(PlayerName.MRS_WHITE) == null ?
                        getGridSquareForBoardSlot(BoardSlotLabel.STARTING_SQUARE_3).setImgPath("images/mrs_white.png") :
                        getGridSquareForBoardSlot(gameBoard.getPlayerByName(PlayerName.MRS_WHITE).getPosition()).setImgPath("images/mrs_white.png")),
                entry(PlayerName.MR_GREEN, gameBoard.getPlayerByName(PlayerName.MR_GREEN) == null ?
                        getGridSquareForBoardSlot(BoardSlotLabel.STARTING_SQUARE_4).setImgPath("images/mr_green.png") :
                        getGridSquareForBoardSlot(gameBoard.getPlayerByName(PlayerName.MR_GREEN).getPosition()).setImgPath("images/mr_green.png")),
                entry(PlayerName.MRS_PEACOCK, gameBoard.getPlayerByName(PlayerName.MRS_PEACOCK) == null ?
                        getGridSquareForBoardSlot(BoardSlotLabel.STARTING_SQUARE_5).setImgPath("images/mrs_peacock.png") :
                        getGridSquareForBoardSlot(gameBoard.getPlayerByName(PlayerName.MRS_PEACOCK).getPosition()).setImgPath("images/mrs_peacock.png")),
                entry(PlayerName.PROFESSOR_PLUM, gameBoard.getPlayerByName(PlayerName.PROFESSOR_PLUM) == null ?
                        getGridSquareForBoardSlot(BoardSlotLabel.STARTING_SQUARE_6).setImgPath("images/professor_plum.png") :
                        getGridSquareForBoardSlot(gameBoard.getPlayerByName(PlayerName.PROFESSOR_PLUM).getPosition()).setImgPath("images/professor_plum.png"))
        );

        try {
            String positionJson = objectMapper.writeValueAsString(positions);
            System.out.print("Position json returned from backend: " + positionJson + "\n");

            return new GameResponse(new StringBuilder(positionJson), new StringBuilder()); // no frontend text display needed
        } catch (Exception e) {
            return GameResponse.returnErrorResponse(e.getMessage());
        }


    }

    public String initializePlayer(String command) throws NumberFormatException, IndexOutOfBoundsException {
        String text = "";
        PlayerName name = PlayerName.valueOf(command);
        addPlayer(name, startingPositions.get(name));
        text += "Selected "+name+"\n";
        text += "If you would like to add an additional player, enter their corresponding index now. Otherwise, enter 'no'\n";
        text += "Available Players: " + availablePlayers +"\n";
        return text;
    }

    public enum GameStage {
        FIRST_PLAYER_SELECTION,
        ADDITIONAL_PLAYER_SELECTION,
        GAMEPLAY,
        CONTINUE_BUTTON,
        SUGGESTION_PARSE_SUSPECT,
        SUGGESTION_PARSE_WEAPON,
        ACCUSATION_PARSE_SUSPECT,
        ACCUSATION_PARSE_WEAPON,
        ACCUSATION_PARSE_ROOM,
        ENDGAME
    }

    public enum Weapon {
        ROPE,
        LEAD_PIPE,
        KNIFE,
        WRENCH,
        CANDLESTICK,
        REVOLVER
    }
    
    public enum PlayerName {
        MISS_SCARLET,
        COLONEL_MUSTARD,
        MRS_WHITE,
        MR_GREEN,
        MRS_PEACOCK,
        PROFESSOR_PLUM
    }
    
    public enum BoardSlotLabel {
        STUDY,
        HALL,
        LOUNGE,
        LIBRARY,
        BILLIARD_ROOM,
        DINING_ROOM,
        CONSERVATORY,
        BALLROOM,
        KITCHEN,
        HALLWAY_1,
        HALLWAY_2,
        HALLWAY_3,
        HALLWAY_4,
        HALLWAY_5,
        HALLWAY_6,
        HALLWAY_7,
        HALLWAY_8,
        HALLWAY_9,
        HALLWAY_10,
        HALLWAY_11,
        HALLWAY_12,
        STARTING_SQUARE_1,
        STARTING_SQUARE_2,
        STARTING_SQUARE_3,
        STARTING_SQUARE_4,
        STARTING_SQUARE_5,
        STARTING_SQUARE_6
    }
    
    List<PlayerName> availablePlayers = new ArrayList<>(Arrays.asList(
            
            PlayerName.MISS_SCARLET,
            PlayerName.COLONEL_MUSTARD,
            PlayerName.MRS_WHITE,
            PlayerName.MR_GREEN,
            PlayerName.MRS_PEACOCK,
            PlayerName.PROFESSOR_PLUM
            
        )
    );
    
    static Map<PlayerName, BoardSlotLabel> startingPositions = Map.ofEntries(
            entry(PlayerName.MISS_SCARLET, BoardSlotLabel.STARTING_SQUARE_1),
            entry(PlayerName.COLONEL_MUSTARD, BoardSlotLabel.STARTING_SQUARE_2),
            entry(PlayerName.MRS_WHITE, BoardSlotLabel.STARTING_SQUARE_3),
            entry(PlayerName.MR_GREEN, BoardSlotLabel.STARTING_SQUARE_4),
            entry(PlayerName.MRS_PEACOCK, BoardSlotLabel.STARTING_SQUARE_5),
            entry(PlayerName.PROFESSOR_PLUM, BoardSlotLabel.STARTING_SQUARE_6)
        );
    
    static List<BoardSlotLabel> rooms = new ArrayList<>(Arrays.asList(
            
            BoardSlotLabel.STUDY,
            BoardSlotLabel.HALL,
            BoardSlotLabel.LOUNGE,
            BoardSlotLabel.LIBRARY,
            BoardSlotLabel.BILLIARD_ROOM,
            BoardSlotLabel.DINING_ROOM,
            BoardSlotLabel.CONSERVATORY,
            BoardSlotLabel.BALLROOM,
            BoardSlotLabel.KITCHEN
            
        )
    );
    Board gameBoard;
    Player legacyWinner = null;

    // Legacy method
    public void startGame() {
        
        // todo take out players if not needed
        gameBoard = new Board(createLayout());
        
        selectPlayers();
        
        randomizeWeaponPlacements();
        
        generateWinningSuggestion();
        
        shuffleCardsAndDeal();
        
        playGame();
        scan.close();
        
    }

    // Legacy method
    public void playGame() {
        int i = 0;
        System.out.print("\nStarting Game!\n\n");

        List<PlayerName> activePlayers = gameBoard.getActivePlayers();
        while (winner == null) {

            Player currPlayer = gameBoard.getPlayers().get(activePlayers.get(i % activePlayers.size()));


            if (currPlayer.isActive()) {
                System.out.print("\n" + currPlayer.getName() + "'s turn!\n\n");
                legacyWinner = takeTurn(currPlayer);
            }


            i++;
        }


    }

    // Legacy method
    private Player takeTurn(Player player) {

        boolean movedViaSuggestion = player.isMovedViaSuggestion();
        BoardSlotLabel currPosition = player.getPosition();

        System.out.print("Here are the positions of all players on the board:\n\n");
        for (PlayerName pName : PlayerName.values()) {
            Player p = gameBoard.getPlayerByName(pName);
            System.out.print(p.getName() + "'s position: " + p.getPosition() + "\n");
        }
        List<BoardSlotLabel> availableMoves = gameBoard.getAvailableMoves(currPosition);

        // if player was moved by suggestion, they have the option to stay in current room and make suggestion
        if (movedViaSuggestion) availableMoves.add(currPosition);

        if (availableMoves.isEmpty()) {
            System.out.print("Player has no available moves. Please enter 100 to make formal accusation; otherwise enter any other integer to skip turn:\n");
        } else {
            System.out.print("\nAvailable moves: " + availableMoves + " Please enter the number of the slot you would like to enter:, or enter 100 to make your formal accusation:\n");
        }


        int choice = scan.nextInt();

        if (choice == 100) {
            return handleAccusation(player.getName());
        }

        // if player has no moves turn is over
        if (availableMoves.isEmpty()) return null;

        handlePlayerMove(player.getName(), availableMoves.get(choice));

        if (movedViaSuggestion) availableMoves.remove(currPosition);

        return null;

    }

    // Modified legacy method
    private boolean handlePlayerMove(PlayerName pName, BoardSlotLabel move) {

        // indicate that player moving via choice
        gameBoard.markPlayerNotMovedViaSuggestion(pName);

        // update occupancy
        gameBoard.setOccupantForBoardSlot(pName, move);

        // if user enters a room, they will make a suggestion
        if (isRoom(move)) {
            return true;
        } else {
            return false;
        }
    }

    // Legacy method
    private boolean isRoom(BoardSlotLabel slot) {
        return rooms.contains(slot);
    }

    // Legacy method
    private Player handleAccusation(PlayerName pName) {

        System.out.print("Make a formal Accusation!\nChoose a suspect: ");
        System.out.print(new ArrayList<>(Arrays.asList(PlayerName.values())) + "\n");
        PlayerName suspect = PlayerName.values()[scan.nextInt()];


        System.out.print("\nChoose a weapon: ");
        System.out.print(new ArrayList<>(Arrays.asList(Weapon.values())) + "\n");
        Weapon weapon = Weapon.values()[scan.nextInt()];

        System.out.print("\nChoose a room: ");
        System.out.print(rooms);
        BoardSlotLabel room = rooms.get(scan.nextInt());


        Suggestion suggestion = makeSuggestion(suspect, weapon, room);

        boolean correctSuspect = gameBoard.compareSuspect(suggestion.getSuspectCard().getSuspect());
        boolean correctWeapon = gameBoard.compareWeapon(suggestion.getWeaponCard().getWeapon());
        boolean correctRoom = gameBoard.compareRoom(suggestion.getRoomCard().getRoom());

        System.out.print("Accusation made! Here are your results: \n");
        System.out.print("Your suspect choice is: " + (correctSuspect ? "correct\n" : "incorrect\n"));
        System.out.print("Your weapon choice is: " + (correctWeapon ? "correct\n" : "incorrect\n"));
        System.out.print("Your room choice is: " + (correctRoom ? "correct\n" : "incorrect\n"));

        if (correctSuspect && correctWeapon && correctRoom) {
            return announceWinner(pName);
        } else {
            System.out.print("False accusation! Player is eliminated.\n");
            gameBoard.removePlayer(pName);

            return checkForLastPlayerRemaining();

        }
    }

    // Modified legacy method
    private Player checkForLastPlayerRemaining() {
        if (gameBoard.getNumActivePlayers() == 1) {
            System.out.print("Only one player remains, so they win!\n");
            winner = gameBoard.getActivePlayers().getFirst();
        }

        return null;
    }

    // Legacy method
    private Player announceWinner(PlayerName pName) {
        System.out.print("Winner :) Congratulations " + pName + "!!! Game over.");
        return gameBoard.getPlayerByName(pName);
    }

    // Legacy method
    private void selectPlayers() {
        int morePlayers = 1;

        while (morePlayers == 1) {
            System.out.print("Available Players: " + availablePlayers + "\nPlease enter the number of the player you would like to select.\n");
            int playerChoice = scan.nextInt();
            PlayerName name = availablePlayers.get(playerChoice);
            addPlayer(name, startingPositions.get(name));

            System.out.print("Done. Please enter 1 if you would like to add another player, and any other integer if you are ready to begin the game:\n");
            morePlayers = scan.nextInt();
        }

        // initialize inactive players
        for (PlayerName pName : availablePlayers) {
            gameBoard.addPlayer(new Player(pName, startingPositions.get(pName), false));
        }

    }

    // Legacy method
    // todo make endpoint
    public void addPlayer(PlayerName name, BoardSlotLabel position) {

        gameBoard.addPlayer(new Player(name, position, true));
        availablePlayers.remove(name);

    }

    public List<PlayerName> getAvailablePlayers() {
        // todo see if needed
        return availablePlayers;

    }

    // Legacy method
    private Map<BoardSlotLabel, BoardSlot> createLayout() {
        Map<BoardSlotLabel, BoardSlot> layout = new HashMap<BoardSlotLabel, BoardSlot>();


        // ADD ROOMS
        layout.put(BoardSlotLabel.STUDY, new Room(BoardSlotLabel.STUDY,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.HALLWAY_1, BoardSlotLabel.HALLWAY_3, BoardSlotLabel.KITCHEN))));

        layout.put(BoardSlotLabel.HALL, new Room(BoardSlotLabel.HALL,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.HALLWAY_1, BoardSlotLabel.HALLWAY_2, BoardSlotLabel.HALLWAY_4))));

        layout.put(BoardSlotLabel.LOUNGE, new Room(BoardSlotLabel.LOUNGE,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.HALLWAY_2, BoardSlotLabel.HALLWAY_5, BoardSlotLabel.CONSERVATORY))));


        layout.put(BoardSlotLabel.LIBRARY, new Room(BoardSlotLabel.LIBRARY,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.HALLWAY_3, BoardSlotLabel.HALLWAY_6, BoardSlotLabel.HALLWAY_8))));


        layout.put(BoardSlotLabel.BILLIARD_ROOM, new Room(BoardSlotLabel.BILLIARD_ROOM,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.HALLWAY_4, BoardSlotLabel.HALLWAY_6, BoardSlotLabel.HALLWAY_7, BoardSlotLabel.HALLWAY_9))));

        layout.put(BoardSlotLabel.DINING_ROOM, new Room(BoardSlotLabel.DINING_ROOM,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.HALLWAY_5, BoardSlotLabel.HALLWAY_7, BoardSlotLabel.HALLWAY_10))));

        layout.put(BoardSlotLabel.CONSERVATORY, new Room(BoardSlotLabel.CONSERVATORY,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.HALLWAY_8, BoardSlotLabel.HALLWAY_11, BoardSlotLabel.LOUNGE))));

        layout.put(BoardSlotLabel.BALLROOM, new Room(BoardSlotLabel.BALLROOM,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.HALLWAY_9, BoardSlotLabel.HALLWAY_11, BoardSlotLabel.HALLWAY_12))));

        layout.put(BoardSlotLabel.KITCHEN, new Room(BoardSlotLabel.KITCHEN,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.HALLWAY_10, BoardSlotLabel.HALLWAY_12, BoardSlotLabel.STUDY))));

        // ADD HALLWAYS
        layout.put(BoardSlotLabel.HALLWAY_1, new Hallway(BoardSlotLabel.HALLWAY_1,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.STUDY, BoardSlotLabel.HALL))));

        layout.put(BoardSlotLabel.HALLWAY_2, new Hallway(BoardSlotLabel.HALLWAY_2,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.LOUNGE, BoardSlotLabel.HALL))));

        layout.put(BoardSlotLabel.HALLWAY_3, new Hallway(BoardSlotLabel.HALLWAY_3,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.STUDY, BoardSlotLabel.LIBRARY))));

        layout.put(BoardSlotLabel.HALLWAY_4, new Hallway(BoardSlotLabel.HALLWAY_4,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.BILLIARD_ROOM, BoardSlotLabel.HALL))));

        layout.put(BoardSlotLabel.HALLWAY_5, new Hallway(BoardSlotLabel.HALLWAY_5,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.LOUNGE, BoardSlotLabel.DINING_ROOM))));

        layout.put(BoardSlotLabel.HALLWAY_6, new Hallway(BoardSlotLabel.HALLWAY_6,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.LIBRARY, BoardSlotLabel.BILLIARD_ROOM))));

        layout.put(BoardSlotLabel.HALLWAY_7, new Hallway(BoardSlotLabel.HALLWAY_7,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.BILLIARD_ROOM, BoardSlotLabel.DINING_ROOM))));

        layout.put(BoardSlotLabel.HALLWAY_8, new Hallway(BoardSlotLabel.HALLWAY_8,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.CONSERVATORY, BoardSlotLabel.LIBRARY))));

        layout.put(BoardSlotLabel.HALLWAY_9, new Hallway(BoardSlotLabel.HALLWAY_9,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.BILLIARD_ROOM, BoardSlotLabel.BALLROOM))));

        layout.put(BoardSlotLabel.HALLWAY_10, new Hallway(BoardSlotLabel.HALLWAY_10,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.DINING_ROOM, BoardSlotLabel.KITCHEN))));

        layout.put(BoardSlotLabel.HALLWAY_11, new Hallway(BoardSlotLabel.HALLWAY_11,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.CONSERVATORY, BoardSlotLabel.BALLROOM))));

        layout.put(BoardSlotLabel.HALLWAY_12, new Hallway(BoardSlotLabel.HALLWAY_12,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.BALLROOM, BoardSlotLabel.KITCHEN))));

        layout.put(BoardSlotLabel.STARTING_SQUARE_1, new StartingSquare(BoardSlotLabel.STARTING_SQUARE_1,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.HALLWAY_2))));

        layout.put(BoardSlotLabel.STARTING_SQUARE_2, new StartingSquare(BoardSlotLabel.STARTING_SQUARE_2,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.HALLWAY_5))));

        layout.put(BoardSlotLabel.STARTING_SQUARE_3, new StartingSquare(BoardSlotLabel.STARTING_SQUARE_3,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.HALLWAY_12))));

        layout.put(BoardSlotLabel.STARTING_SQUARE_4, new StartingSquare(BoardSlotLabel.STARTING_SQUARE_4,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.HALLWAY_11))));

        layout.put(BoardSlotLabel.STARTING_SQUARE_5, new StartingSquare(BoardSlotLabel.STARTING_SQUARE_5,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.HALLWAY_8))));

        layout.put(BoardSlotLabel.STARTING_SQUARE_6, new StartingSquare(BoardSlotLabel.STARTING_SQUARE_6,
                new ArrayList<>(Arrays.asList(BoardSlotLabel.HALLWAY_3))));

        return layout;
    }


    // Legacy method
    private void randomizeWeaponPlacements() {

        Random rand = new Random();

        List<BoardSlotLabel> availableRooms = new ArrayList<>(Arrays.asList(

            BoardSlotLabel.STUDY,
            BoardSlotLabel.HALL,
            BoardSlotLabel.LOUNGE,
            BoardSlotLabel.LIBRARY,
            BoardSlotLabel.BILLIARD_ROOM,
            BoardSlotLabel.DINING_ROOM,
            BoardSlotLabel.CONSERVATORY,
            BoardSlotLabel.BALLROOM,
            BoardSlotLabel.KITCHEN

        ));

        try {
            for (Weapon weapon : Weapon.values()) {
                BoardSlotLabel choice = availableRooms.get(rand.nextInt(availableRooms.size()));
                gameBoard.setWeaponForRoom(choice, weapon);
                availableRooms.remove(choice);
            }
        } catch (Exception e) {

            // todo better error handling
            System.out.print("Error initializing weapon placement: " + e.getMessage());
        }
    }

    // Legacy method
    private void generateWinningSuggestion() {
        Random rand = new Random();

        // person
        PlayerName[] possibleSuspects = PlayerName.values();
        PlayerName who = possibleSuspects[rand.nextInt(possibleSuspects.length)];

        // room
        BoardSlotLabel where = rooms.get(rand.nextInt(rooms.size()));

        // weapon
        Weapon[] possibleWeapons = Weapon.values();
        Weapon what = possibleWeapons[rand.nextInt(possibleWeapons.length)];

        gameBoard.setWinningSuggestion(new Suggestion(new SuspectCard(who), new WeaponCard(what), new RoomCard(where)));

        System.out.print("[generateWinningSuggestion]: Winning trio is " + gameBoard.getWinningSuggestion().getSuspectCard().getSuspect() +
                " with the " + gameBoard.getWinningSuggestion().getWeaponCard().getWeapon() + " in the " + gameBoard.getWinningSuggestion().getRoomCard().getRoom() + "\n\n");

    }

    // Legacy method
    private void shuffleCardsAndDeal() {
        List<Card> remainingCards = new ArrayList<>(Arrays.asList(

                new SuspectCard(PlayerName.MISS_SCARLET),
                new SuspectCard(PlayerName.COLONEL_MUSTARD),
                new SuspectCard(PlayerName.MRS_WHITE),
                new SuspectCard(PlayerName.MR_GREEN),
                new SuspectCard(PlayerName.MRS_PEACOCK),
                new SuspectCard(PlayerName.PROFESSOR_PLUM),

                new WeaponCard(Weapon.ROPE),
                new WeaponCard(Weapon.LEAD_PIPE),
                new WeaponCard(Weapon.KNIFE),
                new WeaponCard(Weapon.WRENCH),
                new WeaponCard(Weapon.CANDLESTICK),
                new WeaponCard(Weapon.REVOLVER),

                new RoomCard(BoardSlotLabel.STUDY),
                new RoomCard(BoardSlotLabel.HALL),
                new RoomCard(BoardSlotLabel.LOUNGE),
                new RoomCard(BoardSlotLabel.LIBRARY),
                new RoomCard(BoardSlotLabel.BILLIARD_ROOM),
                new RoomCard(BoardSlotLabel.DINING_ROOM),
                new RoomCard(BoardSlotLabel.CONSERVATORY),
                new RoomCard(BoardSlotLabel.BALLROOM),
                new RoomCard(BoardSlotLabel.KITCHEN)

            )
        );

        remainingCards.removeIf(c -> c.getSuspect() == gameBoard.getWinningSuggestion().getSuspectCard().getSuspect());
        remainingCards.removeIf(c -> c.getWeapon() == gameBoard.getWinningSuggestion().getWeaponCard().getWeapon());
        remainingCards.removeIf(c -> c.getRoom() == gameBoard.getWinningSuggestion().getRoomCard().getRoom());
    }

    // Legacy method
    private void promptForSuggestion(BoardSlotLabel room) {

        System.out.print("Make a suggestion!\nChoose a suspect: ");
        System.out.print(new ArrayList<>(Arrays.asList(PlayerName.values())) + "\n");
        PlayerName suspect = PlayerName.values()[scan.nextInt()];


        System.out.print("\nChoose a weapon: ");
        System.out.print(new ArrayList<>(Arrays.asList(Weapon.values())) + "\n");
        Weapon weapon = Weapon.values()[scan.nextInt()];



        System.out.print("\nYour suggestion room is: " + room + "\n");


        Suggestion suggestion = makeSuggestion(suspect, weapon, room);

        System.out.print("Suggestion made! Here are your results: \n");
        System.out.print("Your suspect choice is: " + (gameBoard.compareSuspect(suggestion.getSuspectCard().getSuspect()) ? "correct\n" : "incorrect\n"));
        System.out.print("Your weapon choice is: " + (gameBoard.compareWeapon(suggestion.getWeaponCard().getWeapon()) ? "correct\n" : "incorrect\n"));
        System.out.print("Your room choice is: " + (gameBoard.compareRoom(suggestion.getRoomCard().getRoom()) ? "correct\n" : "incorrect\n"));
    }

    // Legacy method
    private Suggestion makeSuggestion(PlayerName suspect, Weapon weapon, BoardSlotLabel room) {
        gameBoard.setOccupantForBoardSlot(suspect, room);
        
        // mark player as moved via suggestion
        gameBoard.markPlayerMovedViaSuggestion(suspect);
        
        return new Suggestion(new SuspectCard(suspect), new WeaponCard(weapon), new RoomCard(room));
    }
    

}
