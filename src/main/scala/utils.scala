import javax.swing.{JFrame, JPanel, JLabel}
import java.io.File
import java.awt.{Color,Graphics,BasicStroke,Font}
import java.awt.image.BufferedImage
import java.util.concurrent.TimeUnit

object Utils {

    var playersDisplayers : List[PlayerDisplayer] = List()




    def loadImage (name : String) : BufferedImage = {
        try {
            javax.imageio.ImageIO.read(getClass.getResource(name))
        }
        catch {
            case _ : Throwable => println("Issues while importing " + name); javax.imageio.ImageIO.read(getClass.getResource("Empty.png"))
        }
    }

    def findFirstOccurenceArray [T](array : Array[T], element : T) : Int = {
        var i : Int = 0
        var found : Boolean = false
        while (!found && i < array.length) {
            found = array(i) == element
            i += 1
        }
        if (found) {
            i - 1 
        } else {
            -1
        }
    }

    def cutString (s : String, charPerLine : Int) : (String, String, String) = {
        var text1 = ""
        var text2 = ""
        var text3 = ""
        if (s.length <= charPerLine) {
            text1 = s
        } else {
            var l = s.substring(0, charPerLine).lastIndexOf(" ")
            text1 = s.substring(0, l)
            text2 = s.substring(l+1)
            if (text2.length > charPerLine) {
                var s = text2
                l = s.substring(0, charPerLine).lastIndexOf(" ")
                text2 = s.substring(0, l)
                text3 = s.substring(l+1)
            }
        }
        (text1, text2, text3)
    }

    var repaintables : List[Repaintable] = List()

}

trait Repaintable {
    def repaint() : Unit
}

class Mover extends Thread {
    var lastMoveX : Int = 0
    var lastMoveY : Int = 0
    var playerdisplayer : PlayerDisplayer = EmptyPlayerDisplayer

    
    def move (moveX : Int, moveY : Int) : Unit = {
        lastMoveX = moveX
        lastMoveY = moveY
        resume
    }

    override def run : Unit = {
        interrupt()
        while (true) {
            for (i <- 0 to playerdisplayer.mapDisplayer.sizeBlock - 1) {
                playerdisplayer.changeCoordinates(lastMoveX, lastMoveY)
                TimeUnit.MILLISECONDS.sleep(100/playerdisplayer.speed)
                playerdisplayer.mapUI.pane.repaint()
            }
            playerdisplayer.isMoving = false
            interrupt()
        }
        stop()
    }
}

object Repainter extends Thread {
    override def run : Unit = {
        Utils.repaintables.foreach(x => x.repaint())
        TimeUnit.MILLISECONDS.sleep(10)
    }
}