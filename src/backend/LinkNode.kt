package backend

class ColumnNode(val name: String): Node() {
    var size: Int = 0
    init{
        columnNode = this
    }

    fun cover() {
        // unlink ColumnNode from ColumnNodes
        this.leftNode.rightNode = this.rightNode
        this.rightNode.leftNode = this.leftNode

        // go through the column and cover each row
        var nextVerticalNode = this.bottomNode
        while (nextVerticalNode != this) {
            var nextHorizontalNode = nextVerticalNode.rightNode
            // go through each row and cover each node
            while (nextHorizontalNode != nextVerticalNode) {
                nextHorizontalNode.coverNode()
                nextHorizontalNode.columnNode.size--
                nextHorizontalNode = nextHorizontalNode.rightNode
            }
            nextVerticalNode = nextVerticalNode.bottomNode
        }
    }

    fun uncover() {
        // relink ColumnNode to ColumnNodes
        this.leftNode.rightNode = this
        this.rightNode.leftNode = this

        // go through the column and uncover each row
        var nextVerticalNode = this.topNode
        while (nextVerticalNode != this) {
            var nextHorizontalNode = nextVerticalNode.leftNode
            // go through each row and uncover each node
            while (nextHorizontalNode != nextVerticalNode) {
                nextHorizontalNode.uncoverNode()
                nextHorizontalNode = nextHorizontalNode.leftNode
                nextHorizontalNode.columnNode.size++
            }
            nextVerticalNode = nextVerticalNode.topNode
        }
    }
}

open class Node() {
    var topNode: Node = this
    var bottomNode: Node = this
    var leftNode: Node = this
    var rightNode: Node = this
    lateinit var columnNode: ColumnNode

    constructor(columnNode: ColumnNode): this() {
        this.columnNode = columnNode
    }

    fun coverNode() {
        this.topNode.bottomNode = this.bottomNode
        this.bottomNode.topNode = this.topNode
    }

    fun uncoverNode() {
        this.topNode.bottomNode = this
        this.bottomNode.topNode = this
    }

    // we only navigate through right / bottom so we only need insert through right/bottom
    fun insertBottom(node: Node): Node {
        node.bottomNode = this.bottomNode
        node.bottomNode.topNode = node
        node.topNode = this
        this.bottomNode = node
        return node
    }

    fun insertRight(node: Node): Node {
        node.rightNode = this.rightNode
        node.rightNode.leftNode = node
        node.leftNode = this
        this.rightNode = node
        return node
    }
}