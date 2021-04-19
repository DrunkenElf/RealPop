import java.util.*


interface Probability {
    fun next(): Float
}

class DynamicProbability(val min: Float, val max: Float): Probability {
    val random = Random()

    override fun next(): Float {
        return min + random.nextFloat() * (max - min)
    }
}