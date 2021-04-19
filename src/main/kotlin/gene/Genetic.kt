import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.util.*


class Genetic(val context: Context, val target: BufferedImage, val mutator: IncrementalMutator) {
    val random = Random()
    var genes: Array<Rectangle?> = arrayOfNulls(context.geneCount)
    var population: Array<Individual?> = arrayOfNulls(context.populationCount)

    // creates new chromosome
    fun newIndividual(): Individual {
        for (i in genes.indices) {
            genes[i] = newRectangle()
        }
        return Individual(genes, Double.MAX_VALUE)
    }

    //creates new population
    fun newPopulation(): Array<Individual?> {
        for (i in population.indices) {
            population[i] = newIndividual()
        }
        return population
    }

    // checks each gene of chromosome with original and mostFit images
    fun expressChromos(
        g: Graphics,
        individual: Individual?,
        tempCanvas: BufferedImage?,
        i: Int = 1,
        printFirs: Boolean = false
    ): Individual {
        if (i != -1) g.clearRect(0, 0, context.width, context.height)
        var count = 0
        var count1 = 0
        val updatedInd = Individual(
            (individual!!.dna.indices).map {
                val gene = individual.dna[it]
                if (tempCanvas != null) {
                    val tmp = context.imgDiff.checkShapeErr(gene!!, target, tempCanvas, true)
                    when (tmp.first.first) {
                        0 -> {
                            gene.draw(g, context)
                            count++
                            gene.apply {
                                error = tmp.second
                                isOld = !gene.isOld
                                canChange = false
                            }
                        }
                        2 -> {
                            val t = mutator.mutate(gene.apply {
                                isMutated = true
                            }, context.mutationProbability.next())
                            t
                        }
                        3 -> {
                            val t = mutator.mutate(gene.apply { isMutated = false }, context.mutationProbability.next())
                            t
                        }
                        1 -> {
                            val oldCol = Color(tmp.first.second!!)
                            count1++
                            val replacement = gene.apply {
                                color = intArrayOf(oldCol.red, oldCol.green, oldCol.blue, oldCol.alpha)
                                canChange = false
                                isOld = !gene.isOld
                                error = tmp.second
                            }
                            replacement.draw(g, context)
                            replacement
                        }

                        else -> {
                            gene.draw(g, context)
                            gene
                        }
                    }
                } else {
                    if (!gene!!.canChange) gene!!.draw(g, context)
                    gene
                }
            }.toTypedArray(), Double.MAX_VALUE, count + count1
        )

        return updatedInd
    }

    // creates new random color
    fun newColor(): IntArray {
        val color = IntArray(4)
        color.set(0, random.nextInt(256))
        color.set(1, random.nextInt(256))
        color.set(2, random.nextInt(256))
        color.set(3, random.nextInt(256))
        return color
    }

    // creates new random polygon
    private fun newRectangle(): Rectangle {
        val sizes = listOf(5, 7)
        val w = sizes.random()
        val h = sizes.random()
        val x = bound(random.nextInt(context.width), 0, context.width - w)
        val y = bound(random.nextInt(context.height), 0, context.height - h)
        val color = newColor()
        return Rectangle(color, x, y, w, h)
    }

}