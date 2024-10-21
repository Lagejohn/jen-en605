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
        COLONEL_MUSTARD,
        MISS_SCARLET,
        PROFESSOR_PLUM,
        MR_GREEN,
        MRS_WHITE,
        MRS_PEACOCK
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
    
    Map<PlayerName, Player> players = new HashMap<>();
    List<PlayerName> activePlayers = new ArrayList<>();
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
        
        playGame();
        scan.close();
        
    }
    
    public void playGame() {
        int i = 0;
        System.out.print("\nStarting Game!\n\n");
        while (winner == null) {
            
            Player currPlayer = gameBoard.getPlayers().get(activePlayers.get(i % activePlayers.size()));
            System.out.print("\n" + currPlayer.getName() + "'s turn!\n\n");
            
            winner = takeTurn(currPlayer);
            
            i++;
        }
        
        
    }
    
    private Player takeTurn(Player player) {
        System.out.print("Here are the positions of all players on the board:\n\n");
        for (PlayerName pName : activePlayers) {
            Player p = gameBoard.getPlayerByName(pName);
            System.out.print(p.getName() + "'s position: " + p.getPosition() + "\n");
        }
        List<BoardSlotLabel> availableMoves = gameBoard.getAvailableMoves(player.getPosition());
        System.out.print("\nAvailable moves: " + availableMoves + " Please enter the number of the slot you would like to enter:, or enter 100 to make your formal accusation:\n");
        
        int choice = scan.nextInt();
        
        if (choice == 100) {
            return handleAccusation(player);
        }
        
        BoardSlotLabel move = availableMoves.get(choice);
        gameBoard.setOccupantForBoardSlot(player.getName(), move);
        if (isRoom(move)) {
            promptForSuggestion(move);
        }
        
        
        return null;
    }
    
    private boolean isRoom(BoardSlotLabel slot) {
        return rooms.contains(slot);
    }
    
    private Player handleAccusation(Player player) {

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
        
        boolean correctSuspect = gameBoard.compareSuspect(suggestion.getSuspect());
        boolean correctWeapon = gameBoard.compareWeapon(suggestion.getWeapon());
        boolean correctRoom = gameBoard.compareRoom(suggestion.getRoom());
        
        System.out.print("Accusation made! Here are your results: \n");
        System.out.print("Your suspect choice is: " + (correctSuspect ? "correct\n" : "incorrect\n"));
        System.out.print("Your weapon choice is: " + (correctWeapon ? "correct\n" : "incorrect\n"));
        System.out.print("Your room choice is: " + (correctRoom ? "correct\n" : "incorrect\n"));
        
        if (correctSuspect && correctWeapon && correctRoom) {
            return announceWinner(player);
        } else {
            System.out.print("False accusation! Player is eliminated.\n");
            activePlayers.remove(player.getName());
            gameBoard.removePlayer(player.getName());
            
        }
        
        return null;
    }
    
    private Player announceWinner(Player player) {
        System.out.print("Correct accusation! Winner :) Congratulations " + player.getName() + "!!! Game over.");
        return player;
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
        
        gameBoard.setPlayers(players);
    }
    
    // todo make endpoint
    public void addPlayer(PlayerName name, BoardSlotLabel position) {
        
        players.put(name, new Player(name, position));
        
        activePlayers.add(name);
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
        
        gameBoard.setWinningSuggestion(makeSuggestion(who, what, where));
        
        System.out.print("FOR DEMONSTRATION: Winning trio is " + gameBoard.getWinningSuggestion().getSuspect() +
                " with the " + gameBoard.getWinningSuggestion().getWeapon() + " in the " + gameBoard.getWinningSuggestion().getRoom() + "\n\n");
        
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
        System.out.print("Your suspect choice is: " + (gameBoard.compareSuspect(suggestion.getSuspect()) ? "correct\n" : "incorrect\n"));
        System.out.print("Your weapon choice is: " + (gameBoard.compareWeapon(suggestion.getWeapon()) ? "correct\n" : "incorrect\n"));
        System.out.print("Your room choice is: " + (gameBoard.compareRoom(suggestion.getRoom()) ? "correct\n" : "incorrect\n"));
    }

    private Suggestion makeSuggestion(PlayerName suspect, Weapon weapon, BoardSlotLabel room) {
     //   gameBoard.movePlayer(suspect, room);
        
        return new Suggestion(suspect, room, weapon);
    }
    

}
