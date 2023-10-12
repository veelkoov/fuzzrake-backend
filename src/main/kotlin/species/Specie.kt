package species

data class Specie(
    val name: String,
) {
    private val parents: MutableSet<Specie> = mutableSetOf()
    private val children: MutableSet<Specie> = mutableSetOf()

    fun getParents() = parents.toSet()
    fun getChildren() = children.toSet()

    fun getAncestors(): Set<Specie> {
        return parents.plus(parents.map { it.getAncestors() }.flatten())
    }

    fun getDescendants(): Set<Specie> {
        return children.plus(children.map { it.getDescendants() }.flatten())
    }

    fun addChild(child: Specie) {
        if (child == this) {
            throw SpecieRecursionException("Cannot add ${child.name} as a child of $name")
        }

        if (getAncestors().contains(child)) {
            throw SpecieRecursionException("Recursion when adding child ${child.name} to $name")
        }

        children.add(child)
        child.parents.add(this)
    }

    fun addParent(parent: Specie) {
        if (parent == this) {
            throw SpecieRecursionException("Cannot add ${parent.name} as a parent of $name")
        }

        if (getDescendants().contains(parent)) {
            throw SpecieRecursionException("Recursion when adding parent ${parent.name} to $name")
        }

        parents.add(parent)
        parent.children.add(this)
    }
}
