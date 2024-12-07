package obj;

import java.util.ArrayList;
import java.util.List;

import controller.GameLogicController;

public class Player {
    GameLogicController.PlayerName name;
    GameLogicController.BoardSlotLabel position;
    boolean active;
    boolean movedViaSuggestion;
    List<Card> hand;
    
    public Player(GameLogicController.PlayerName name, GameLogicController.BoardSlotLabel position, boolean active) {
        this.name = name;
        this.position = position;
        this.active = active;
        this.movedViaSuggestion = false;
        this.hand = new ArrayList<>();
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
    
    public void activate() {
        this.active = true;
    }
    
    public void makeInactive() {
        this.active = false;
    }
    
    public boolean isActive() {
        return this.active;
    }
    
    public void markMovedViaSuggestion() {
        this.movedViaSuggestion = true;
    }
    
    public void markNotMovedViaSuggestion() {
        this.movedViaSuggestion = false;
    }
    
    public boolean isMovedViaSuggestion() {
        return this.movedViaSuggestion;
    }

    public List<Card> getHand() { return this.hand; }

    public void addToHand(Card c) { this.hand.add(c); }
}
