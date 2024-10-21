
public class Player {
    GameLogicController.PlayerName name;
    GameLogicController.BoardSlotLabel position;
    
    public Player(GameLogicController.PlayerName name, GameLogicController.BoardSlotLabel position) {
        this.name = name;
        this.position = position;
    }
    
    public GameLogicController.PlayerName getName() {
        return this.name;
    }
    
    public GameLogicController.BoardSlotLabel getPosition() {
        return this.position;
    }
    
    public void setPosition(GameLogicController.BoardSlotLabel position) {
        this.position = position;
    }
}
