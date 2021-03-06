import java.io.File
import java.awt.{Color,Graphics,BasicStroke,Font}
import java.util.concurrent.TimeUnit
import javax.swing.{JFrame, JPanel, JLabel}

abstract class MyButton (imageName_ : String) extends Object with Descriptable {

    var originalImageName = imageName_
    var previousImageName = "" // to avoid loading the same image twice
    var imageName = imageName_
    var x : Int = 0
    var y : Int = 0
    var width : Int = 300
    var height : Int = 136
    var image = Utils.loadImage(imageName)
    var visible : Boolean = false
    var alwaysVisible : Boolean = false
    context = "" // the value of Utils.frame.currentState to decide if we display
    var clickable : Boolean = true
    var poke_font : Font = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("PokemonPixelFont.ttf"))
    poke_font = poke_font.deriveFont(Font.PLAIN,30)
    var text : String = ""
    var xtext  = 0
    var ytext = 0
    def display (g : Graphics) : Unit = {
        if (visible && (context == Utils.frame.currentState || context == "All") || alwaysVisible) {
            g.setFont(poke_font)
            var metrics = g.getFontMetrics(poke_font);
            // Coordonées du texte
            xtext = x + (width - metrics.stringWidth(text)) / 2;
            ytext = y + ((height - metrics.getHeight()) / 2) + metrics.getAscent();
            g.drawImage(image, x, y, null)
            g.drawString(text,xtext,ytext)
        }
    }

    def onClick (xClick : Int, yClick : Int) : Boolean = {
        if ((visible && clickable || alwaysVisible) && x <= xClick && xClick <= (x + width) && y <= yClick && yClick <= (y + height) && 
              (context == Utils.frame.currentState || context == "All")) {
            isClicked
            true // tells the listener that the button is clicked
        } else {
            false
        }
    }

    def isClicked : Unit = {
        // function called when the button is clicked
        Utils.print("The button is clicked")
    }

    def setVisible(b : Boolean) : Unit = {
        visible = b
        update
    }

    def update : Unit = {
        clickable = visible
    }

    override def isMouseOver (x_click : Int, y_click : Int) : Boolean = {
        visible && x <= x_click && x_click <= (x + width) && y <= y_click && y_click <= (y + height) && clickable
    }
}

abstract class InvisibleButton extends MyButton("Empty.png") {
    alwaysVisible = true
    visible = true
    var showOnDebug : Boolean = false
    override def display (g : Graphics) : Unit = {
        if (Utils.debug && showOnDebug && (context == Utils.frame.currentState || context == "All")) {
            g.drawRect(x, y, width, height)
        }
    }
}

abstract class BattleButton (imageName_ : String) extends MyButton(imageName_) {
    context = "Battle"
    def resetOnMainMenu : Unit = {
        Utils.battleButtonList.foreach(x => x.setVisible(false))
        Utils.battleMenuButtonList.foreach(x => x.setVisible(true))
    }
}

abstract class PokedexButton extends InvisibleButton {
    context = "Pokedex"
}

abstract class MapButton (imageName_ : String) extends MyButton(imageName_) {
    context = "Map"
    def resetOnMainMenu : Unit = {
        Utils.mapButtonList.foreach(x => x.setVisible(false))
        Utils.mapMenuButtonList.foreach(x => x.setVisible(true))
    }
}

class ChoiceButton (n_ : Int) extends MyButton("Buttons/EmptyButton.png") {
    context = "Choice"
    var n : Int = n_
    var newPos = Utils.buttonPosition(n)
    x = newPos._1
    y = newPos._2

    override def isClicked : Unit = {
        Utils.makeChoice(n)
    }

