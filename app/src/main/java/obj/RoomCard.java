package obj;

import controller.GameLogicController;

public class RoomCard extends Card {
    GameLogicController.BoardSlotLabel room;
    
    public RoomCard(GameLogicController.BoardSlotLabel room) {
        this.room = room;
    }

    @Override
    public GameLogicController.BoardSlotLabel getRoom() {
        return this.room;
    }

    protected String cardType() { return "Room card"; }

    public String getContents() { return this.room.name(); }


}
