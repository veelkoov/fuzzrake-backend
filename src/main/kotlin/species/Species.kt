package species

class Species(
    private val byName: Map<String, Specie>,
    private val asTree: List<Specie>,
) {
    fun getByName(name: String): Specie {
        return byName[name] ?: throw SpecieException("No specie named '$name'")
    }

    fun getNames() = byName.keys.toSet()

    fun getAsTree() = asTree.toList()

    class Builder {
        private val byName: MutableMap<String, Specie.Builder> = mutableMapOf()
        private val asTree: MutableList<Specie.Builder> = mutableListOf()

        fun getByNameCreatingMissing(name: String): Specie.Builder {
            return byName.computeIfAbsent(name) {
                Specie.Builder(name)
            }
        }

        fun addRootSpecie(rootSpecie: Specie.Builder) {
            asTree.add(rootSpecie)
        }

        fun getResult(): Species {
            return Species(
                byName.mapValues { it.value.getResult() },
                asTree.map { it.getResult() }
            )
        }
    }
}
