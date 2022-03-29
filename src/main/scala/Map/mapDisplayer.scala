import java.awt.Graphics

class MapDisplayer (frame : UI) {

    var x : Int = 0
    var y : Int = -25
    var n : Int = -1 // when there will be several maps

    var iStart : Int = 0
    var jStart : Int = 0

    var sizeMap : Int = 60
    var grid : Array[Array[List[Block]]] = Array.fill(sizeMap)(Array.fill(sizeMap)(List(new EmptyBlock)))

    var ui : UI = frame
    var sizeBlock : Int = 0

    var imgName : String = "Maps/1.png"
    var img = Utils.loadImage(imgName)

    var menuBackground = Utils.loadImage("/Maps/MenuBackground.png")

    def getWildMonster : Monster = new Rattata
    def getWildLevel : Int = 1



    def initialise (sizeB : Int, i : Int = -1, j : Int = -1) : Unit = {
        PlayerDisplayer.mapDisplayer = this
        sizeBlock = ui.sizeBlock
        for (i <- 0 to sizeMap - 1) {
            for (j <- 0 to sizeMap - 1) {
                grid(i)(j) foreach (b => b.initialise(i, j))
            }
        }
        if (i == -1 && j == -1) {
            PlayerDisplayer.i = iStart
            PlayerDisplayer.j = jStart
        } else {
            PlayerDisplayer.i = i
            PlayerDisplayer.j = j
        }
        PlayerDisplayer.alignCoordinates
        PlayerDisplayer.whichMap = n

        if (Utils.mapDisplayers(n-1) != EmptyMapDisplayer) {
            Utils.mapDisplayers(n-1) = this
        }

        PlayerDisplayer.initialise
        // we set this coordinates of the map so the player is in the middle of its box
        x = PlayerDisplayer.x - PlayerDisplayer.leftBox - (((PlayerDisplayer.rightBox - PlayerDisplayer.leftBox)/2)/sizeBlock)*sizeBlock
        y = PlayerDisplayer.y - PlayerDisplayer.topBox - (((PlayerDisplayer.botBox - PlayerDisplayer.topBox)/2)/sizeBlock)*sizeBlock
    }

    def update : Unit = {
        for (i <- 0 to sizeMap - 1) {
            for (j <- 0 to sizeMap - 1) {
                grid(i)(j) foreach (b => b.updateCoordinatesOnMap(i, j))
            }
        }
        Utils.characterDisplayers.foreach(x => if (x.whichMap == PlayerDisplayer.whichMap) x.update)
    }
    

    //TODO: implement a zBuffer system to really draw everything in the right order
    def display (g : Graphics) : Unit = {
        g.drawImage(img, -x%sizeBlock - sizeBlock, -y%sizeBlock - sizeBlock, null)
        for (j <- 0 to sizeMap-1) {
            for (i <- 0 to sizeMap-1) {
                grid(i)(j) foreach (b => b.updateCoordinates(x, y, sizeBlock))
                grid(i)(j) foreach (b => b.display(g))
            }
            Utils.characterDisplayers.filter( chara => chara.j == j) foreach (chara => chara.display(g, x, y, n))
        }
        //sorts the character rendering order in respect to their depth on screen
        g.drawImage(menuBackground, 0, 400, null)
    }


    def changeCoordinates (moveX : Int, moveY : Int) : Unit = {
        x += moveX
        y += moveY
    }

    //Since trees are such particular objects, being more than 1 block thick and having an image that spans over 9 blocks, we define a special function to add one in the grid
    //this function covers the multiple blocks of the tree with invisible unwalkable blocks.
    def addTree(i : Int, j : Int) : Unit = {
        grid(i)(j) = (new TreeRoot)::grid(i)(j)
        grid(i+1)(j) = (new InvisibleBlock)::grid(i+1)(j)
        grid(i)(j+1) = (new InvisibleBlock)::grid(i)(j+1)
        grid(i+1)(j+1) = (new InvisibleBlock)::grid(i+1)(j+1)
    }
}

object EmptyMapDisplayer extends MapDisplayer (EmptyUI) {

}

class MapDisplayer1 (frame : UI) extends MapDisplayer (frame : UI) {

    override def initialise (sizeB : Int, i : Int = -1, j : Int = -1) : Unit = {
        super.initialise(sizeB, i, j)
        SecondCharacterDisplayer.mapDisplayer = this
        SecondPlayerDisplayer.mapDisplayer = this
    }

