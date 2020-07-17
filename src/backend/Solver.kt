package backend

/*
    The solver was inspired by Jonathan Chu's "Dancing Links and Sudoku".
    The concept of Dancing Links and Algorithm X were brought in to solve Sudokus at much faster speed than standard
    back-tracing algorithm. Algorithm X is able to solve Exact Cover problems and it is a very interesting concept for
    me to learn.
 */

class Solver() {
    /*
        Algorithm X by Dr. Donald knuth:
        - Create Matrix
        - Algorithm X as below:
        - if matrix has no column : return solution
        - else:
            - choose a column C in Matrix
            - cover C
            - for each row in C: Add row to solution
            - recursively calls Algorithm X on the reduced matrix
            - uncover C
            - remove each row in C from solution
     */

    // this is the size (width/height) of the entire Sudoku
    val sudokuSize = 9  //9
    // this is the size (width/height) of a single region/box
    val regionSize = 3  //3
    val noValue = 0
    val minValue = 1
    val maxValue = this.sudokuSize
    val numberOfConstraints = 4
    val coverStartingIndex = 1

    // standard sudoku grid
    var sudokuGrid: Array<IntArray> = Array(sudokuSize) { IntArray(sudokuSize) }
    // sudoku grid in matrix form
    var sudokuMatrix: Array<IntArray> = Array(sudokuSize * sudokuSize * maxValue) {
        IntArray(sudokuSize * sudokuSize * numberOfConstraints)
    }

    var header: ColumnNode
    // storing results
    var solutionNodes: MutableList<Node> = mutableListOf()
    var resultNodes: MutableList<Node> = mutableListOf()

    init {
        header = convertMatrixToDL()
    }

    // Find out the index of a value cell in matrix
    private fun getMatrixIndex(row: Int, column: Int, value: Int): Int {
        val retval = (row - 1) * sudokuSize * sudokuSize + (column - 1) * sudokuSize + (value - 1)
        return retval
    }

    // Sudoku has 4 constraints that needs to be satisfied, so we add them to the matrix
    private fun appendSudokuConstraints() {
        var progressiveIndex = 0

        // Go through cell constraint. Each cell should only contain the value in range
        for (row in coverStartingIndex .. sudokuSize) {
            for (column in coverStartingIndex .. sudokuSize) {
                for (value in coverStartingIndex .. sudokuSize) {
                    val matrixIndex = getMatrixIndex(row, column, value)
                    sudokuMatrix[matrixIndex][progressiveIndex] = 1
                }
                // increase progressive index for every cell
                progressiveIndex++
            }
        }

        // Going through row constraint. Each row should contain only one identical value.
        for (row in coverStartingIndex .. sudokuSize) {
            for (value in coverStartingIndex .. sudokuSize) {
                for (column in coverStartingIndex .. sudokuSize) {
                    val matrixIndex = getMatrixIndex(row, column, value)
                    sudokuMatrix[matrixIndex][progressiveIndex] = 1
                }
                // increase progressive index for each constraint.
                progressiveIndex++
            }
        }

        // Going through column  constraint. Each column should contain only one identical value.
        for (column in coverStartingIndex .. sudokuSize) {
            for (value in coverStartingIndex .. sudokuSize) {
                for (row in coverStartingIndex .. sudokuSize) {
                    val matrixIndex = getMatrixIndex(row, column, value)
                    sudokuMatrix[matrixIndex][progressiveIndex] = 1
                }
                // increase progressive index for each constraint.
                progressiveIndex++
            }
        }

        // Going through region constraint. Each region should contain only one identical value.
        for (row in coverStartingIndex .. sudokuSize step regionSize){
            for (column in coverStartingIndex .. sudokuSize step regionSize) {
                for (value in coverStartingIndex .. sudokuSize) {
                    for (rowIndex in 0 until regionSize) {
                        for (columnIndex in 0 until regionSize) {
                            val matrixIndex = getMatrixIndex(row+rowIndex, column+columnIndex, value)
                            sudokuMatrix[matrixIndex][progressiveIndex] = 1
                        }
                    }
                    // increase progressive index for each constraint.
                    progressiveIndex++
                }
            }
        }
    }

