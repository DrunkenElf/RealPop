/**
 * Created by kenny on 5/23/16.
 */


import kotlinx.coroutines.*
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.GridLayout
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.WindowConstants
import kotlin.math.floor

fun main(args: Array<String>) {
    //println(3 + floor(3/2) - 1)
    GenDraw(System.currentTimeMillis()).run()
}


class GenDraw(val start: Long) {
    val fileName = "nature.jpg"
    val target = ImageIO.read(Thread.currentThread().contextClassLoader.getResource(fileName))
    val geneCount = if (target.width * target.height > 2000000) 60000
     else if (target.width * target.height in 300001..499999) 10000
     else 30000

    val context = Context(
        width = target.width,
        height = target.height,
        geneCount = geneCount,
        populationCount = 10,
        mutationProbability = DynamicProbability(0.001f, 0.5f),
        maxPolygonSize = 12,
        useAlpha = true,
        imgDiff = ImageDifference(target)
    )
    val mutator = IncrementalMutator(context)
    val genetic = Genetic(context, target, mutator)
    val type = 4
    val crossOver = Mutation()

    val mostFitCanvas: BufferedImage = BufferedImage(context.width, context.height, BufferedImage.TYPE_INT_ARGB)
    val mostFitCanvasGraphics = mostFitCanvas.graphics

    val tempCanvas = BufferedImage(context.width, context.height, BufferedImage.TYPE_INT_ARGB)

    val saveOutputFrequency = 50
    var population = genetic.newPopulation()
    var lastSavedMin = 0
    var lastSavedMillis = 0


    // BufferedImages for each chromosome in population
    val canvas: List<BufferedImage> = List(context.populationCount) { index ->
        BufferedImage(
            context.width,
            context.height,
            BufferedImage.TYPE_INT_ARGB
        )
    }


    fun run() {
        val frame = JFrame()
        println(geneCount)
        println(target.width * target.height)
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

        frame.setSize(target.width + 50, target.height + 50)
        frame.isVisible = true
        frame.title = "genes = ${context.geneCount}; population size = ${context.populationCount}"
        val f = File("evolved")
        f.mkdirs()
        var i = 0

        var k = 0
        val panel = object : JPanel() {
            override fun getPreferredSize(): Dimension {
                return Dimension(target.width, target.height)
            }
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                genetic.expressChromos(
                    mostFitCanvasGraphics,
                    population.first()!!,
                    null,
                    -1, /*if (k <= 0) true else*/
                )
                g.drawImage(mostFitCanvas, 0, 0, context.width, context.height, this)

                //after each 50 iterations saves image
                if ((i % saveOutputFrequency == 0)) {
                    ImageIO.write(
                        mostFitCanvas, "png",
                        File("evolved/evolved1_${fileName.replace("jpg", "png")}")
                    )
                }
                k++
            }
        }
        //panel.
        frame.add(panel)
        panel.revalidate()
        mostFitCanvasGraphics.color = Color.BLACK
        mostFitCanvasGraphics.clearRect(0,0, target.width, target.height)
        panel.repaint()
        Thread.sleep(1000)
        println("start of the Iteration")
        // body of our iteration
        do {
            runBlocking {
                population = evalFitness()
            }

            println(
                "iteration: ${i} fitness: ${population.first()!!.fitness}" + if (i % 10 == 0) "time: ${
                    timeFormat(System.currentTimeMillis() - start)
                }" else ""
            )
            //after each 200 iterations increases accuracy
            if (i > 0 && i % 10 == 0) {
                context.imgDiff.incAccuracy(i)
                //after each 1000 iterations decreases max allowed size of polygon
                if (i % 100 == 0) mutator.decreaseSize()
            }
            panel.repaint()
            buildNextGeneration(i)
            i++

        } while (population.first()!!.fitness > 0)
    }

    fun timeFormat(millis: Long): String {
        lastSavedMin = (millis / 1000 / 60).toInt()
        lastSavedMillis = (millis / 1000 % 60).toInt()
        return "${lastSavedMin}:${lastSavedMillis}"
    }


    // builds new generation by use of crossover and mutation
    private fun buildNextGeneration(change: Int) {
        for (i in 1 until population.size) {
            // we use elitism for crossover
            val one = population[0]!!
            val two = population[i]!!
            population[i] = (crossOver
                .perform(
                    Pair(one, two),
                    mutator,
                    context.mutationProbability.next(),
                    change % 2 == 0
                ))
        }
    }

    // asynchronously compute fitnes of each Individual
    // returns modified Individuals because drawing gene(poligon) we evaluate its local fitness
    suspend fun evalFitness(): Array<Individual?> {
        val corouts: List<Deferred<Individual?>> = (canvas.indices).map { index ->
            CoroutineScope(Dispatchers.IO).async {
                someFun(index)
            }
        }
        return corouts.awaitAll().sortedBy { it!!.fitness }.toTypedArray()
    }

    fun someFun(i: Int): Individual? {
        val indiv = genetic.expressChromos(canvas[i].graphics, population[i], mostFitCanvas, printFirs = i == 1)
        // combine mostfitcanvas with created chromosome(element of population)
        val tmp = tempCanvas.graphics
        tmp.drawImage(mostFitCanvas, 0, 0, null)
        tmp.drawImage(canvas[i], 0, 0, null)
        indiv.fitness = context.imgDiff.compare(tempCanvas, target)
        tmp.dispose()
        canvas[i].graphics.dispose()
        return indiv
    }
}