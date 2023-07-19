package minesweeper
import kotlin.random.Random

fun main() {
    val mines = println("How many mines do you want on the field?").run { readln().toInt() }.also { Minesweeper().printField() }
    var set = println("Set/unset mines marks or claim a cell as free:").run { readln() }.split(" ").toMutableList()
    val game = Minesweeper(mines, set)
    while (true) {
        game.playerTurn(set)
        if (game.lose) return
        game.printField()
        if (game.checkWin()) break
        set = println("Set/unset mines marks or claim a cell as free:").run { readln() }.split(" ").toMutableList()
    }
    game.printFieldWin()
    println("Congratulations! You found all the mines!")
}

class Minesweeper(private val mines: Int = 0, private val set: MutableList<String> = mutableListOf()) {

    private fun MutableList<MutableList<String>>.copyLock(): MutableList<MutableList<String>> {
        val list = MutableList(this.size) { MutableList(this[0].size) { "" } }
        for (i in this.indices) when (i) {
            0, 1, 11 -> for (j in this[i].indices) list[i][j] = this[i][j]
            else -> for (j in this[i].indices) when (j) {
                0, 1, 11 -> list[i][j] = this[i][j]
                else -> list[i][j] = "."
            }
        }
        return list
    }

    private val rows = 12
    private val cols = 12

    private val gameWin = getField(mines)
    private val game = gameWin.copyLock()
    var lose = false

    private fun getField(mines: Int): MutableList<MutableList<String>> {
        val field = MutableList(rows) { MutableList(cols) { "/" } }
        var cMines = mines
        for (j in field.indices) {
            when (j) {
                1, 11 -> for (i in field[j].indices) when (i) {
                    1, 11 -> field[j][i] = "|"
                    else -> field[j][i] = "—"
                }
                0 -> for (i in field[0].indices) when (i) {
                    1, 11 -> field[j][i] = "|"
                    0 -> field[0][0] = " "
                    else -> field[0][i] = (i - 1).toString()
                }
                else -> for (i in field[j].indices) when (i) {
                    0 -> field[j][0] = (j - 1).toString()
                    1, 11 -> field[j][i] = "|"
                    else -> while (cMines != 0) {
                        val ch = Random.nextInt(81)
                        val x = (ch / 9) + 2
                        val y = (ch % 9) + 2
                        if (x == set[1].toInt() + 1 && y == set[0].toInt() + 1) continue
                        if (field[x][y] != "*") {
                            field[x][y] = "*"
                            for (k in -1..1) {
                                for (l in -1..1) {
                                    if (field[x + k][y + l] == "/") field[x + k][y + l] = 1.toString()
                                    else if (field[x + k][y + l].first().isDigit()) field[x + k][y + l] =
                                        (field[x + k][y + l].toInt() + 1).toString()
                                }
                            }
                            cMines -= 1
                        }
                    }
                }
            }
        }
        return field
    }

    fun  printField()  { for (i in game.indices) println(game[i].joinToString("")) }

    fun  printFieldWin()  { for (i in gameWin.indices) println(gameWin[i].joinToString("")) }

    fun checkWin() = checkWin(game, gameWin)

    private fun checkWin(game: MutableList<MutableList<String>>, gameWin: MutableList<MutableList<String>>): Boolean {
        var count = 0
        var rCount = 0
        var bool = true
        for (i in 2 until  rows - 1) {
            for (j in 2 until  cols - 1) {
                if (game[i][j] == "." && gameWin[i][j] != "*") bool = false
                if (game[i][j] == "*") count ++.run { if (game[i][j] == "*" && gameWin[i][j] == "*") rCount++ }
            }
        }
        return (count == mines && count == rCount) || bool
    }

    fun playerTurn(set: MutableList<String>) = if (set[2] == "free") freeCell(set[1].toInt() + 1, set[0].toInt() + 1) else if (set[2] == "mine") mineCell(set[1].toInt() + 1, set[0].toInt() + 1) else println("Incorrect input!")

    private fun freeCell(x: Int, y: Int) {
        when (gameWin[x][y]) {
            "*" -> {
                println("You stepped on a mine and failed!")
                lose = true
            }
            in "12345678" -> game[x][y] = gameWin[x][y]
            "/" -> openCage(x, y)
        }
    }

    private fun openCage(x: Int, y: Int) {
        game[x][y] = gameWin[x][y]
        for (l in -1..1) {
            for (k in -1..1) {
                if ((game[x + l][y + k] == "." || game[x + l][y + k] == "*") && gameWin[x + l][y + k] == "/") {
                    openCage(x + l, y + k)
                } else game[x + l][y + k] = gameWin[x + l][y + k]
            }
        }
    }

    private fun mineCell(x: Int, y: Int) = if (game[x][y] == ".") game[x][y] = "*" else if (game[x][y] == "*") game[x][y] = "." else println("Incorrect cell")

}