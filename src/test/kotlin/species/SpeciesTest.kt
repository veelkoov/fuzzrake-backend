package species

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SpeciesTest {
    @Test
    fun testGetNames() {
        val subject = getTestInstance()

        assertEquals(
            setOf("Root 1", "Middle 1", "Leaf 1A", "Leaf 1B", "Root 2", "Leaf 2"),
            subject.getNames(),
        )
    }

    @Test
    fun testGetAsTree() {
        val subject = getTestInstance()

        assertEquals(
            setOf("Root 1", "Root 2"),
            subject.getAsTree().map { it.name }.toSet(),
        )
    }

    @Test
    fun testGetByName() {
        val subject = getTestInstance()

        assertEquals("Leaf 1B", subject.getByName("Leaf 1B").name)
        assertFailsWith<SpecieException> { subject.getByName("Middle 2") }
    }

    private fun getTestInstance(): Species {
        val species = Species.Builder()

        val root1 = species.getByNameCreatingMissing("Root 1")
        val middle1 = species.getByNameCreatingMissing("Middle 1")
        root1.addChild(middle1)

        val leaf1a = species.getByNameCreatingMissing("Leaf 1A")
        val leaf1b = species.getByNameCreatingMissing("Leaf 1B")
        middle1.addChild(leaf1a)
        middle1.addChild(leaf1b)

        val root2 = species.getByNameCreatingMissing("Root 2")
        val leaf2 = species.getByNameCreatingMissing("Leaf 2")
        root2.addChild(leaf2)

        species.addRootSpecie(root1)
        species.addRootSpecie(root2)

        return species.getResult()
    }
}
