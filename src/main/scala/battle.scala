class Battle (p1 : Player, p2 : Player) {
    var ui : BattleUI = new BattleUI(p1, p2, this)
    def initialise : Unit = {
        ui.initialise

        p1.battle = this
        p2.battle = this

        p1.enterBattle
        p2.enterBattle

        ui.updateImages
    }

    def start : Unit = {
        while (p1.playing && p2.playing) {
            p1.newTurn
            ui.updateImages
            if (p2.playing) {
                p2.newTurn
                ui.updateImages
            }
        }
    }
}

object EmptyBattle extends Battle (EmptyPlayer, EmptyPlayer) {
    
}