package obj;

import java.util.List;
import controller.GameLogicController;

public class Room extends BoardSlot {
    
    private GameLogicController.Weapon weapon;
    
    public Room(GameLogicController.BoardSlotLabel label, List<GameLogicController.BoardSlotLabel> availableMoves) {
        super(label, availableMoves);
    }
    
    @Override
    public void setWeapon(GameLogicController.Weapon weapon) {
        this.weapon = weapon;
    }
    
    public GameLogicController.Weapon getWeapon() {
        return this.weapon;
    }

}
