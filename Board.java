import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {
    
    protected Map<GameLogicController.PlayerName, Player> players = new HashMap<>();
            
    protected Suggestion winningSuggestion;
    protected Map<GameLogicController.BoardSlotLabel, BoardSlot> layout;
    
    public Board(Map<GameLogicController.BoardSlotLabel, BoardSlot> layout) {
        this.layout = layout;
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
    
    public void setPlayers(Map<GameLogicController.PlayerName, Player> players) {
        this.players = players;
    }

    public Map<GameLogicController.PlayerName, Player> getPlayers() {
        return this.players;
    }
    
    public Player getPlayerByName(GameLogicController.PlayerName playerName) {
        return this.getPlayers().get(playerName);
    }
    
    public List<GameLogicController.BoardSlotLabel> getAvailableMoves(GameLogicController.BoardSlotLabel currSlot) {
        List<GameLogicController.BoardSlotLabel> adjacentSlots = this.layout.get(currSlot).getAdjacentSlots();
        
        adjacentSlots.removeIf(s -> !this.layout.get(s).canEnter());
        
        return adjacentSlots;
    }
    
    public void setOccupantForBoardSlot(GameLogicController.PlayerName playerName, GameLogicController.BoardSlotLabel slot) {
        Player player = this.getPlayers().get(playerName);
        this.layout.get(player.getPosition()).removeOccupant(player);
        this.layout.get(slot).addOccupant(player);
        player.setPosition(slot);
    }
    
    public boolean compareSuspect(GameLogicController.PlayerName suspect) {
        return suspect == this.winningSuggestion.getSuspect();
    }
    
    public boolean compareWeapon(GameLogicController.Weapon weapon) {
        return weapon == this.winningSuggestion.getWeapon();
    }
    
    public boolean compareRoom(GameLogicController.BoardSlotLabel room) {
        return room == this.winningSuggestion.getRoom();
    }
    
    public void movePlayer(GameLogicController.PlayerName player, GameLogicController.BoardSlotLabel room) {
        
        // todo fix this to include non active players
        this.players.get(player).setPosition(room);
        this.layout.get(room).addOccupant(this.players.get(player));
    }
    
    public void removePlayer(GameLogicController.PlayerName playerName) {
        this.players.remove(playerName);
    }

}