    // This function converts a 2-dimension array grid to the dancing link matrix
    fun convertGridToMatrix() {
        appendSudokuConstraints()

        // converting existing Sudoku grid with filled values to matrix
        this.sudokuGrid.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { columnIndex, value->
                if (value != this.noValue) {
                    for (symbol in this.minValue .. this.maxValue) {
                        // everything is indexed. Thus we just need to fill those with no value with 0 to indicate they
                        // are not a constraint row, nor a row with pre-filled value.
                        if (symbol != value) {
                            sudokuMatrix[getMatrixIndex(rowIndex + 1, columnIndex + 1, symbol)].fill(0)
                        }
                    }
                }
            }
        }
    }

    // This function converts a dancing link matrix to dancing link nodes
    fun convertMatrixToDL(): ColumnNode {
        var headerNode = ColumnNode("head")
        var columnNodes: MutableList<ColumnNode> = mutableListOf()

        // create a top column node list to store columns information
        for (i in sudokuMatrix[0].indices) {
            var columnNode = ColumnNode(i.toString())
            columnNodes.add(columnNode)
            headerNode = headerNode.insertRight(columnNode) as ColumnNode
        }
        // set headerNode back to original
        headerNode = headerNode.rightNode.columnNode

        // go through each row cell under each columnNode, and create the node if value exist
        for (col in sudokuMatrix) {
            var previousNode: Node? = null
            for (j in col.indices) {
                if (col[j] == 1){
                    val columnNode = columnNodes[j]
                    val newNode = Node(columnNode)

                    if (previousNode == null) {
                        previousNode = newNode
                    }

                    columnNode.topNode.insertBottom(newNode)
                    previousNode = previousNode.insertRight(newNode)
                    columnNode.size++
                }
            }
        }

        headerNode.size = columnNodes.size

        return headerNode
    }

    // This function converts dancing link nodes to a 2-dimension array grid
    fun convertDLToGrid(): Array<IntArray> {
        var result: Array<IntArray> = Array(sudokuSize) { IntArray(sudokuSize) }
        for (node in resultNodes) {
            var minNode = node
            var min = minNode.columnNode.name.toInt()

            // look for node with smallest value
            var temp = node.rightNode
            while (temp != node) {
                var value = temp.columnNode.name.toInt()
                if (min > value) {
                    min = value
                    minNode = temp
                }
                temp = temp.rightNode
            }

            val minValue = minNode.columnNode.name.toInt()
            val nextMin = minNode.rightNode.columnNode.name.toInt()
            val row = minValue/sudokuSize
            val column = minValue%sudokuSize
            val value = (nextMin%sudokuSize) + 1
            result[row][column] = value
        }

        return result
    }

    // This function can greatly reduce the execution time by selecting min count of a column to avoid computing through sparse matrix.
    fun quickSelect(): ColumnNode {
        var rightNode = header.rightNode.columnNode
        var minNode = rightNode
        while (rightNode != header) {
            if (minNode.columnNode.size > rightNode.columnNode.size) {
                minNode = rightNode
            }
            rightNode = rightNode.rightNode.columnNode
        }
        return minNode
    }

    // algorithm X implementation on paper
    fun search(k: Int) {
        if (header.rightNode == header) {
            // resolved
            resultNodes = solutionNodes.toMutableList()
            return
        } else {
            if (resultNodes.isNotEmpty()) {
                return
            }

            var column: ColumnNode = quickSelect()
            // we can stop this thread if any column has no more bottom node
            if (column.size == 0) {
                return
            }

            column.cover()

            var row = column.bottomNode
            while (row != column) {  // O(nRows)
                // add to solution
                solutionNodes.add(row)

                // cover
                var rightNode = row.rightNode
                while (rightNode != row) {  //
                    rightNode.columnNode.cover()
                    rightNode = rightNode.rightNode
                }

                // recursive call
                search(k+1)

                // remove from solution
                solutionNodes.remove(solutionNodes.last())
                column = row.columnNode

                // uncover
                var leftNode = row.leftNode
                while(leftNode != row) {
                    leftNode.columnNode.uncover()
                    leftNode = leftNode.leftNode
                }

                row = row.bottomNode
            }

            column.uncover()
        }
    }

    // Adds a new Sudoku to the solver
    fun newSudoku(sudokuGrid: Array<IntArray>){
        this.sudokuGrid = sudokuGrid
    }

    // A testing beginner Sudoku
    fun useHardcodedSudoku(){

        // Expert sudoku on Sudoku.com
        val sudoku: Array<IntArray> =
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

        // empty
//        val sudoku: Array<IntArray> =
//            arrayOf(
//                intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
//                intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
//                intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
//                intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
//                intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
//                intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
//                intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
//                intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
//                intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0)
//            )


        sudoku.forEachIndexed { rowIndex, _ ->
            sudoku[rowIndex].forEachIndexed { columnIndex, _ ->
                sudokuGrid[rowIndex][columnIndex] = sudoku[rowIndex][columnIndex]
            }
        }
    }

    // Testing function that prints and see the header node list
    fun printHeader() {
        var thisNode = header.rightNode
        while (header != thisNode) {

            print(thisNode.columnNode)
            print(" | ")
            var nextNode = thisNode.bottomNode
            while (nextNode != thisNode) {
                print(nextNode)
                print(" ")
                nextNode = nextNode.bottomNode
            }
            println()
            thisNode = thisNode.rightNode
        }
    }
}

fun main() {
    // matrix tested clear.
    val solver = Solver()
    solver.useHardcodedSudoku()
    solver.convertGridToMatrix()
}