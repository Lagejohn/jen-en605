package obj;

import controller.GameLogicController;

public class WeaponCard extends Card {
    GameLogicController.Weapon weapon;
    
    public WeaponCard(GameLogicController.Weapon weapon) {
        this.weapon = weapon;
    }
    
    @Override
    public GameLogicController.Weapon getWeapon() {
        return this.weapon;
    }

    protected String cardType() { return "Weapon card"; }

    public String getContents() { return this.weapon.name(); }

}
