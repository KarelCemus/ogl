import scala.io.Source
import util.control.Breaks._
import scala.collection.mutable
import EasyMetrics._


object ogl {
  def main(args: Array[String]): Unit = {
        println("Started")
        startMeasurament()
        //val data1 = Source.fromFile("um_tempcolor", "utf-8").getLines
        // Remember a line is broken(encoding)
        // better iterate in loop to avoid errors
        val data1 = Source.fromFile("um_tempcolor", "utf-8").getLines.toArray
        //#with open("um_tempcolor", 'r') as file1:
        //#    data1 = file1.read()
        
        //#with open("docs/fourthstyle", 'r') as file1:
        //#    data1 = file1.read()

// #result = subprocess.run([
// #    'git',
// #    'log',
// #    '--graph',
// #    #'--pretty="format:\\%Cred%h%Creset -%C(yellow)%d%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset"',
// #    #'--pretty=format:\%Cred%h%Creset -%C(yellow)%d%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset',
// #    '--pretty=format:%h -%d %s (%cr) <%an>',
// #    '--abbrev-commit',
// #    '--color'
// #    ],
// #    stdout=subprocess.PIPE)
// #data1 = result.stdout.decode("utf-8")

def parse_line(line: String): (Array[(String, String)], String) = {
    var escapes: mutable.ArrayBuffer[(String, String)] = mutable.ArrayBuffer()
    var escapes_so_far = ""
    var clean_line: StringBuilder =  new StringBuilder()

    var index = 0
    var clean_index = 0
    while (index < line.size) {
        if (line(index) == '\u001b') {
            val start = line.indexOf("\u001b[", index)
            if (start != 1) {
                val end = line.indexOf("m", start)
                if ((end - start) <= 2) {
                    //#print(line[start:end + 1])
                    //escapes(len(escapes)-1)(1) = line.slice(start,end + 1)
                    // escapes = escapes.updated(
                    //     len(escapes)-1, 
                    //     (escapes(len(escapes) -1)._1, line.substring(start, end+1)))
                    escapes(escapes.size-1) =
                        (escapes(escapes.size -1)._1, line.substring(start, end+1))
                    //(len(escapes)-1)(1) = line.slice(start,end + 1)
                } else {
                    escapes_so_far += line.slice(start,end + 1)
                }
                index = end
            }
        } else {
            //escapes = escapes :+ (escapes_so_far, "")
            escapes.append((escapes_so_far, ""))
            escapes_so_far = ""
            clean_line.append(line(index))
            clean_index += 1
        }
        index = index + 1
    }
    return (escapes.toArray, clean_line.toString())
}

// #from shutil import get_terminal_size
// #print(get_terminal_size())

//import re

// not sure if rgx is the fastest way
//first_non_graph_rgx = re.compile(r"[^*|\\/ _]")
val first_non_graph_rgx = """[^*|\\/ _]""".r
val allGraphChars = Set('*','|','\\','/',' ','_')


type CharMatrix = Array[Array[Char]]

type CharMatrixView = scala.collection.SeqView[scala.collection.mutable.IndexedSeqView[Char,Array[Char]],Array[scala.collection.mutable.IndexedSeqView[Char,Array[Char]]]]    
type SqueezedMatrixView = scala.collection.SeqView[Char,Array[Char]]


println("File load " + took())

var style: mutable.ArrayBuffer[Array[(String, String)]] = 
    new mutable.ArrayBuffer[Array[(String, String)]](data1.size)
// var style: mutable.ArrayBuffer[Array[(String, String)]] = 
//     new mutable.ArrayBuffer[Array[(String, String)]]()
// var style: mutable.ArrayBuffer[Vector[(String, String)]] = mutable.ArrayBuffer.fill(data1.size)(Vector())
var graph_lines: mutable.ArrayBuffer[Array[Char]] = new mutable.ArrayBuffer[Array[Char]](data1.size)
//var graph_lines: mutable.ArrayBuffer[Array[Char]] = new mutable.ArrayBuffer[Array[Char]]()
var messages:  mutable.ArrayBuffer[String] = new mutable.ArrayBuffer[String](data1.size)
//var messages:  mutable.ArrayBuffer[String] = new mutable.ArrayBuffer[String]()
//for (raw_line in data1.splitlines()) {

var max_graph_idx = -1
//var idx = 0 
for (raw_line <- data1) {
//    idx = idx + 1 
    //println(ind)
    //println(raw_line.size)
    //println(raw_line)
    val (escapes, line) = parse_line(raw_line)
    //println("LOOP 1")
    //val match_ = first_non_graph_rgx.findFirstMatchIn(line)

    val index = line.indexWhere(c => !allGraphChars.contains(c))
    //allGraphChars.zipWithIndex.
    
    var graph = line
    var message = ""

    //last_graph_idx = None
    if (index >= 0) {
        val last_graph_idx = index
        if (last_graph_idx > max_graph_idx ) {
            max_graph_idx = last_graph_idx
        }
        graph = line.slice(0, last_graph_idx)
        message = line.slice(last_graph_idx, line.size)
    }
    //println("AFTER MATCH")

    //if (graph.contains('/') || graph.contains("\\")) {
    if (graph.exists(c => c == '/' || c == '\\')) {
        var extended = graph.replace("*", "|")

        extended = extended.replace("_", " ")
        //#style.append(escapes[:len(extended)])
        //#style.append(escapes.copy())
        //style.append(copy.deepcopy(escapes))
        style.append(escapes) // style.append(copy.deepcopy(escapes))
        //graph_lines.append(extended)
        graph_lines.append(extended.toArray)
        messages.append("")
    }

    //println("AFTER CONTAINS")

    // graph_lines.append(graph.replace("|_", "|─").toList)
    graph_lines.append(graph.replace("|_", "|─").toArray)
    style.append(escapes.toArray)
    messages.append(message)

    //println("AFTER AFFTER CONTAINS")
}

print("Summary | max graph idx: " + max_graph_idx)
print(" | lines: " + graph_lines.size)
println(" | original lines: " + data1.size)
println("Splitted graph " + took())
startMeasurament()

// for (lno, line in enumerate(graph_lines)) {
//     graph_lines[lno] = line.toList
// }



// def get_matrix_size(matrix: Vector[Vector[Char]]) = (len(matrix), len(matrix(0)))

def get_matrix_size(matrix: CharMatrixView) = (matrix.size, matrix(0).size)



def get_sub_matrix_view(target: Array[Array[Char]], 
                        start_pos: (Int, Int),
                        size: (Int, Int)): SqueezedMatrixView = {

    val (start_x, start_y) = start_pos
    val (height, width) = size
    val end_y = start_y + width

    val target_height = target.size

    var tempArray = Array.ofDim[Char](height*width)
    var view = tempArray.view

    var i = 0
    var j = 0
    var column = 0
    while (i < height) {
        j = 0
        while (j < width) {
            tempArray(column +  j) = target(start_x + i)(start_y + j) 
            j+=1
        }
        column += j
        i+=1
    }
    
    return view
}

def get_sub_matrix(target: Array[Array[Char]], start_pos: (Int, Int) , size: (Int, Int)): Vector[Vector[Char]] = {
    val (start_x, start_y) = start_pos
    val (height, width) = size
    val end_y = start_y + width

    val target_height = target.size

    if (start_x + height > target_height)
        return null

    var window = Vector.empty[Vector[Char]]

    for (offset_x <- 0 until height) {
        val x = start_x + offset_x

        if (end_y > target(x).size) {
            return null   
        }

        // optimize??
        window = window :+ target(x).slice(start_y,end_y).toVector
        //window = window :+ tuple(target(x)[start_y:end_y])
        // #window[offset_x] = target(x)[start_y:end_y]
    }

    return window
}


def equals_matrix(target: CharMatrix, start_pos: (Int, Int))(expected: CharMatrixView): Boolean = { 
    val (start_x, start_y) = start_pos
    // if (start_pos == (9,5)) println("expected" + expected.toList.map(_.toList))
    
    // if (start_pos == (9,5)) println("comparsion" + 
    //     target.slice(start_x,start_x + expected.size).map(_.drop(start_y)).toList.map(_.toList))

    // if (start_pos == (9,5)) println("comparsion2" + 
    //     target.slice(start_x,start_x + expected.size).toList.map(_.toList) + "|")

    if (target.size < start_x + expected.size) {
        return false
    }

   var target_x = 0
   //for (i <- 0 until expected.size) {
    var i = 0
    var j = 0
    while (i < expected.size) {
    // if (start_pos == (9,5)) println("debug=" + i)
        target_x = start_x + i
        if (target(target_x).size < start_y + expected(i).size) {
            // if (start_pos == (9,5)) println("give up column too big")
            return false
        }
        // if (start_pos == (9,5)) println("debug target size =" + target(i).size)
        // if (start_pos == (9,5)) println("debug going size =" + start_y + "+"+ expected(i).size)
        //for (j <- 0 until expected(i).size) {
        j = 0
        while (j < expected(i).size) {
            // if (start_pos == (9,5)) println("debug" + (i,j))
           if (target(target_x)(start_y + j) != expected(i)(j) ) {
               return false
           }
        //    if (start_pos == (9,5)) println("after debug" + (i,j))
           j += 1
        }
        i += 1
    }
    // if (start_pos == (9,5)) println("out")

   return true
}


def replace_matrix(replacement: CharMatrixView, 
                   target: Array[Array[Char]],
                   start_pos: (Int, Int)) = {
    val (start_x, start_y) = start_pos

    val new_target = target

    // println("replace")
    // println(replacement)
    // println(start_pos   )

    var i = 0
    var j = 0
    //for (i <- 0 until replacement.size) {
    while (i < replacement.size) {
        // for (j <- 0 until replacement(i).size) {
        j = 0
        while (j < replacement(i).size) {
            //target(start_x + i)(start_y + j) = replacement(i)(j)
            // new_target = new_target.updated(
            //     start_x + i, 
            //     new_target(start_x + i).updated(start_y + j, replacement(i)(j)))
            //new_target(start_x + i) = new_target(start_x + i).updated(start_y + j, replacement(i)(j))
            new_target(start_x + i)(start_y + j) = replacement(i)(j)
            j+=1
        }
        
        i+=1
    }
    // println(new_target.slice(start_x, start_x + len(replacement)))
    new_target
    //updateTarget(new_target)
}


//def print_matrix(matrix):
//    for i in range(len(matrix)):
//        print(matrix[i])

def get_matrix_str(matrix: CharMatrixView) = matrix.map(_.mkString("")).mkString("\n")

/* Seems to be slower */
def findInArray(target: CharMatrix, array: Array[(CharMatrixView,(CharMatrixView, ((Int, Int), (Int, Int))))],
    start_pos: (Int, Int), size: (Int, Int)): 
    (CharMatrixView, ((Int, Int), (Int, Int))) = {
  val (lidx, ridx) = start_pos
  val (height, width) = size
  var i = 0
  var found = null
  while (i < array.size) {
    if (equals_matrix(target, start_pos)(array(i)._1)) {
      return array(i)._2
    }

    i+=1
  }

  return found
}

def replace_list_equals(origin_target: Array[Array[Char]],
                        substitutions: Map[CharMatrixView,(CharMatrixView, ((Int, Int), (Int, Int)))], 
                        expected_size: (Int, Int),
                        max_column: Int = -1) = {     
  var target = origin_target
  //val expected_size = get_matrix_size(substitutions.values.toIndexedSeq(0)._1)
  val first_char_set = substitutions.keySet.map(m => m(0)(0))
  val substitutionsArray = substitutions.toArray
  var lidx = 0
  var ridx = 0

  while (lidx < target.size) {
    val inner_max_column = if (max_column == -1) target(lidx).size  else max_column + 1
    ridx = 0
    while (ridx < inner_max_column) {
      val start_pos = (lidx, ridx)
      var found: (CharMatrixView, ((Int,Int), (Int, Int))) = null
      if (first_char_set.contains(target(lidx)(ridx))) {
        found = substitutionsArray.find(x => equals_matrix(target, start_pos)(x._1)).map(_._2).getOrElse(null)
        //found = findInArray(target, substitutionsArray, start_pos, expected_size)
      }
      
      while (found != null) {
        val pair = found
        val replacement = pair._1
        replace_matrix(replacement, target, start_pos)
        
        var paint = pair._2
        
        if (paint != null) {
            val (source, dest) = paint
            val source_line = lidx + source._1
            val source_column = ridx + source._2
            val dest_line = lidx + dest._1
            val dest_column = ridx + dest._2
            val source_style = style(source_line)(source_column)
            style(dest_line)(dest_column) = source_style
        }

        found = null
        if (first_char_set.contains(target(lidx)(ridx))) {
          found = substitutions.find(x => equals_matrix(target, start_pos)(x._1)).map(_._2).getOrElse(null)
          //found = findInArray(target, substitutionsArray, start_pos, expected_size)
        }
      }
      ridx +=1    
    }
    lidx +=1
  }

  target
}

def replace_list(origin_target: Array[Array[Char]],
                 substitutions: Map[SqueezedMatrixView,(Array[((Int, Int), Char)], ((Int, Int), (Int, Int)))], 
                 expected_size: (Int, Int),
                 max_column: Int = -1) = {
    //println("replace_list")
     
    var target = origin_target

    // println("substitutions list")
    // substitutions.foreach { case (a, (b, _)) => 
    //     println("expected")
    //     println(a.map(_.mkString("")).mkString("\n"))
    //     println("replacement")
    //     println(b.map(_.mkString("")).mkString("\n"))
    // }
    //substitutions.map(x => (x._1.map(_.mkString("")).mkString("\n"), x._2._1.map(_.mkString("")).mkString("\n"))).foreach(println)
    
    //val expected_size = get_matrix_size(substitutions.keys.head)
    //val first_char_set = substitutions.keySet.map(m => m(0)(0))
    //println("SMART WILL BUILD")
    val smartSet = Array.fill[Set[Char]](expected_size._1, expected_size._2)(Set())
    for (i <- 0 until expected_size._1) {
      for (j <- 0 until expected_size._2) {
        smartSet(i)(j) = substitutions.keySet.map(m => m(i*expected_size._2 + j))
      }
    }

    //println("SMART SET BUILT")

    def smartContains(start_pos: (Int, Int)): Boolean = {
      val (start_x, start_y) = start_pos
    
      if (target.size < start_x + expected_size._1) {
        return false
      }

      var target_x = 0
      var target_y = 0

      var i = 0
      var j = 0

      while (i < expected_size._1) {
        target_x = start_x + i

        if (target(target_x).size < start_y + expected_size._2) {
          return false
        }
        
        j = 0
        while (j < expected_size._2) {
          target_y = start_y + j
          if (!smartSet(i)(j).contains(target(target_x)(target_y))) {
            return false
          }

          j += 1
        }

        i += 1
      }

     return true
    }
    
    //println(expected_size)
    var lidx = 0
    var ridx = 0
    //for (lidx <- 0 until target.size) {
    while (lidx < target.size) {
        val inner_max_column = if (max_column == -1) target(lidx).size  else max_column + 1
        //for (ridx <- 0 until inner_max_column) {
        ridx = 0
        while (ridx < inner_max_column) {
            //////println("replace_list 4 " + (lidx,ridx))
            // println("breakable--->")
            //breakable {
            // if (max_column != -1 && ridx > max_column) {
            //     // println("break!")
            //     break
            // }
                
            val start_pos = (lidx, ridx)
            //////println("start_pos=" + (lidx,ridx))
            var window: SqueezedMatrixView = null
            var found: (Array[((Int, Int), Char)], ((Int,Int), (Int, Int))) = null
            // if (true) {
            //if (first_char_set.contains(target(lidx)(ridx))) {
            //if (first_char_set.contains(target(lidx)(ridx)) && smartContains(start_pos)) {
            if (smartContains(start_pos)) {
                window = get_sub_matrix_view(target, start_pos, expected_size)
                found = substitutions.getOrElse(window, null)
            }
            //--// var foundTuple = substitutions.find(x => equals_matrix(target, start_pos)(x._1))
            //--// var found = foundTuple.map(_._2).getOrElse(null)
            //////println("found tuple")
            //////println(foundTuple.map(t => get_matrix_str(t._1)).getOrElse(""))
            //var found = substitutions.find(equals_matrix(target, start_pos))
            ////println("after_matrix")
            
            
            //#if expected_size[1] > 3:
            //#    print_matrix(window)
            //#    print()
            while (window != null && found != null) {
                        val pair = found
                        // println("replace_list 5" + pair)
                        val replacement = pair._1
                        // #print_matrix(replacement)
                        // println("replace matrix")
                        // println(replacement)
                        //target = 
                        // println("replace_list 6 " + replacement.toSeq.map(_.toSeq))
                        //replace_matrix(replacement, target, start_pos)
                        // println("replace_list 7 " + window.toList.map(_.toList))

                        //var paintR = substitutions(window)
                        //println("replace_list 8" + paintR)
                        var paint = pair._2
                        var k = 0
                        while (k < replacement.size) {
                          val ((x, y), c) = replacement(k)
                          target(lidx + x)(ridx + y) = c
                          //target(lidx + replacement(k)._1._1)(ridx + replacement(k)._1._2) = replacement(k)._2
                          k += 1
                        }

                        // if (paint == null) {
                        //   replace_matrix(replacement, target, start_pos)
                        // }
                        // println("replace_list 9")
                        if (paint != null) {
                            val (source, dest) = paint
                            val source_line = lidx + source._1
                            val source_column = ridx + source._2
                            val dest_line = lidx + dest._1
                            val dest_column = ridx + dest._2
                            //style(dest_line)(dest_column) = style(source_line)(source_column).copy()
                            val source_style = style(source_line)(source_column)
                            //style(dest_line) = style(dest_line).updated(dest_column, source_style)
                            style(dest_line)(dest_column) = source_style
                            //style = style.updated(dest_line, )
                        }
                    //}

                    window = null
                    found = null
                    // if (true) {
                    // if (first_char_set.contains(target(lidx)(ridx))) {
                    // if (first_char_set.contains(target(lidx)(ridx)) && smartContains(start_pos)) {
                    if (smartContains(start_pos)) {
                        window = get_sub_matrix_view(target, start_pos, expected_size)
                        found = substitutions.getOrElse(window, null)
                    }
                   //found = null
                   //found = substitutions.find(x => equals_matrix(target, start_pos)(x._1)).map(_._2).getOrElse(null)
                   //foundTuple = substitutions.find(x => equals_matrix(target, start_pos)(x._1))
                   //found = foundTuple.map(_._2).getOrElse(null)
                //    println("found tuple")
                //    println(foundTuple.map(t => get_matrix_str(t._1)).getOrElse(""))
                //}
            }
        //}
            ridx +=1    
        }
        lidx +=1
    }

    target
}

class GitLines(var lines: Array[Array[Char]]) {
    var inner_paint: ((Int, Int), (Int, Int)) = null
    var max_column = -1
    var needle: SqueezedMatrixView = null
    var expected_size: (Int, Int) = null
    var substitutions: Map[SqueezedMatrixView,(Array[((Int, Int), Char)], ((Int, Int), (Int, Int)))] = Map()

    //def GitLines(lines: Vector[Vector[Char]]) {
       // this.lines = lines
        //this._paint = null
        //this.substitutions = {}
      //  this.max_column = -1
    //}

    def replace(args: String*) = {
        val size = (args.size, args(0).size) 
        if (this.expected_size != null) {
          if (size != expected_size) {
            throw new RuntimeException("Wrong expected size")
          }
        } else {
          this.expected_size = (args.size, args(0).size) 
        }
        this.needle = args.toArray.view.flatMap(_.toArray.view)
        this
    }

    def by(args: String*): Unit = {
        val size = (args.size, args(0).size) 
        if (this.expected_size != null) {
          if (size != expected_size) {
            throw new RuntimeException("Wrong expected size")
          }
        } else {
          this.expected_size = (args.size, args(0).size) 
        }

        val expected = this.needle

        val replacement = args.toArray.view.map(_.toArray.view)
        val replacementPoints = mutable.ArrayBuffer[((Int, Int), Char)]()
        //tuple([tuple(list(line)) for line in this.needle])

        
        for (i <- 0 until expected_size._1) {
          for (j <- 0 until expected_size._2) {
            if (needle(i*expected_size._2 + j) != replacement(i)(j)) {
              replacementPoints.append(((i, j), replacement(i)(j)))
            }
          }

        }

        this.substitutions = this.substitutions.updated(
            expected, 
            (replacementPoints.toArray, this.inner_paint))
    }

    def set_maxcolumn(max_column: Int): Unit = {
        this.max_column = max_column
    }

    def run(): Unit = {
        replace_list(this.lines, this.substitutions, this.expected_size, this.max_column)
        // replace_list_equals(this.lines, this.substitutions, this.max_column) //slower
        this.inner_paint = null
        this.needle = null
        this.substitutions = Map()
        this.max_column = -1
        this.expected_size = null
    }

    def paint(args: String*) = {
        if (args.size == 0) {
            this.inner_paint = null
        } else {
            var p = List.empty[(Int, Int)]
            for (i <- args.indices) {
                for (j <- args(i).indices) {
                    if (args(i)(j) == 'S') {
                        p = p :+ (i, j)
                    }
                }
            }

            for (i <- args.indices) {
                for (j <- args(i).indices) {
                    if (args(i)(j) == 'D') {
                        p = p :+ (i, j)
                    }
                }
            }

            this.inner_paint = (p(0), p(1))
        }
    }
}


val lines = new GitLines(graph_lines.toArray)


lines.paint()
lines.replace("/",
              "/").by("╭",
                      "╯")

lines.paint()
lines.replace("\\",
              "\\").by("╮",
                       "╰")


lines.run()
println("// Substitutions: " + took())
startMeasurament()

///==========================

mark()

lines.paint("S ",
            "D ")
lines.replace("| ",
              " ╮").by("| ",
                       "╰╮")

lines.paint()
lines.replace("|╯",
              "* ").by("├╯",
                       "* ")
lines.run()
print("Micro: 2x2 |=" + took())

//>=>=========================


lines.paint()
lines.replace("╰ ",
              "╭ ").by("| ",
                       "| ")

lines.paint("SD",
            "  ")
lines.replace("╰ ",
              " |").by("╰╮",
                       " |")

lines.paint("SD",
            "  ")
lines.replace("╰ ",
              " *").by("╰╮",
                       " *")

lines.paint("SD",
            "  ")
lines.replace("╰ ",
              " ╮").by("╰╮",
                       " |")

lines.run()
print(" ╰=" + took())

//>=>=========================

lines.paint("  ",
            "SD")
lines.replace(" *",
              "╭ ").by(" *",
                       "╭╯")

lines.paint("DS",
            "  ")
lines.replace(" ╯",
              "* ").by("╭╯",
                       "* ")

lines.paint("  ",
            "SD")
lines.replace(" |",
              "╭ ").by(" |",
                       "╭╯")

lines.paint("DS")
lines.replace(" ╯",
              "| ").by("╭╯",
                       "| ")

lines.paint("  ",
            "SD")
lines.replace(" ╯",
              "╭ ").by(" |",
                       "╭╯")

lines.run()
print(" ' '=" + took())

//>=>=========================

lines.paint(" D",
            " S")
lines.replace("* ",
              "|╮").by("*╮",
                       "||")
//lines.run()

///#????
///#lines.paint("  ",
///#            "SD")
///#lines.replace("|╯",
///#              "╭|").by("||",
///#                       "╭╯")




lines.run()
println(" *=" + took())
startMeasurament()

println("2x2 substitutions: " + tookFromMark())



//============================


lines.paint("D S",
            "   ")
lines.replace(" |╯",
              "╭| ").by("╭|╯",
                        "|| ")

lines.paint("D S",
            "   ")
lines.replace(" |─",
              "╭| ").by("╭|─",
                        "|| ")

//### new strategy

lines.paint()
lines.replace(" |╯",
              " | ").by(" ├╯",
                        " | ")

lines.paint()
lines.replace(" |╯",
              "╮| ").by(" ├╯",
                        "╮| ")



lines.run()
println("3x3 substitutions " + took())
startMeasurament()


// =================================

lines.set_maxcolumn(0)
lines.replace("|╭",
              "|╯").by("|╭",
                       "├╯")

lines.replace("||",
              "|╯").by("||",
                       "├╯")

lines.run()

println("last 2x2 substitutions " + took())


println("Global took: " + globalTook())

/*
 *  Too many escape codes since to make the line break before end of
 *  line is reached.
 *  With this function we try to compress style be reusing style of
 *  consecutive chars
 */
def compress_style(line_number: Int, line: Vector[Char]) = {
    //previous_idx = None
    //for idx, column in enumerate(line):
    //    if previous_idx is not None:
    //        if (style[line_number][previous_idx][0] == style[line_number][idx][0] and
    //                style[line_number][previous_idx][1] == style[line_number][idx][1]):
    //            style[line_number][previous_idx][1] = ""
    //            style[line_number][idx][0] = ""

    //    previous_idx = idx
}


def compress_escapes(line: String) = {
    //#return re.sub(r'(\x1b[[^m]*m)\x1bm(\x1b[[^m]*m)',
    //#              r'\1\2',
    //#              line)
    //final_line = line
    //#final_line = re.sub('\x1b\\[m(\x1b\\[[^m]+m)',
    //#                    r'\1',
    //#                    final_line)
    //#final_line = re.sub('\x1b\\[m(\x1b\\[[^m]+m)',
    //#                    r'\1',
    //#                    final_line)
    //final_line = re.sub('\x1b\\[m *\x1b\\[m',
    //                    '\x1b\\[m',
    //                    final_line)
    //final_line = re.sub('\x1b\\[m \\*\x1b\\[m',
    //                    '\x1b\\[m \\*',
    //                    final_line)

    //return final_line
    line
}


val final_ = lines.lines
for ((columns, line_number) <- final_.view.zipWithIndex) {
    //compress_style(line_number, columns)
    var line = ""
    var unstyled_line = ""
    for ((column, idx) <- columns.view.zipWithIndex) {
        breakable {
            if (idx > 80) {
                break
            }
            //#print(idx + column)
            //#print(idx + column)
            //#line += style[line_number][idx] + column
            unstyled_line += column
            //#if column == " ":
            //#    #line += column
            //#    line += style[line_number][idx][0] + column
            //#else:
            //#    #line += style[line_number][idx][0] + column + style
            line = line + style(line_number)(idx)._1 + column + style(line_number)(idx)._2
        }
    }

    line = line //+ messages(line_number)

    var not_empty_line = (unstyled_line.replace("|", "").replace("*", "").replace("", "")).size > 0
    not_empty_line = true
    if (not_empty_line) {
        line = line.replace('|', '│')
        //#line = line.replace('*', '┿')
        //#line = line.replace('|', 'H')
        line = compress_escapes(line)
        //#line = line.replace('\u001b', '\u001b')
        //#print(line + "<<" + str(len(line)), end='')
        //#print(line, end='')
        //#print()
        //#print(line + " <<" + str(len(line)) +"-"+ str(len(unstyled_line)))
        println(line)
        //#print(line[:40])
    }
}


//# good_
//# good chars for dot:
//# ┿
//# ╪
//# ┯
//# ╿
//# ┃

}}