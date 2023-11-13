package species

class CreatorSpeciesResolver(
    private val species: Species,
) {
    private val selfAndDescendantsCache = mutableMapOf<String, Set<String>>()
    private val mostSpecies: Specie = species.getByName("Most species") // grep-assumed-does-specie-when-artisan-has-only-doesnt
    private val other: Specie = species.getByName("Other") // grep-species-other

    fun resolveDoes(speciesDoes: Collection<String>, speciesDoesnt: Collection<String>): Set<String> {
        val assumedSpeciesDoes = if (speciesDoes.isEmpty() && speciesDoesnt.isNotEmpty()) {
            setOf(mostSpecies.name)
        } else {
            speciesDoes
        }

        val ordered = getOrderedDoesDoesnt(assumedSpeciesDoes, speciesDoesnt)

        val result = mutableSetOf<String>()

        ordered.forEach { (specie, does) ->
            val descendants = getVisibleSelfAndDescendants(specie)

            if (does) {
                descendants.forEach(result::add)
            } else {
                descendants.forEach(result::remove)
            }
        }

        return result
    }

    /**
     * @return Specie => Does?
     */
    fun getOrderedDoesDoesnt(speciesDoes: Collection<String>, speciesDoesnt: Collection<String>): Map<Specie, Boolean>
    {
        var result = listOf<Pair<Specie, Boolean>>()
            .plus(speciesDoes.map { specieName -> getSpecieOrOtherForUnusual(specieName) to true })
            .plus(speciesDoesnt.map { specieName -> getSpecieOrOtherForUnusual(specieName) to false })

        // Remove any "doesn't do" "Other"
        result = result.filter { (specie, does) -> specie != other || does }

        result = result.sortedWith { item1: Pair<Specie, Boolean>, item2: Pair<Specie, Boolean> ->
            val depthDiff = item1.first.getDepth() - item2.first.getDepth()

            if (0 != depthDiff) { depthDiff } else {
                if (item2.second) 1 else 0 - if (item1.second) 1 else 0
            }
        }

        return result.toMap()
    }

    private fun getVisibleSelfAndDescendants(self: Specie): Set<String>
    {
        return selfAndDescendantsCache.computeIfAbsent(self.name) {
            self.getSelfAndDescendants().map { it.name }.filter { species.getVisibleNames().contains(it) }.toSet()
        }
    }

    private fun getSpecieOrOtherForUnusual(specieName: String): Specie
    {
        return if (species.hasName(specieName)) {
            species.getByName(specieName)
        } else {
            other
        }
    }
}