    override def update : Unit = {
        Utils.choiceType match {
            case "Pokemon" => {visible = true; clickable = PlayerDisplayer.player.team(n).name != "Empty"; text = PlayerDisplayer.player.team(n).name;
                               imageName = PlayerDisplayer.player.team(n).monsterType.imageButtonName}
            case "Pokemon Slot" => {visible = true; clickable = true; text = PlayerDisplayer.player.team(n).name;
                               imageName = PlayerDisplayer.player.team(n).monsterType.imageButtonName}
            case "Yes No" => if (n == 0) {visible = true; clickable = true; text = "No"; imageName = "Buttons/AttackButton.png"} 
                             else if (n == 1) {visible = true; clickable = true; text = "Yes"; imageName = "Buttons/GrassButton.png"}
                             else {visible = false; clickable = false}
            case _ => {visible = false; clickable = false} 
        }
        image = Utils.loadImage(imageName)
        
    }

}

object CloseButton extends MyButton ("Buttons/Close.png") {
    context = "All"
    x = 594
    y = 2
    width = 15
    height = 15
    alwaysVisible = true
    override def isClicked : Unit = {
        Utils.frame.close
    }
}

object HelpButton extends MyButton ("Buttons/Help.png") {
    context = "All"
    x = 0
    y = 0
    width = 15
    height = 15
    alwaysVisible = true
    override def isClicked : Unit = {
        Utils.frame.help
    }
}

class CastAttackButton (n_ : Int) extends BattleButton ("Buttons/EmptyButton.png") {
    var n : Int = n_
    var newPos = Utils.buttonPosition(n)
    x = newPos._1
    y = newPos._2
    override def isClicked : Unit = {
        if (Player.chooseAttack(n)) {
            resetOnMainMenu
        }
    }

    override def display (g : Graphics) : Unit = {
        if (visible && (context == Utils.frame.currentState || context == "All") || alwaysVisible) {
            g.setFont(poke_font)
            var metrics = g.getFontMetrics(poke_font);
            // Coordonées du texte
            xtext = x + (width - metrics.stringWidth(text)) / 2;
            ytext = y + ((height - metrics.getHeight()) / 2) + metrics.getAscent() - 8;
            g.drawImage(image, x, y, null)
            g.drawString(text,xtext,ytext)
            if (Player.currentMonster.attacks(n).name != "Empty") {
                g.drawImage(Utils.typeIcons(Utils.typeIconNumber(Player.currentMonster.attacks(n).attackType)), x + width/2 - 61, y + height - 62, null)
            }
        }
    }

    override def update : Unit = {
        if (Player.currentMonster.attacks(n).name != "Empty") {
            imageName = Player.currentMonster.attacks(n).attackType.imageButtonName
            text = Player.currentMonster.attacks(n).name
            clickable = visible
        } else {
            imageName = "Buttons/EmptyButton.png"
            text = "Empty"
            clickable = false
        }
        if (previousImageName != imageName) {
            previousImageName = imageName
            image = Utils.loadImage(imageName)
        }
    }

    override def onMouseOver (g : Graphics, x : Int, y : Int, width : Int, height : Int) : Unit = {
        var metrics = g.getFontMetrics
        var text = Player.currentMonster.attacks(n).toString
        var liString = Utils.cutString(text, 30)

        var xToShow = x.max(20)
        var yToShow = y - 10 - 20*liString.length

        
        (0 until liString.length).foreach(x => xToShow = xToShow.min(width - metrics.stringWidth(liString(x)) - 20))

        (0 until liString.length).foreach(x => g.drawString(liString(x), xToShow, yToShow + 20*x))
    }
}

class ChangeMonsterButton (n_ : Int) extends BattleButton ("Buttons/EmptyButton.png") {
    var n : Int = n_
    var newPos = Utils.buttonPosition(n)
    x = newPos._1
    y = newPos._2


    override def isClicked : Unit = {
        if (Player.changeMonster(n)) {
            resetOnMainMenu
        }        
    }

    override def isMouseOver (x_click : Int, y_click : Int) : Boolean = {
        visible && x <= x_click && x_click <= (x + width) && y <= y_click && y_click <= (y + height) && (Player.team(n).name != "Empty") 
    }

    override def onMouseOver (g : Graphics, x : Int, y : Int, width : Int, height : Int) : Unit = {
        var metrics = g.getFontMetrics
        var text = Player.team(n).toString
        var liString = Utils.cutString(text, 30)

        var xToShow = x.max(20)
        var yToShow = y - 10 - 20*liString.length

        
        (0 until liString.length).foreach(x => xToShow = xToShow.min(width - metrics.stringWidth(liString(x)) - 20))

        (0 until liString.length).foreach(x => g.drawString(liString(x), xToShow, yToShow + 20*x))
    }

