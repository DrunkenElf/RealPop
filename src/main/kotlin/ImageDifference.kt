import java.awt.Color
import java.awt.image.BufferedImage
import java.lang.Double.min
import java.lang.String
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

interface FitnessFunction {
    fun compare(bi: BufferedImage, bi2: BufferedImage): Double
}


class ImageDifference(val target: BufferedImage) : FitnessFunction {
    val step = 3
    var allowedError = 35
    val rgbTarget: Array<IntArray> = Array(target.width) { x: Int ->
        IntArray(target.height) { y -> target.getRGB(x, y) }
    }

    // finds fitness error of whole image
    override fun compare(bi: BufferedImage, bi2: BufferedImage): Double {
        var error = 0.0
        (0 until bi.width step step).forEach { x ->
            (0 until bi.height step step).forEach { y ->
                val rgb = bi.getRGB(x, y)
                val rgb2 = rgbTarget[x][y]
                error += findDiff(Color(rgb), Color(rgb2))
            }
        }
        return error
    }

    // fitness function itself
    fun findDiff(col1: Color, col2: Color): Double {
        val mean = (col1.red + col2.red) / 2
        val r = (col1.red - col2.red) /*and 0xFF*/
        val g = (col1.green - col2.green)
        val b = (col1.blue - col2.blue)
        return sqrt(
            ((((512 + mean) * r * r) shr 8) +
                    4 * g * g +
                    (((767 - mean) * b * b) shr 8)).toDouble()
        )
    }

    fun incAccuracy() {
        allowedError = max(7, allowedError - 1)
        println("accuracy increased; new delta = $allowedError")
    }

    // checks shape for availability to be passed
    fun checkShapeErr(
        shape: Rectangle,
        bi2: BufferedImage,
        prevCanvas: BufferedImage,
        insi: Boolean
    ): Pair<Pair<Int, Int?>, Double> {
        var errNew = 0.0
        var errOld = 0.0

        val stepx = with(shape.w) {
            if (this == 5) return@with 1
            else if (this == 7) return@with 2
            else return@with 1
        }
        val stepy = with(shape.h) {
            if (this == 5) return@with 1
            else if (this == 7) return@with 2
            else return@with 1
        }

        val tempList = ArrayList<Pair<Int, Pair<Int, Int>>>(50)
        val color = Color(shape.color[0], shape.color[1], shape.color[2], shape.color[3]).rgb
        var i = 0

        (shape.x until (shape.w + shape.x) step stepx).forEach { x ->
            (shape.y until (shape.h + shape.y) step stepy).forEach { y ->
                if (x >= bi2.width || y >= bi2.height)
                else {
                    val rgb2 = rgbTarget[x][y]
                    val rgbPrev = prevCanvas.getRGB(x, y)
                    tempList.add(Pair(rgbPrev, Pair(x, y)))
                    errNew += findDiff(Color(color), Color(rgb2))
                    errOld += findDiff(Color(rgbPrev), Color(rgb2))
                    i++
                }
            }
        }
        val colOld: Int? = if (tempList.size >= 1) getAveragCol(tempList) else null
        val colTar = rgbTarget[
                kotlin.math.min(shape.x + shape.w / 2 - 1, target.width-1)][
                kotlin.math.min(shape.y + shape.h / 2 - 1, target.height-1)]
        if (errNew < errOld) {
            if (findDiff(Color(color), Color(colTar)) < allowedError + 5)
                return Pair(Pair(0, null), errNew)
            else return Pair(Pair(2, color), errNew)
        } else {
            if (polygonDiff(tempList) < allowedError + 20)
                return Pair(Pair(1, colOld), errOld)
            else return Pair(Pair(3, colOld), errOld)
        }
    }

    fun getAveragCol(colos: ArrayList<Pair<Int, Pair<Int, Int>>>): Int {
        val rgbChannels = arrayOf(0, 0, 0, 0)
        colos.forEach {
            val tmpColor = Color(it.first)
            rgbChannels[0] += tmpColor.red
            rgbChannels[1] += tmpColor.green
            rgbChannels[2] += tmpColor.blue
            rgbChannels[3] += tmpColor.alpha
        }
        return Color(
            rgbChannels[0] / colos.size,
            rgbChannels[1] / colos.size,
            rgbChannels[2] / colos.size,
            rgbChannels[3] / colos.size
        ).rgb
    }

    fun polygonDiff(colos: ArrayList<Pair<Int, Pair<Int, Int>>>): Double {
        var error = 0.0
        var i = 0
        colos.forEach {
            error += findDiff(Color(it.first), Color(rgbTarget[it.second.first][it.second.second]))
        }
        return error
    }
}