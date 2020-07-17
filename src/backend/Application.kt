package backend

import kotlin.system.measureTimeMillis

class Application {
    val solver = Solver()
    var resultGrid : Array<IntArray> = Array(solver.sudokuSize) { IntArray(solver.sudokuSize) }

    init{
        solver.newSudoku(
            arrayOf(
                intArrayOf(0, 6, 0, 0, 0, 0, 3, 0, 0),
                intArrayOf(0, 0, 2, 0, 0, 0, 7, 0, 0),
                intArrayOf(1, 0, 7, 0, 8, 0, 4, 0, 0),
                intArrayOf(0, 0, 0, 4, 6, 0, 0, 0, 3),
                intArrayOf(0, 0, 0, 2, 0, 0, 0, 0, 7),
                intArrayOf(7, 0, 4, 0, 0, 5, 2, 0, 0),
                intArrayOf(0, 0, 0, 0, 5, 0, 0, 8, 9),
                intArrayOf(0, 5, 0, 9, 0, 0, 6, 0, 0),
                intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0)
            )
        )
        solver.convertGridToMatrix()
        solver.header = solver.convertMatrixToDL()
    }

    fun run() {
        solver.search(0)
        resultGrid = solver.convertDLToGrid()
        printResult()
    }

    // This function prints the result of the Sudoku
    fun printResult() {
        if (solver.resultNodes.isEmpty()) {
            println("Couldn't find a solution")
        } else {
            printArray(resultGrid)
        }
    }
}

// Test print any 2 dimension array
fun printArray(array: Array<IntArray>) {
    for (i in array) {
        for (j in i) {
            print("$j ")
        }
        println()
    }
}

fun main () {
    val time = measureTimeMillis {
        val app = Application()
        app.run()
        println()
    }
    println("Execution time: $time")
}