    override def update : Unit = {
        if (Player.team(n).alive && Player.team(n).name != "Empty") {
            imageName = Player.team(n).monsterType.imageButtonName
            clickable = visible
        } else {
            imageName = "Buttons/EmptyButton.png"
            clickable = false
        }
        text = Player.team(n).name
        if (previousImageName != imageName) {
            previousImageName = imageName
            image = Utils.loadImage(imageName)
        }
    }

    override def display (g : Graphics) : Unit = {
        if (visible && (context == Utils.frame.currentState || context == "All") || alwaysVisible) {
            g.setFont(poke_font)
            var metrics = g.getFontMetrics(poke_font);
            // Coordonées du texte
            xtext = x + (width - metrics.stringWidth(text)) / 2;
            ytext = y + ((height - metrics.getHeight()) / 2) + metrics.getAscent() - (if (Player.team(n).name != "Empty") 8 else 0);
            g.drawImage(image, x, y, null)
            g.drawString(text,xtext,ytext)
            if (Player.team(n).name != "Empty") {
                g.drawImage(Utils.typeIcons(Utils.typeIconNumber(Player.team(n).monsterType)), x + width/2 - 61, y + height - 62, null)
            }
        }
    }
}

class UseItemButton (n_ : Int) extends BattleButton ("Buttons/EmptyButton.png") {
    var n : Int = n_
    var indexOfObject : Int = 0
    var newPos = Utils.buttonPosition(n)
    x = newPos._1
    y = newPos._2

    override def isClicked : Unit = {
        if (Player.useItem(indexOfObject)) {
            resetOnMainMenu
        }        
    }

    override def onMouseOver (g : Graphics, xMouse : Int, yMouse : Int, widthWindow : Int, heightWindow : Int) : Unit = {
        var metrics = g.getFontMetrics
        var text = Player.usableInventory(n).toString


        var liString = Utils.cutString(text, 30)

        var xToShow = xMouse.max(20)
        var yToShow = yMouse - 10 - 20*liString.length

        
        (0 until liString.length).foreach(x => xToShow = xToShow.min(widthWindow - metrics.stringWidth(liString(x)) - 20))

        (0 until liString.length).foreach(x => g.drawString(liString(x), xToShow, yToShow + 20*x))
    }

    override def update : Unit = {
        indexOfObject = 4*NextPageItemButton.currentPage + n
        if (Player.usableInventory(indexOfObject).name != "Empty" && Player.usableInventory(indexOfObject).usable) {
            imageName = "Buttons/BagButton.png"
            clickable = visible
        } else {
            imageName = "Buttons/EmptyButton.png"
            clickable = false
        }
        text = Player.usableInventory(indexOfObject).name + "(" + Player.usableInventory(indexOfObject).amount + ")"
        if (previousImageName != imageName) {
            previousImageName = imageName
            image = Utils.loadImage(imageName)
        }
    }
}

class ChoosePokemonPokedexButton (n_ : Int) extends PokedexButton {
    var n : Int = n_
    x = 322
    y = 82 + 38*n
    width = 231
    height = 39
    showOnDebug = false

    override def isClicked : Unit = {
        if (Utils.frame.pokedexPane.ready) {
            Utils.frame.pokedexPane.changeCurrentPokemon(n)
        }
    }
}

class MoveListPokedexButton (n_ : Int) extends PokedexButton {
    var n : Int = n_
    x = 561
    y = 256 + n * 179 
    //y = 435 // for -1 it's 77
    width = 49
    height = 36
    showOnDebug = true

    override def isClicked : Unit = {
        if (Utils.frame.pokedexPane.ready) {
            Utils.frame.pokedexPane.moveList(n)
        }
    }
}

object AttackButton extends BattleButton ("Buttons/AttackButton.png") {
    var newPos = Utils.buttonPosition(0)
    x = newPos._1
    y = newPos._2
    visible = true
    text = "Attack"

