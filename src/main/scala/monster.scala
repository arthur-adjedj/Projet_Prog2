import java.text.Normalizer
import java.lang.Math

abstract class Monster {
    var hpMax : Int = 100
    var hp : Int = 100
    var xp : Int = 0
    var level : Int = 0

    var attackStat : Int = 100
    var defenseStat : Int = 100
    var speedStat : Int = 100

    var attackBattle : Int = 100
    var defenseBattle : Int = 100
    var speedBattle : Int = 100
    var accuracyBattle : Float = 1
    var evasionBattle : Float = 1

    var alive : Boolean = true
    var wild : Boolean = false
    var monstersSeen : List[Monster] = List()

    var hpMaxPerLevel : Int = 10

    var attackStage : Int = 0
    var defenseStage : Int = 0
    var speedStage : Int = 0
    var accuracyStage : Int = 0
    var evasionStage : Int = 0

    var status : List[Status] = List()
    var attacks : Array[Attack] = Array.fill(4){EmptyAttack}

    var baseXp : Int = 1
    var xpGraph : String = "Fast"
    var previousXpStep : Int = 0
    var nextXpStep : Int = 0
    def xpRate : Float = {(xp - previousXpStep).toFloat / (nextXpStep - previousXpStep).toFloat}
    def hpRate : Float = {hp.toFloat / hpMax.toFloat}

    var monsterType : Type = EmptyType

    var name : String = ""
    var owner : Player = EmptyPlayer

    def imgNameFront : String = {"Monsters/" + originalName + "Front.png"}
    def imgNameBack : String = {"Monsters/" + originalName + "Back.png"}
    var uiYShift : Int = 0
    var originalName : String = ""
    def typeName : String = {monsterType.name}

    def enterBattle : Unit = {
        attackBattle = attackStat
        defenseBattle = defenseStat
        speedBattle = speedStat 
        accuracyBattle = 1
        evasionBattle = 1

        attackStage = 0
        defenseStage = 0
        speedStage = 0
        accuracyStage = 0
        evasionStage = 0

        monstersSeen = List()
    }

    def enterField : Unit = {
        newMonsterSeen(owner.opponent.currentMonster)
        owner.opponent.currentMonster.newMonsterSeen(this)
    }

    def newMonsterSeen (other : Monster) : Unit = {
        if (monstersSeen.forall(x => x.name != other.name) && other.name != "Empty"){
            monstersSeen = other :: monstersSeen 
        }
    }

    def newTurn : Unit = {

    }

    def endTurn : Unit = {
        endTurnStatus
    }


    def castAttack (attack : Attack, other : Monster) : Unit = {
        var random = scala.util.Random.nextFloat()
        var thisAccuracyEff = this.accuracyBattle * calcModifier(this, "accuracy")
        var otherEvasionEff = other.evasionBattle * calcModifier(other, "evasion")
        if (status.exists(x => x.name == "Freeze")) {
            DiscusionLabel.changeText(name + " cannot attack because he's frozen")
        } else if (status.exists(x => x.name == "Sleep")) {
            DiscusionLabel.changeText(name + " cannot attack because he's sleeping")
        } else if (status.exists(x => x.name == "Paralysis") && scala.util.Random.nextFloat() <= 1f/4f) {
            DiscusionLabel.changeText(name + " cannot attack because he's paralysed")
        } else if (other.status.exists(x => x.name == "Protection")) {
            DiscusionLabel.changeText(other.name + " is protected")
        } else {
            for (i <- 1 to attack.nOfHits){
                if (other.alive) {
                    random = scala.util.Random.nextFloat()
                    if (status.exists(x => x.name == "Confusion") && random <= 0.5) {
                        this.receiveAttack(attack, this)
                        attack.cast(other, this)
                    } else if (random <= attack.accuracy*thisAccuracyEff*otherEvasionEff) {
                        DiscusionLabel.changeText(name + " casts " + attack.name)
                        attack.cast(this, other)
                        other.receiveAttack(attack, this)
                    } else {
                        if (random <= attack.accuracy) {
                            DiscusionLabel.changeText(attack.name + " missed")
                        } else if (random <= attack.accuracy*thisAccuracyEff) {
                            DiscusionLabel.changeText(name + " missed his attack")
                        } else {
                            DiscusionLabel.changeText(other.name + " dodged")
                        }
                    }
                }
            }
        }
    }

    def calcModifier (monster : Monster, stat : String) : Float = {
        var stage = {stat match {
            case "attack" => monster.attackStage
            case "defense" => monster.defenseStage
            case "speed" => monster.speedStage
            case "accuracy" => monster.accuracyStage
            case "evasion" => monster.evasionStage
        }}

        {stage match {
            case -6 => 25f/100f
            case -5 => 28f/100f
            case -4 => 33f/100f
            case -3 => 40f/100f
            case -2 => 50f/100f
            case -1 => 66f/100f
            case 0 => 100f/100f
            case 1 => 150f/100f
            case 2 => 200f/100f
            case 3 => 250f/100f
            case 4 => 300f/100f
            case 5 => 350f/100f
            case 6 => 400f/100f
        }}

    }

