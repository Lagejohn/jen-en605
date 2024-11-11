package obj;

import java.util.ArrayList;
import java.util.List;

import controller.GameLogicController;

public class BoardSlot {
    
    protected List<Player> occupants;
    protected GameLogicController.BoardSlotLabel label;
    protected List<GameLogicController.BoardSlotLabel> adjacentSlots;
    
    public BoardSlot(GameLogicController.BoardSlotLabel label, List<GameLogicController.BoardSlotLabel> adjacentSlots) {
        this.occupants = new ArrayList<>();
        this.label = label;
        this.adjacentSlots = adjacentSlots;
    }

    public List<Player> getOccupants() {
        return this.occupants;
    }

    public void addOccupant(Player player) {
        this.occupants.add(player);
    }
    
    public void removeOccupant(Player player) {
        this.occupants.remove(player);
    }
    
    public List<GameLogicController.BoardSlotLabel> getAdjacentSlots() {
        return this.adjacentSlots;
    }
    
    public void setWeapon(GameLogicController.Weapon weapon) throws Exception {
        throw new Exception("Cannot set weapon for this type of board slot.");
    }
    
    public boolean canEnter() {
        return true;
    }
    
    
}
