

class Mutation {

    //selects genes for mutation
    fun perform(
        pair: Pair<Individual, Individual>,
        mutator: Mutator,
        mutationProbability: Float,
        order: Boolean
    ): Individual {
        val dnaLength = pair.first.dna.size
        val genes = arrayOfNulls<Rectangle?>(dnaLength)
        for (i in genes.indices) {

            val first = pair.first.dna[i]
            val second = pair.second.dna[i]
            if (!first!!.canChange && !second!!.canChange)
                genes[i] = if (first.error < second.error)
                    mutator.mutate(first, mutationProbability)
                else mutator.mutate(second, mutationProbability)
            else if (!first.canChange)
                genes[i] = mutator.mutate(first, mutationProbability)
            else if (!second!!.canChange)
                genes[i] = mutator.mutate(second, mutationProbability)
            else if (first.error < second.error/*Random().nextDouble() > .5*/) {
                genes[i] = mutator.mutate(first, mutationProbability)
            } else {
                genes[i] = mutator.mutate(second, mutationProbability)
            }
        }
        return Individual(genes, Double.MAX_VALUE)
    }
}