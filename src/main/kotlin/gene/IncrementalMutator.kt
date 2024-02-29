import java.util.*
import kotlin.math.max

interface Mutator {
    fun mutate(gene: Rectangle, probability: Float): Rectangle
}

class IncrementalMutator(val context: Context) : Mutator {
    val random = Random()

    // for color
    val maxColorDelta = 100
    val halfMaxColorDelta = maxColorDelta / 2

    //var sizes = listOf(5, 7, 9, 13)
    var sizes = listOf(2, 3, 5, 7, 9, 13, 18, 24, 30, 36, 42, 50)

    override fun mutate(gene: Rectangle, probability: Float): Rectangle {
        if (random.nextDouble() > probability) {
            return gene
        }

        return mutateRectangle(gene)
    }

    fun decreaseSize() {
        sizes.subList(0, max(1, sizes.size-1))
        /*when (sizes.size) {
            3 -> sizes = sizes.subList(0, max(1, sizes.size-1))
            4 -> sizes = sizes.subList(0, sizes.size-1)
            5 -> sizes = sizes.subList(0, sizes.size-1)
            6 -> sizes = sizes.subList(0, sizes.size-1)
            7 -> sizes = sizes.subList(0, sizes.size-1)
            8 -> sizes = sizes.subList(0, sizes.size-1)
            //3 -> sizes = listOf(7, 5)
            //3 -> sizes = listOf(3, 5)
            //4 -> sizes = listOf(3, 5, 7)
        }*/
    }

    private fun mutateRectangle(gene: Rectangle): Rectangle {
        //if polygon is locked, mutate color and set to "not locked" (isMutated = false)
        if (gene.canChange && gene.isMutated) {
            when (random.nextInt(4)) {
                0 -> mutateColor(gene, 0) // red
                1 -> mutateColor(gene, 1) // green
                2 -> mutateColor(gene, 2) // blue
                3 -> mutateColor(gene, 3) // alpha
            }
            gene.isMutated = false
            return gene
        }
        // if isOld, mutate position
        else if (gene.isOld) {
            when (random.nextInt(2)) {
                0 -> {
                    val newPosition = moveRectangle(gene.x, gene.y)
                    gene.x = bound(
                        if (random.nextBoolean())
                            if (random.nextBoolean()) newPosition.first + random.nextInt(gene.w) + 10 else newPosition.first - random.nextInt(
                                gene.w
                            ) + 10
                        else
                            random.nextInt(context.width),
                        0,
                        context.width - gene.w
                    )
                    gene.y = bound(
                        if (random.nextBoolean())
                            if (random.nextBoolean())
                                newPosition.second + random.nextInt(gene.h) + 10
                            else newPosition.second - random.nextInt(gene.h) + 10
                        else
                            random.nextInt(context.height),
                        0,
                        context.height - gene.h
                    )
                }

                1 -> {
                    //gene.w = bound(sizes.random(), 3, 5)
                    if (random.nextBoolean()) {
                        gene.w = bound(sizes.random(), 3, 5)
                        gene.h = bound(sizes.random(), 7, 30)
                    } else {
                        gene.w = bound(sizes.random(), 7, 30)
                        gene.h = bound(sizes.random(), 3, 5)
                    }
                    /* gene.w = bound(sizes.random(), 7, 30)
                     gene.h = bound(sizes.random(), 3, 5)*/
                    /* gene.w = sizes.random()
                     gene.h = sizes.random()*/

                }
            }
            gene.isOld = false
            gene.canChange = true
            return gene
        } else {
            //mutate color
            when (random.nextInt(4)) {
                0 -> mutateColor(gene, 0) // red
                1 -> mutateColor(gene, 1) // green
                2 -> mutateColor(gene, 2) // blue
                3 -> mutateColor(gene, 3) // alpha
            }
            gene.isOld = true
            gene.canChange = true
            return gene
        }
    }


    private fun mutateColor(gene: Shape, i: Int) {
        gene.color[i] = bound(gene.color[i] + random.nextInt(maxColorDelta) - halfMaxColorDelta, 0, 255)
    }

    private fun moveRectangle(x: Int, y: Int): Pair<Int, Int> {
        val randomInt = random.nextInt(30)
        if (randomInt % 5 == 0) {
            if (randomInt % 3 == 0) {
                return Pair(
                    x + random.nextInt(200),
                    y - random.nextInt(200)
                )
            }
            return Pair(
                x - random.nextInt(200),
                y + random.nextInt(200)
            )
        }
        return Pair(x, y)
    }
}