    def receiveAttack (attack : Attack, other : Monster) : Unit = {
        
        var otherAttackEff = other.attackBattle * calcModifier(other, "attack")
        var thisDefenseEff = defenseBattle * calcModifier(this, "defense")

        var random = scala.util.Random.nextFloat()*38f/255f + 217f/255f
        var damage = ((((2f/5f*other.level.toFloat+2f)*attack.power.toFloat*otherAttackEff.toFloat/thisDefenseEff.toFloat)/50f+2f)*random*attack.attackType.multDamage(other.monsterType)).toInt

        takeDamage(damage)
        
    }

    def receiveStatus (stat : Status) : Unit = {
        def max_duration (s : Status, name : String) : Unit = {
            if (s.name == name) {
                s.durationLeft = s.duration
            }
        }

        var exists = status.exists(x => x.name == stat.name)
        if (exists) {
            status.foreach(x => max_duration(x, stat.name))
        } else {
            status = stat :: status
            println(name + " has the status " + stat.name)
            stat.onAdd(this)
        }
    }

    def removeStatus (stat : Status) : Unit = {
        status = status.filter(x => x.name != stat.name)
    }

    def endTurnStatus : Unit = {
        status.foreach(x => x.onEndTurn(this))
        status = status.filter(x => x.durationLeft != 0)
    }

    def heal (amount : Int) : Unit = {
        hp += amount
        if (hp > hpMax) {
            hp = hpMax
        }
    }

    def takeDamage (amount : Int) : Unit = {
        hp -= amount
        if (hp <= 0) {
            hp = 0
            die
        }
    }

    def die : Unit = {
        DiscusionLabel.changeText(name + " died !")
        alive = false
        var monstersSeenAlive = monstersSeen.filter(x => x.alive && x.name != "Empty")
        var exp : Float = baseXp.toFloat*level.toFloat/7f/monstersSeenAlive.length.toFloat
        if (!wild) {
            exp *= 3f/2f
        }
        monstersSeenAlive.foreach(x => x.gainXp(exp.toInt))
        if (owner.team.forall(x => (!x.alive) || (x.name == "Empty"))) {
            owner.lose
        }
        //owner.changeMonster

    }

    def gainXp (amount : Int) : Unit = {
        xp += amount
        if (xp >= nextXpStep) {
            var diff = xp - nextXpStep
            xp = nextXpStep
            levelUp
            gainXp(diff)
        }
    }

    def levelUp : Unit = {
        if (level != 0) {
            hpMax += hpMaxPerLevel
        }
        hp = hpMax
        level += 1
        previousXpStep = nextXpStep
        xpGraph match {
            case "Fast" => nextXpStep = (0.8 * Math.pow(level+1, 3)).toInt
            case "Medium Fast" => nextXpStep = (Math.pow(level+1, 3)).toInt
            case "Medium Slow" => nextXpStep = (1.2 * Math.pow(level+1, 3) - 15 * Math.pow(level+1, 2) + 100 * (level+1) - 140).toInt
            case "Slow" => nextXpStep = (1.25 * Math.pow(level+1, 3)).toInt
        }
        if (level > 1) {
            DiscusionLabel.changeText(name + " is now level " + level)
        }
    }

    def gainLvl (n : Int) : Unit = {
        for (i <- 1 to n) {
            gainXp(nextXpStep - xp)
        }
    }

    override def toString : String = {
        name + " is a " + originalName + " monster of type " + typeName
    }

    levelUp

}




class Pikachu extends Monster {
    hpMax = 35
    hp = 35
    attackStat = 55
    defenseStat = 40
    speedStat = 90

    xpGraph = "Medium Fast"
    baseXp = 112

    monsterType = Electric
    name = "Pikachuuuu"
    originalName = "Pikachu"
    attacks(0) = QuickAttack
    attacks(1) = DoubleSlap
    attacks(2) = Thunder
    attacks(3) = EmptyAttack
}

class Squirtle extends Monster {
    hpMax = 44
    hp = 44
    attackStat = 48
    defenseStat = 65
    speedStat = 50

    xpGraph = "Medium Slow"
    baseXp = 63

    monsterType = Water
    name = "Carapuuuce"
    originalName = "Squirtle"

    attacks(0) = QuickAttack
    attacks(1) = AquaTail

    uiYShift = 18

}


class Bulbasaur extends Monster {
    hpMax = 45
    hp = 45
    attackStat = 49
    defenseStat = 49
    speedStat = 45

    xpGraph = "Medium Slow"
    baseXp = 64

    monsterType = Grass
    name = "Bulbizaare"
    originalName = "Bulbasaur"

    attacks(0) = QuickAttack
    uiYShift = 23
}

class Charmander extends Monster {
    hpMax = 39
    hp = 39
    attackStat = 52
    defenseStat = 43
    speedStat = 65

    xpGraph = "Medium Slow"
    baseXp = 62

    monsterType = Fire
    name = "Saalameche"
    originalName = "Charmander"

    attacks(0) = QuickAttack

    uiYShift = 18
}

class Rattata extends Monster {
    hpMax = 30
    hp = 30
    attackStat = 56
    defenseStat = 35
    speedStat = 72

    xpGraph = "Medium Fast"
    baseXp = 51

    monsterType = Normal
    name = "Ratatata"
    originalName = "Rattata"

    attacks(0) = QuickAttack
    uiYShift = 15

}

object EmptyMonster extends Monster {
    name = "Empty"
    originalName = "Empty"
    alive = false
}

