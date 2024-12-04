
public class WeaponCard extends Card {
    GameLogicController.Weapon weapon;
    
    public WeaponCard(GameLogicController.Weapon weapon) {
        this.weapon = weapon;
    }
    
    @Override
    public GameLogicController.Weapon getWeapon() {
        return this.weapon;
    }

}