    override def isClicked : Unit = {


        AttackButton.setVisible(false)
        BagButton.setVisible(false)
        MonsterButton.setVisible(false)
        RunButton.setVisible(false)

        Utils.castAttackButtonList.foreach(x => x.setVisible(true))

        BackAttackButton.setVisible(true)
    }

    override def update : Unit = {
        if (Player.currentMonster.alive) {
            imageName = originalImageName
            clickable = visible
        } else {
            imageName = "Buttons/EmptyButton.png"
            clickable = false
        }
        if (previousImageName != imageName) {
            previousImageName = imageName
            image = Utils.loadImage(imageName)
        }
    }
}


object BagButton extends BattleButton ("Buttons/BagButton.png") {
    var newPos = Utils.buttonPosition(1)
    x = newPos._1
    y = newPos._2
    visible = true
    text = "Bag"


    override def isClicked : Unit = {
        AttackButton.setVisible(false)
        BagButton.setVisible(false)
        MonsterButton.setVisible(false)
        RunButton.setVisible(false)

        Utils.useItemButtonList.foreach(x => x.setVisible(true))
        NextPageItemButton.setVisible(true)

        NextPageItemButton.currentPage = 0

        BackBagButton.setVisible(true)
    }

    override def update : Unit = {
        if (Player.currentMonster.alive && Player.usableInventory.filter(x => x.name != "Empty").length > 0) {
            imageName = originalImageName
            clickable = visible
        } else {
            imageName = "Buttons/EmptyButton.png"
            clickable = false
        }
        if (previousImageName != imageName) {
            previousImageName = imageName
            image = Utils.loadImage(imageName)
        }
    }
}


object MonsterButton extends BattleButton ("Buttons/MonstersButton.png") {
    var newPos = Utils.buttonPosition(2)
    x = newPos._1
    y = newPos._2
    visible = true
    text = "Pokemons"

    override def isClicked : Unit = {

        AttackButton.setVisible(false)
        BagButton.setVisible(false)
        MonsterButton.setVisible(false)
        RunButton.setVisible(false)

        Utils.changeMonsterButtonList.foreach(x => x.setVisible(true))
    }

    override def update : Unit = {
        if (Player.team.exists(x => (x != Player.currentMonster) && (x.name != "Empty") && (x.alive))) {
            imageName = originalImageName
            clickable = visible
        } else {
            imageName = "Buttons/EmptyButton.png"
            clickable = false
        }
        if (previousImageName != imageName) {
            previousImageName = imageName
            image = Utils.loadImage(imageName)
        }
    }

}


object RunButton extends BattleButton ("Buttons/RunButton.png") {
    var newPos = Utils.buttonPosition(3)
    x = newPos._1
    y = newPos._2
    visible = true
    text = "Run"

    override def isClicked : Unit = {
        DiscussionLabel.changeText("You run, far, really far !")
        Player.runningAway = true
        Player.lose
    }

    override def update : Unit = {
        if (Player.currentMonster.alive && Player.opponent.name == "Wild") {
            imageName = originalImageName
            clickable = visible
        } else {
            imageName = "Buttons/EmptyButton.png"
            clickable = false
        }
        if (previousImageName != imageName) {
            previousImageName = imageName
            image = Utils.loadImage(imageName)
        }
    }
}

object BackAttackButton extends BattleButton ("Buttons/AttackButton.png") {
    x = 154
    y = 693
    visible = false
    text = "Back"

    override def isClicked : Unit = {
       resetOnMainMenu
    }
}

object BackBagButton extends BattleButton ("Buttons/AttackButton.png") {
    x = 3
    y = 693
    visible = false
    text = "Back"

    override def isClicked : Unit = {
       resetOnMainMenu
    }
}


object NextPageItemButton extends BattleButton ("Buttons/EmptyButton.png") {
    var newPos = Utils.buttonPosition(5)
    x = newPos._1
    y = newPos._2
    text = "Next"
    var currentPage = 0
    override def isClicked : Unit = {
        var numberOfPage : Int = 1 + Player.usableInventory.filter(x => x.name != "Empty").length / 4
        currentPage = (currentPage + 1) % numberOfPage
    }

