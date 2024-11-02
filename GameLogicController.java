import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import static java.util.Map.entry;

public class GameLogicController {
    // todo: take out for GUI implementation
    Scanner scan = new Scanner( System.in );
    

    enum Weapon {
        ROPE,
        LEAD_PIPE,
        KNIFE,
        WRENCH,
        CANDLESTICK,
        REVOLVER
    }
    
    enum PlayerName {
        MISS_SCARLET,
        COLONEL_MUSTARD,
        MRS_WHITE,
        MR_GREEN,
        MRS_PEACOCK,
        PROFESSOR_PLUM
    }
    
    enum BoardSlotLabel {
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
    Player winner = null;
    
    
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
    
    public void playGame() {
        int i = 0;
        System.out.print("\nStarting Game!\n\n");
        
        List<PlayerName> activePlayers = gameBoard.getActivePlayers();
        while (winner == null) {
            
            Player currPlayer = gameBoard.getPlayers().get(activePlayers.get(i % activePlayers.size()));
            
            
            if (currPlayer.isActive()) {
                System.out.print("\n" + currPlayer.getName() + "'s turn!\n\n");
                winner = takeTurn(currPlayer);
            }
            
            
            i++;
        }
        
        
    }
    
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
    
    private void handlePlayerMove(PlayerName pName, BoardSlotLabel move) {
        
        // indicate that player moving via choice
        gameBoard.markPlayerNotMovedViaSuggestion(pName);
        
        // update occupancy
        gameBoard.setOccupantForBoardSlot(pName, move);
        
        // if user enters a room, they will make a suggestion
        if (isRoom(move)) {
            promptForSuggestion(move);
        }
    }
    
    private boolean isRoom(BoardSlotLabel slot) {
        return rooms.contains(slot);
    }
    
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
    
    private Player checkForLastPlayerRemaining() {
        if (gameBoard.getNumActivePlayers() == 1) {
            System.out.print("Only one player remains, so they win!\n");
            return announceWinner(gameBoard.getActivePlayers().get(0));
        }
        
        return null;
    }
    
    private Player announceWinner(PlayerName pName) {
        System.out.print("Winner :) Congratulations " + pName + "!!! Game over.");
        return gameBoard.getPlayerByName(pName);
    }
    
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
    
    // todo make endpoint
    public void addPlayer(PlayerName name, BoardSlotLabel position) {
        
        gameBoard.addPlayer(new Player(name, position, true));
        availablePlayers.remove(name);
        
    }
    
    public List<PlayerName> getAvailablePlayers() {
        // todo see if needed
        return availablePlayers;
        
    }
    
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
        
        System.out.print("FOR DEMONSTRATION: Winning trio is " + gameBoard.getWinningSuggestion().getSuspectCard().getSuspect() +
                " with the " + gameBoard.getWinningSuggestion().getWeaponCard().getWeapon() + " in the " + gameBoard.getWinningSuggestion().getRoomCard().getClass() + "\n\n");
        
    }
    
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

    private Suggestion makeSuggestion(PlayerName suspect, Weapon weapon, BoardSlotLabel room) {
        gameBoard.setOccupantForBoardSlot(suspect, room);
        
        // mark player as moved via suggestion
        gameBoard.markPlayerMovedViaSuggestion(suspect);
        
        return new Suggestion(new SuspectCard(suspect), new WeaponCard(weapon), new RoomCard(room));
    }
    

}
