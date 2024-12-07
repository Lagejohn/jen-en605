package obj;

import java.util.List;
import controller.GameLogicController;
// todo figure out imports to put all card classes in a folder
public abstract class Card {
    
    public GameLogicController.PlayerName getSuspect() {
        return null;
    }
    
    public GameLogicController.Weapon getWeapon() {
        return null;
    }
    
    public GameLogicController.BoardSlotLabel getRoom() {
        return null;
    }

    protected abstract String cardType();

    public abstract String getContents();



    public static String displayHand(List<Card> hand) {
        String display = "";
        for (Card c : hand) {
            display += "Card Type: " + c.cardType() + ", Item: " + c.getContents() + "\n";
        }

        return display;
    }

}
