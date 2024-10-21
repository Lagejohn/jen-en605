
public class Suggestion {
    
    private GameLogicController.PlayerName suspect;
    private GameLogicController.BoardSlotLabel room;
    private GameLogicController.Weapon weapon;
    
    public Suggestion(GameLogicController.PlayerName suspect, GameLogicController.BoardSlotLabel room, GameLogicController.Weapon weapon) {
        this.suspect = suspect;
        this.room = room;
        this.weapon = weapon;
    }
    
    public GameLogicController.PlayerName getSuspect() {
        return this.suspect;
    }
    
    public GameLogicController.BoardSlotLabel getRoom() {
        return this.room;
    }
    
    public GameLogicController.Weapon getWeapon() {
        return this.weapon;
    }

}
