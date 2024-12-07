package obj;

import controller.GameLogicController;

public class SuspectCard extends Card {
    GameLogicController.PlayerName suspect;
    
    public SuspectCard(GameLogicController.PlayerName suspect) {
        this.suspect = suspect;
    }
    
    @Override
    public GameLogicController.PlayerName getSuspect() {
        return this.suspect;
    }

    protected String cardType() { return "Suspect card"; }

    public String getContents() { return this.suspect.name(); }

}
