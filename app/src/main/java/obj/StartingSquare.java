package obj;

import java.util.List;
import controller.GameLogicController;

public class StartingSquare extends BoardSlot {

    public StartingSquare(GameLogicController.BoardSlotLabel label,
            List<GameLogicController.BoardSlotLabel> availableMoves) {
        super(label, availableMoves);
    }

}
