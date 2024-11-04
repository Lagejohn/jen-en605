
public class SuspectCard extends Card {
    GameLogicController.PlayerName suspect;
    
    public SuspectCard(GameLogicController.PlayerName suspect) {
        this.suspect = suspect;
    }
    
    @Override
    public GameLogicController.PlayerName getSuspect() {
        return this.suspect;
    }

}
