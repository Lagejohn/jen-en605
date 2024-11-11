import java.util.List;

public class Hallway extends BoardSlot {

    public Hallway(GameLogicController.BoardSlotLabel label, List<GameLogicController.BoardSlotLabel> availableMoves) {
        super(label, availableMoves);
    }
    
    // only one player can be in a hallway at a time
    @Override
    public boolean canEnter() {
        return this.occupants.isEmpty();
    }

}
