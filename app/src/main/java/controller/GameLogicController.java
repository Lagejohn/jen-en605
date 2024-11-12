package controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import static java.util.Map.entry;

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

    PlayerName sugSuspect;
    Weapon sugWeapon;

    PlayerName accSuspect;
    Weapon accWeapon;
    BoardSlotLabel accRoom;

    /**
     * Primary method for handling input commands from the frontend client
     *
     * @param command String representing command submitted
     * @return Text to be sent back to the user based on consequences of their command
     */
    public StringBuilder processCommand(String command) {
        System.out.println("[processCommand] Current gamestage: "+gamestage);
        System.out.println("[processCommand] User command: '"+command+"'");
        StringBuilder text = new StringBuilder("User: " + command + "\n");
        switch(gamestage) {

            case ADDITIONAL_PLAYER_SELECTION -> {
                if(command.equals("no")) {
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

                    text.append(setupTurn());
                    break;
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
                if (command.equals("100")) {
                    //winner = handleAccusation(currPlayer.getName());
                    text.append("Make a formal Accusation!\nChoose a suspect: ");
                    text.append(new ArrayList<>(Arrays.asList(PlayerName.values()))).append("\n");
                    gamestage = GameStage.ACCUSATION_PARSE_SUSPECT;
                    break;
                }
                if (availableMoves.isEmpty()) {
                    System.out.printf("[processCommand] player %s declined to accuse and has no available moves, skipping...",currPlayer.getName());
                    text.append("Player declined to accuse and has no available moves, skipping turn...\n");
                    turnNum++;
                    text.append(setupTurn());
                    break;
                }
                selectedMove = availableMoves.get(Integer.parseInt(command));
                boolean isInRoom = handlePlayerMove(currPlayer.getName(),selectedMove);
                if(isInRoom) {
                    // Must make a suggestion
                    text.append("Make a suggestion!\nChoose a suspect: ");
                    text.append(new ArrayList<>(Arrays.asList(PlayerName.values()))).append("\n");
                    gamestage = GameStage.SUGGESTION_PARSE_SUSPECT;
                } else {
                    turnNum ++;
                    text.append(setupTurn());
                }
                break;
            }

            case SUGGESTION_PARSE_SUSPECT -> {
                sugSuspect = PlayerName.values()[Integer.parseInt(command)];
                text.append("\nChoose a weapon: ");
                text.append(new ArrayList<>(Arrays.asList(Weapon.values()))).append("\n");
                gamestage = GameStage.SUGGESTION_PARSE_WEAPON;

                break;
            }

            case SUGGESTION_PARSE_WEAPON -> {
                sugWeapon = Weapon.values()[Integer.parseInt(command)];

                text.append("\nYour suggestion room is: ").append(selectedMove).append("\n");

                Suggestion suggestion = makeSuggestion(sugSuspect, sugWeapon, selectedMove);

                System.out.print("Suggestion made! Here are your results: \n");
                text.append("Your suspect choice is: ").append(gameBoard.compareSuspect(suggestion.getSuspectCard().getSuspect()) ? "correct\n" : "incorrect\n");
                text.append("Your weapon choice is: ").append(gameBoard.compareWeapon(suggestion.getWeaponCard().getWeapon()) ? "correct\n" : "incorrect\n");
                text.append("Your room choice is: ").append(gameBoard.compareRoom(suggestion.getRoomCard().getRoom()) ? "correct\n" : "incorrect\n");

                if (currPlayer.isMovedViaSuggestion()) {
                    availableMoves.remove(currPlayer.getPosition());
                }
                gamestage = GameStage.GAMEPLAY;
                turnNum++;
                text.append(setupTurn());
            }

            case ACCUSATION_PARSE_SUSPECT -> {
                accSuspect = PlayerName.values()[Integer.parseInt(command)];
                text.append("\nChoose a weapon: ");
                text.append(new ArrayList<>(Arrays.asList(Weapon.values()))).append("\n");
                gamestage = GameStage.ACCUSATION_PARSE_WEAPON;
                break;
            }

            case ACCUSATION_PARSE_WEAPON -> {
                accWeapon = Weapon.values()[Integer.parseInt(command)];

                text.append("\nChoose a room: ");
                text.append(rooms);
                gamestage = GameStage.ACCUSATION_PARSE_ROOM;
                break;
            }

            case ACCUSATION_PARSE_ROOM -> {
                accRoom = rooms.get(Integer.parseInt(command));

                Suggestion suggestion = makeSuggestion(accSuspect, accWeapon, accRoom);
                boolean correctSuspect = gameBoard.compareSuspect(suggestion.getSuspectCard().getSuspect());
                boolean correctWeapon = gameBoard.compareWeapon(suggestion.getWeaponCard().getWeapon());
                boolean correctRoom = gameBoard.compareRoom(suggestion.getRoomCard().getRoom());

                text.append("Accusation made! Here are your results: \n");
                text.append("Your suspect choice is: ").append(correctSuspect ? "correct\n" : "incorrect\n");
                text.append("Your weapon choice is: ").append(correctWeapon ? "correct\n" : "incorrect\n");
                text.append("Your room choice is: ").append(correctRoom ? "correct\n" : "incorrect\n");

                if (correctSuspect && correctWeapon && correctRoom) {
                    winner = currPlayer.getName();
                    gamestage = GameStage.ENDGAME;
                } else {
                    System.out.printf("False accusation! Player %s is eliminated.\n", currPlayer.getName());
                    gameBoard.removePlayer(currPlayer.getName());

                    checkForLastPlayerRemaining();

                }
            }

            case ENDGAME -> {
                text.append(winner).append(" has already won! Press restart to play again.\n");
            }
        }
        return text;
    }

    /** Standard setup for a player's turn that does everything prior to the player submitting their move
     * @return text output that ought to be displayed at the start of a turn
     */
    private String setupTurn() {
        System.out.printf("[setupTurn] Setting up turn #%d\n", turnNum);
        StringBuilder text = new StringBuilder();

        currPlayer = gameBoard.getPlayers().get(activePlayers.get(turnNum % activePlayers.size()));
        text.append("\n").append(currPlayer.getName()).append("'s turn!\n\n");
        boolean movedViaSuggestion = currPlayer.isMovedViaSuggestion();
        BoardSlotLabel currPosition = currPlayer.getPosition();
        System.out.printf("[setupTurn] Current player %s; current position: %s; moved by suggestion: %s\n", currPlayer.getName(), currPosition.name(), currPlayer.isMovedViaSuggestion());

        text.append("Here are the positions of all players on the board:\n\n");
        for (PlayerName pName : PlayerName.values()) {
            Player p = gameBoard.getPlayerByName(pName);
            text.append(p.getName()).append("'s position: ").append(p.getPosition()).append("\n");
        }
        availableMoves = gameBoard.getAvailableMoves(currPosition);

        // if player was moved by suggestion, they have the option to stay in current room and make suggestion
        if (movedViaSuggestion) availableMoves.add(currPosition);

        if (availableMoves.isEmpty()) {
            text.append("Player has no available moves. Please enter 100 to make formal accusation; otherwise enter any other integer to skip turn:\n");
        } else {
            text.append("\nAvailable moves: ").append(availableMoves).append(" Please enter the number of the slot you would like to enter:, or enter 100 to make your formal accusation:\n");
        }

        return text.toString();
    }

    public String restartGame() {
        String text = "--New Game Started--\n";
        text += "Welcome to clueless!\n\n";
        gameBoard = new Board(createLayout());
        winner = null;
        gamestage = GameStage.FIRST_PLAYER_SELECTION;
        turnNum = 0;

        // selectPlayers()
        text += "Available Players: " + availablePlayers + "\nPlease enter the number of the player you would like to select.\n";


        //randomizeWeaponPlacements();
        randomizeWeaponPlacements();

        //generateWinningSuggestion();
        generateWinningSuggestion();

        //shuffleCardsAndDeal();


        //playGame();

        return text;
    }

    public String initializePlayer(String command) throws NumberFormatException, IndexOutOfBoundsException {
        String text = "";
        int playerChoice = Integer.parseInt(command);
        PlayerName name = availablePlayers.get(playerChoice);
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