    override def update : Unit = {
        if (Player.usableInventory.filter(x => x.name != "Empty").length > 4) {
            imageName = "Buttons/AttackButton.png"
            clickable = visible
        } else {
            imageName = "Buttons/EmptyButton.png"
            clickable = false
        }
        if (previousImageName != imageName) {
            previousImageName = imageName
            image = Utils.loadImage(imageName)
        }
    }
}

object ShowPokedexButton extends MapButton ("Buttons/AttackButton.png") {
    var newPos = Utils.buttonPosition(0)
    x = newPos._1
    y = newPos._2
    visible = true
    text = "Pokedex"

    override def isClicked : Unit = {
        Utils.frame.backToPokedex
    }
}

object ShowInventoryButton extends MapButton ("Buttons/BagButton.png") {
    var newPos = Utils.buttonPosition(1)
    x = newPos._1
    y = newPos._2
    visible = true
    text = "Bag"

    override def isClicked : Unit = {
        DiscussionLabel.changeText("This functionnality is not implemented yet !")
    }
}

object ShowTeamButton extends MapButton ("Buttons/MonstersButton.png") {
    var newPos = Utils.buttonPosition(2)
    x = newPos._1
    y = newPos._2
    visible = true
    text = "Pokemons"

    override def isClicked : Unit = {
        Utils.mapButtonList.foreach(x => x.setVisible(false))
        Utils.showPokemonMapButtonList.foreach(x => x.setVisible(true))
    }
}

object SaveButton extends MapButton ("Buttons/RunButton.png") {
    var newPos = Utils.buttonPosition(3)
    x = newPos._1
    y = newPos._2
    visible = true
    text = "Save"

    override def isClicked : Unit = {
        DiscussionLabel.changeText("The game is saved ! Press 5 to load it !" )
        Utils.save

    }
}

object OptionsButton extends MapButton ("Buttons/IceButton.png") {
    var newPos = Utils.buttonPosition(4)
    x = newPos._1
    y = newPos._2
    visible = true
    text = "Options"

    override def isClicked : Unit = {
        DiscussionLabel.changeText("This functionnality is not implemented yet !")
    }
}

object TrainerButton extends MapButton ("Buttons/FireButton.png") {
    var newPos = Utils.buttonPosition(5)
    x = newPos._1
    y = newPos._2
    visible = true
    text = Player.name

    override def isClicked : Unit = {
        DiscussionLabel.changeText("This functionnality is not implemented yet !")
        //Utils.askChoice("Pokemon")
    }
}


class PokemonMapButton (n_ : Int) extends MapButton ("Buttons/EmptyButton.png") {
    var n : Int = n_
    var newPos = Utils.buttonPosition(n)
    x = newPos._1
    y = newPos._2

    override def isClicked : Unit = {
        if (Player.team(n).talk) {
            resetOnMainMenu
        }
    }

    override def update : Unit = {
        if (Player.team(n).name != "Empty") {
            imageName = Player.team(n).monsterType.imageButtonName
            clickable = visible
        } else {
            imageName = "Buttons/EmptyButton.png"
            clickable = false
        }
        text = Player.team(n).name
        if (previousImageName != imageName) {
            previousImageName = imageName
            image = Utils.loadImage(imageName)
        }
    }

    override def display (g : Graphics) : Unit = {
        if (visible && (context == Utils.frame.currentState || context == "All") || alwaysVisible) {
            g.setFont(poke_font)
            var metrics = g.getFontMetrics(poke_font);
            // Coordonées du texte
            xtext = x + (width - metrics.stringWidth(text)) / 2;
            ytext = y + ((height - metrics.getHeight()) / 2) + metrics.getAscent() - (if (Player.team(n).name != "Empty") 8 else 0);
            g.drawImage(image, x, y, null)
            g.drawString(text,xtext,ytext)
            if (Player.team(n).name != "Empty") {
                g.drawImage(Utils.typeIcons(Utils.typeIconNumber(Player.team(n).monsterType)), x + width/2 - 61, y + height - 62, null)
            }
        }
    }
}