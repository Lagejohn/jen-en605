package obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import controller.GameLogicController;

public class Board {
    
    protected Map<GameLogicController.PlayerName, Player> players = new HashMap<>();
            
    protected Suggestion winningSuggestion;
    protected Map<GameLogicController.BoardSlotLabel, BoardSlot> layout;
    
    int numActivePlayers;
    
    public Board(Map<GameLogicController.BoardSlotLabel, BoardSlot> layout) {
        this.layout = layout;
        this.numActivePlayers = 0;
    }
    
    
    public void setLayout(Map<GameLogicController.BoardSlotLabel, BoardSlot> layout) {
        this.layout = layout;
    }
    
    public Map<GameLogicController.BoardSlotLabel, BoardSlot> getLayout() {
        return this.layout;
    }
    
    public void setWeaponForRoom(GameLogicController.BoardSlotLabel room, GameLogicController.Weapon weapon) throws Exception {
        this.layout.get(room).setWeapon(weapon);
    }
    
    public void setWinningSuggestion(Suggestion winningSuggestion) {
        this.winningSuggestion = winningSuggestion;
    }
    
    public Suggestion getWinningSuggestion() {
        return this.winningSuggestion;
    }
    
    public int getNumActivePlayers() {
        return this.numActivePlayers;
    }

    public void addPlayer(Player p) {
        this.players.put(p.getName(), p);
        
        if (p.isActive()) this.numActivePlayers++;
    }

    public Map<GameLogicController.PlayerName, Player> getPlayers() {
        return this.players;
    }
    
    public Player getPlayerByName(GameLogicController.PlayerName pName) {
        return this.getPlayers().get(pName);
    }
    
    public List<GameLogicController.BoardSlotLabel> getAvailableMoves(GameLogicController.BoardSlotLabel currSlot) {
        List<GameLogicController.BoardSlotLabel> adjacentSlots = this.layout.get(currSlot).getAdjacentSlots();
        
        adjacentSlots.removeIf(s -> !this.layout.get(s).canEnter());
        
        return adjacentSlots;
    }
    
    public void setOccupantForBoardSlot(GameLogicController.PlayerName pName, GameLogicController.BoardSlotLabel slot) {
        Player player = this.getPlayerByName(pName);
        this.layout.get(player.getPosition()).removeOccupant(player);
        this.layout.get(slot).addOccupant(player);
        player.setPosition(slot);
    }
    
    public void markPlayerMovedViaSuggestion(GameLogicController.PlayerName pName) {
        this.getPlayerByName(pName).markMovedViaSuggestion();
    }
    
    public void markPlayerNotMovedViaSuggestion(GameLogicController.PlayerName pName) {
        this.getPlayerByName(pName).markNotMovedViaSuggestion();
    }
    
    public boolean compareSuspect(GameLogicController.PlayerName suspect) {
        return suspect == this.winningSuggestion.getSuspectCard().getSuspect();
    }
    
    public boolean compareWeapon(GameLogicController.Weapon weapon) {
        return weapon == this.winningSuggestion.getWeaponCard().getWeapon();
    }
    
    public boolean compareRoom(GameLogicController.BoardSlotLabel room) {
        return room == this.winningSuggestion.getRoomCard().getRoom();
    }
    
    public void removePlayer(GameLogicController.PlayerName pName) {
        this.numActivePlayers --;
        this.players.get(pName).makeInactive();
    }
    
    public List<GameLogicController.PlayerName> getActivePlayers() {
        List<Player> activePlayers = players.values().stream()
                .filter(p -> p.isActive())
                .collect(Collectors.toList());
        
        List <GameLogicController.PlayerName> activePlayerNames = new ArrayList<>();
        
        for (Player p : activePlayers) {
            activePlayerNames.add(p.getName());
        }
        
        return activePlayerNames;
    }

}
