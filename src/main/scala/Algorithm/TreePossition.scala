package Algorithm

object TreePossition {

  def main(args: Array[String]): Unit = {

    case class Tree(var left: Tree, var right: Tree, value: Int)

//        1
//      2     2
//    3   3     3
//            4   4
//          5
//        6

    val master = Tree(null, null, 1)

    master.left = Tree(null, null, 2)
    master.right = Tree(null, null, 2)

    master.left.left = Tree(null, null, 3)
    master.left.right = Tree(null, null, 3)
    master.right.right = Tree(null, null, 3)

    master.right.right.right = Tree(null, null, 4)
    master.right.right.left = Tree(null, null, 4)

    master.right.right.left.left = Tree(null, null, 5)
    master.right.right.left.left.left = Tree(null, null, 6)

    println(master)

    def findPosition(tr: Tree, position: Int): Int = {

      println(s"Level => $position")
      if (tr.left != null && tr.right != null) {

        val firstposition = findPosition(tr.left, position + 1)
        val secondPosition = findPosition(tr.right, position + 1)
        if (firstposition < secondPosition) secondPosition else firstposition

      } else if (tr.left == null && tr.right != null) {
        findPosition(tr.right, position + 1)
      } else if (tr.left != null && tr.right == null) {
        findPosition(tr.left, position + 1)
      } else
        position

    }

    val highestDept = findPosition(master, 1)

    println(highestDept)
    def printPosition(tr: Tree, position: Int, printLevel: Int): Int = {

//      println(s"Level => $position")

      if(position == printLevel)  print( s"Level@$position ",tr.value)
      if (tr.left != null && tr.right != null) {
        val firstposition = printPosition(tr.left, position + 1, printLevel)
        val secondPosition =
          printPosition(tr.right, position + 1, printLevel)
        if (firstposition < secondPosition) secondPosition else firstposition

      } else if (tr.left == null && tr.right != null) {
        printPosition(tr.right, position + 1, printLevel)
      } else if (tr.left != null && tr.right == null) {
        printPosition(tr.left, position + 1, printLevel)
      } else
        position

    }

    var min = 1
    var max = highestDept
    while(min < max){
      printPosition(master,1 , min)
      println()
      printPosition(master,1 , max)
      println()
      min = min+1
      max = max-1
    }


  }

}
