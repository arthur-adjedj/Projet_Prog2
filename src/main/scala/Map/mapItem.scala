//These items are unique, you cannot stack them

abstract class MapItem {
    var id : Int = -1
    var name : String = "Nothing"
    var usable : Boolean = true
    def imgName : String = "Items/" + name + ".png"
    def onSelect (p : CharacterDisplayer) : Unit = {}
    def onUnselect (p : CharacterDisplayer) : Unit = {}
}

class Key (id_ : Int) extends MapItem {
    id = id_
    usable = false
    name = "Key"
}

class Bike extends MapItem {
    name = "Bike"
    override def onSelect (p : CharacterDisplayer) : Unit = {
        p.speed *= 2
    }

    override def onUnselect (p : CharacterDisplayer) : Unit = {
        p.speed /= 2
    }
}

object EmptyMapItem extends MapItem {

}