import java.text.Normalizer
import java.lang.Math
import javax.management.Descriptor
import java.awt.image.BufferedImage

abstract class Monster {
    var xp : Int = 0
    var level : Int = 0

    var baseHpStat : Int = 1
    var baseAttackStat : Int = 1
    var baseDefenseStat : Int = 1
    var baseSpeedStat : Int = 1
    var hpMax : Int = 100
    var hp : Int = 100

    var IVHp : Int = scala.util.Random.nextInt(16)
    var IVAttack : Int = scala.util.Random.nextInt(16)
    var IVDefense : Int = scala.util.Random.nextInt(16)
    var IVSpeed : Int = scala.util.Random.nextInt(16)

    var EVHp : Int = 0
    var EVAttack : Int = 0
    var EVDefense : Int = 0
    var EVSpeed : Int = 0

    

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
        status = List()
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

    def getSpeed : Int = {
        (speedBattle.toFloat * calcModifier(this, "speed")).toInt
    }


    def castAttack (attack : Attack, other : Monster) : Unit = {
        var random = scala.util.Random.nextFloat()
        var thisAccuracyEff = this.accuracyBattle * calcModifier(this, "accuracy")
        var otherEvasionEff = other.evasionBattle * calcModifier(other, "evasion")
        if (status.exists(x => x.name == "Freeze")) {
            DiscussionLabel.changeText(name + " cannot attack because he's frozen")
        } else if (status.exists(x => x.name == "Sleep")) {
            DiscussionLabel.changeText(name + " cannot attack because he's sleeping")
        } else if (status.exists(x => x.name == "Paralysis") && scala.util.Random.nextFloat() <= 1f/4f) {
            DiscussionLabel.changeText(name + " cannot attack because he's paralysed")
        } else if (other.status.exists(x => x.name == "Protection")) {
            DiscussionLabel.changeText(other.name + " is protected")
        } else {
            for (i <- 1 to attack.nOfHits){
                if (other.alive) {
                    random = scala.util.Random.nextFloat()
                    if (status.exists(x => x.name == "Confusion") && random <= 0.5) {
                        this.receiveAttack(attack, this)
                        attack.cast(this, this)
                    } else if (random <= attack.accuracy*thisAccuracyEff*otherEvasionEff) {
                        DiscussionLabel.changeText(name + " casts " + attack.name)
                        attack.cast(this, other)
                        other.receiveAttack(attack, this)
                    } else {
                        if (random <= attack.accuracy) {
                            DiscussionLabel.changeText(attack.name + " missed")
                        } else if (random <= attack.accuracy*thisAccuracyEff) {
                            DiscussionLabel.changeText(name + " missed his attack")
                        } else {
                            DiscussionLabel.changeText(other.name + " dodged")
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
        var handledDamages = attack.handlesDamages(other, this)
        if (handledDamages != -1) {
            takeDamage(handledDamages)
        } else {
            var otherAttackEff = other.attackBattle * calcModifier(other, "attack")
            var thisDefenseEff = defenseBattle * calcModifier(this, "defense")

            var random = scala.util.Random.nextFloat()*38f/255f + 217f/255f
            var damage = ((((2f/5f*other.level.toFloat+2f)*attack.power.toFloat*otherAttackEff.toFloat/thisDefenseEff.toFloat)/50f+2f)*random*attack.attackType.multDamage(this.monsterType)).toInt

            takeDamage(damage)
        } 
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
        DiscussionLabel.changeText(name + " is KO !")
        alive = false
        var monstersSeenAlive = monstersSeen.filter(x => x.alive && x.name != "Empty" && x.level < 100)
        var exp : Float = baseXp.toFloat*level.toFloat/7f/monstersSeenAlive.length.toFloat
        if (!wild) {
            exp *= 3f/2f
        }
        monstersSeenAlive.foreach(x => x.gainXp(exp.toInt))
        monstersSeenAlive.foreach(x => x.EVHp = (x.EVHp + baseHpStat).min(65535))
        monstersSeenAlive.foreach(x => x.EVAttack = (x.EVAttack + baseAttackStat).min(65535))
        monstersSeenAlive.foreach(x => x.EVDefense = (x.EVDefense + baseDefenseStat).min(65535))
        monstersSeenAlive.foreach(x => x.EVSpeed = (x.EVSpeed + baseSpeedStat).min(65535))
        if (owner.team.forall(x => (!x.alive) || (x.name == "Empty"))) {
            owner.lose
        }
        owner.changeMonster

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
        level += 1

        var previousHpMax = hpMax
        hpMax = (((baseHpStat + IVHp) * 2 + (Math.sqrt(EVHp)/4f).toInt) * level)/100 + level + 10
        if (level == 1) {
            hp = hpMax
        } else {
            heal(hpMax - previousHpMax)
        }
        attackStat = (((baseAttackStat + IVAttack) * 2 + (Math.sqrt(EVAttack)/4f).toInt) * level)/100 + 5
        defenseStat = (((baseDefenseStat + IVDefense) * 2 + (Math.sqrt(EVDefense)/4f).toInt) * level)/100 + 5
        speedStat = (((baseSpeedStat + IVSpeed) * 2 + (Math.sqrt(EVSpeed)/4f).toInt) * level)/100 + 5



        previousXpStep = nextXpStep
        xpGraph match {
            case "Fast" => nextXpStep = (0.8 * Math.pow(level+1, 3)).toInt
            case "Medium Fast" => nextXpStep = (Math.pow(level+1, 3)).toInt
            case "Medium Slow" => nextXpStep = (1.2 * Math.pow(level+1, 3) - 15 * Math.pow(level+1, 2) + 100 * (level+1) - 140).toInt
            case "Slow" => nextXpStep = (1.25 * Math.pow(level+1, 3)).toInt
        }
        if (level > 1) {
            DiscussionLabel.changeText(name + " is now level " + level)
        }
    }

    def gainLvl (n : Int) : Unit = {
        for (i <- 1 to n) {
            gainXp(nextXpStep - xp)
        }
    }

    override def toString : String = {
        name + " is a " + originalName + " monster of type " + typeName + ". "
    }

    levelUp

}


class Pikachu extends Monster {
    baseHpStat = 35
    baseAttackStat = 55
    baseDefenseStat = 40
    baseSpeedStat = 90

    xpGraph = "Medium Fast"
    baseXp = 112

    monsterType = Electric
    name = "Pikachu"
    originalName = "Pikachu"
    attacks(0) = QuickAttack
    attacks(1) = DoubleSlap
    attacks(2) = ThunderWave
    attacks(3) = Thunder

}

class Squirtle extends Monster {
    baseHpStat = 44
    baseAttackStat = 48
    baseDefenseStat = 65
    baseSpeedStat = 50

    xpGraph = "Medium Slow"
    baseXp = 63

    monsterType = Water
    name = "Carapuuuce"
    originalName = "Squirtle"

    attacks(0) = Tackle
    attacks(1) = WaterGun
    attacks(2) = AquaTail
    attacks(3) = TailWhip

    uiYShift = 18

}


class Bulbasaur extends Monster {
    baseHpStat = 45
    baseAttackStat = 49
    baseDefenseStat = 49
    baseSpeedStat = 45

    xpGraph = "Medium Slow"
    baseXp = 64

    monsterType = Grass
    name = "Bulbizare"
    originalName = "Bulbasaur"

    attacks(0) = Growl
    attacks(1) = Tackle
    attacks(2) = VineWhip
    attacks(3) = Growth

    uiYShift = 23
}

class Charmander extends Monster {
    baseHpStat = 39
    baseAttackStat = 52
    baseDefenseStat = 43
    baseSpeedStat = 65

    xpGraph = "Medium Slow"
    baseXp = 62

    monsterType = Fire
    name = "Salameche"
    originalName = "Charmander"

    attacks(0) = Growl
    attacks(1) = Scratch
    attacks(2) = Ember
    attacks(3) = Flamethrower
    uiYShift = 18
}

class Rattata extends Monster {
    baseHpStat = 30
    baseAttackStat = 56
    baseDefenseStat = 35
    baseSpeedStat = 72

    xpGraph = "Medium Fast"
    baseXp = 51

    monsterType = Normal
    name = "Ratatata"
    originalName = "Rattata"

    attacks(0) = Tackle
    attacks(1) = TailWhip
    attacks(2) = QuickAttack
    attacks(3) = Crunch
    uiYShift = 15

}

object EmptyMonster extends Monster {
    name = "Empty"
    originalName = "Empty"
    alive = false
}
