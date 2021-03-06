import java.awt.event._
import java.awt.{Color,Graphics,BasicStroke,Font}
import java.awt.event.MouseEvent
import javax.swing.{JFrame, JPanel, JLabel}
import java.io.File



class DrawPanelMap extends MyPanel {

    var mapDisplayer : MapDisplayer = EmptyMapDisplayer

    def changeMap(map : MapDisplayer) : Unit = {
        mapDisplayer = map
    }


    override def paintComponent (g : Graphics) : Unit = {
        if (ready) {
            super.paintComponent(g)
            mapDisplayer.display(g)
            DiscussionLabel.display(g)
            Utils.mapButtonList.foreach(x => x.display(g))
            endPaintComponent(g)
        }
    }

    override def onKeyPressed (e : KeyEvent) : Unit = {
        e.getKeyChar.toLower match {
            case 'z' => PlayerDisplayer.move(0, -1)
            case 's' => PlayerDisplayer.move(0, 1)
            case 'q' => PlayerDisplayer.move(-1, 0)
            case 'd' => PlayerDisplayer.move(1, 0)
            case 'e' => PlayerDisplayer.interactExplicitly
            case 'a' => PlayerDisplayer.changeCurrentItem

            case 'i' => SecondPlayerDisplayer.move(0, -1)
            case 'k' => SecondPlayerDisplayer.move(0, 1)
            case 'j' => SecondPlayerDisplayer.move(-1, 0)
            case 'l' => SecondPlayerDisplayer.move(1, 0)
            case 'o' => SecondPlayerDisplayer.interactExplicitly

            case 'p' => Utils.frame.backToPokedex
            case 'n' => if (Utils.debug) PlayerDisplayer.noClip = !PlayerDisplayer.noClip
            case _ =>
        }
    }
}