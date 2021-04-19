data class Context(
    val width: Int,
    val height: Int,
    var geneCount: Int = 128,
    var populationCount: Int = 20,
    val mutationProbability: Probability,
    val maxPolygonSize: Int = 5,
    val pixelSize: Int = 8,
    val useAlpha: Boolean = true,
    val imgDiff: ImageDifference
)

data class Individual(val dna: Array<Rectangle?>, var fitness: Double, var numOfPassed: Int = 0)