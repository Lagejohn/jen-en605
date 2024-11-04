
public class Suggestion {
    
    private SuspectCard suspect;
    private WeaponCard weapon;
    private RoomCard room;
    
    public Suggestion(SuspectCard suspect, WeaponCard weapon, RoomCard room) {
        this.suspect = suspect;
        this.room = room;
        this.weapon = weapon;
    }
    
    public SuspectCard getSuspectCard() {
        return this.suspect;
    }
    
    public WeaponCard getWeaponCard() {
        return this.weapon;
    }
    
    public RoomCard getRoomCard() {
        return this.room;
    }

}