    override def getWildMonster : Monster = {
        var random = scala.util.Random.nextFloat
        if (random < 0.5f) {
            new Rattata
        } else if (random < 0.75f) {
            new Ponyta
        } else {
            new Eevee
        }
    }

    override def getWildLevel : Int = {
        scala.util.Random.nextInt(5) + 2
    }


    n = 1
    iStart = 7
    jStart = 4

    for(i <- 0 to sizeMap/2 -1) {
        addTree(2*i,0)
        addTree(2*i,sizeMap-2)
        addTree(0,2*i)
        addTree(sizeMap-2,2*i)

    }

    grid(4)(3) = List(new MultiCliff(0, 1))
    grid(4)(4) = List(new MultiCliff(0, 1))
    grid(4)(5) = List(new MultiCliff(-1, 1))
    grid(3)(5) = List(new MultiCliff(-1, 0))
    grid(2)(5) = List(new MultiCliff(-1, 0))

    grid(2)(3) = List(new GrassBlock)
    grid(2)(4) = List(new GrassBlock)
    grid(3)(3) = List(new GrassBlock)
    grid(3)(4) = List(new GrassBlock)

    for (i <- 7 to 13) {
        for (j <- 7 to 13) {
            grid(i)(j) = List(new IceBlock)
        }
    }

    for (i <- 7 to 13) {
        grid(i)(6) = List(new IceBlock,new MultiCliff(1, 0))
        grid(i)(14) = List(new IceBlock,new MultiCliff(-1, 0))

        grid(6)(i) = List(new IceBlock,new MultiCliff(0,-1))
        grid(14)(i) = List(new IceBlock,new MultiCliff(0, 1))
    }
    grid(6)(6) = List(new IceBlock,new MultiCliff(1,-1))
    grid(14)(14) = List(new IceBlock,new MultiCliff(-1,1))
    grid(14)(6) = List(new IceBlock,new MultiCliff(1,1))
    grid(6)(14) = List(new IceBlock,new MultiCliff(-1,-1))
    grid(7)(6) = List(new IceBlock)

    grid(13)(13) = List(new IceBlock,new RockBlock)
    grid(12)(8) = List(new IceBlock,new RockBlock)
    grid(8)(9) = List(new IceBlock,new RockBlock)
    grid(9)(7) = List(new IceBlock,new RockBlock)
    grid(13)(8) = List(new IceBlock,new RockBlock)
    grid(11)(10) = List(new IceBlock,new RockBlock)
    grid(11)(11) = List(new IceBlock,new RockBlock)
    grid(11)(12) = List(new IceBlock,new RockBlock)
    grid(10)(12) = List(new IceBlock,new RockBlock)
    grid(9)(12) = List(new IceBlock,new RockBlock)
    grid(9)(11) = List(new IceBlock,new RockBlock)
    grid(9)(10) = List(new IceBlock,new RockBlock)

    grid(10)(3) = List(new HealBlock)

    grid(11)(3) = List(new MapItemBlock(new Bike))
    
    grid(3)(12) = List(new Door(1))
    grid(1)(12) = List(new MapItemBlock(new Key(1)))

    grid(10)(6) = List(new Door(2))


    grid(18)(10) = List(new TargetedPortal(2, 3, 3))
}

class MapDisplayer2(frame : UI) extends MapDisplayer(frame : UI) {
    n = 2
    iStart = 3
    jStart = 3

    grid(3)(3) = List(new TargetedPortal(1, 18, 10))
    grid(17)(17) = List(new TargetedPortal(1, 17, 17))

    grid(5)(2) = List(new MapItemBlock(new Surf))

    for (i <- 7 to 13) {
        for (j <- 7 to 13) {
            grid(i)(j) = List(new MultiWater(0,0))
        }
    }
    for (i <- 7 to 13) {
        grid(i)(6) =  List(new MultiWater(1, 0))
        grid(i)(14) = List(new MultiWater(-1,0))
        grid(6)(i) =  List(new MultiWater(0,-1))
        grid(14)(i) = List(new MultiWater(0, 1))
    }
    grid(6)(6) =   List(new MultiWater(1,-1))
    grid(14)(14) = List(new MultiWater(-1,1))
    grid(14)(6) =  List(new MultiWater(1,1))
    grid(6)(14) =  List(new MultiWater(-1,-1))
}