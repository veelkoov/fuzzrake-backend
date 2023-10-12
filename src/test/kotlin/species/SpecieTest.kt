package species

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SpecieTest {
    @Test
    fun `Species with equal names are equal`() {
        val specieA = Specie("The specie")
        val specieB = Specie("The specie")

        assertEquals(specieA, specieB)
    }

    @Test
    fun `Relationship is being set two-way`() {
        val parentA = Specie("Parent A")
        val childA = Specie("Child A")

        childA.addParent(parentA)
        assertContains(parentA.getChildren(), childA)

        val parentB = Specie("Parent B")
        val childB = Specie("Child B")

        parentB.addChild(childB)
        assertContains(childB.getParents(), parentB)
    }

    @Test
    fun `Test getParents() and getAncestors() sets`() {
        val top1a = Specie("Top 1A")
        val top1b = Specie("Top 1B")
        val top2a = Specie("Top 2A")
        val top2b = Specie("Top 2B")
        val middle1 = Specie("Middle 1")
        val middle2 = Specie("Middle 2")
        val bottom = Specie("Bottom")

        bottom.addParent(middle1)
        bottom.addParent(middle2)

        middle1.addParent(top1a)
        middle1.addParent(top1b)

        middle2.addParent(top2a)
        middle2.addParent(top2b)

        assertEquals(setOf("Top 1A", "Top 1B"),
            middle1.getParents().map { it.name }.toSet())
        assertEquals(middle1.getParents(), middle1.getAncestors())
        assertEquals(setOf("Top 1A", "Top 1B", "Top 2A", "Top 2B", "Middle 1", "Middle 2"),
            bottom.getAncestors().map { it.name }.toSet())
    }

    @Test
    fun `Test getChildren() and getDescendants() sets`() {
        val top = Specie("Top")
        val middle1 = Specie("Middle 1")
        val middle2 = Specie("Middle 2")
        val bottom1a = Specie("Bottom 1A")
        val bottom1b = Specie("Bottom 1B")
        val bottom2a = Specie("Bottom 2A")
        val bottom2b = Specie("Bottom 2B")

        top.addChild(middle1)
        top.addChild(middle2)

        middle1.addChild(bottom1a)
        middle1.addChild(bottom1b)

        middle2.addChild(bottom2a)
        middle2.addChild(bottom2b)

        assertEquals(setOf("Bottom 1A", "Bottom 1B"),
            middle1.getChildren().map { it.name }.toSet())
        assertEquals(middle1.getChildren(), middle1.getDescendants())
        assertEquals(setOf("Middle 1", "Middle 2", "Bottom 1A", "Bottom 1B", "Bottom 2A", "Bottom 2B"),
            top.getDescendants().map { it.name }.toSet())
    }

    @Test
    fun `Cannot recurse itself`() {
        val specie = Specie("Test specie")

        assertFailsWith<SpecieRecursionException> {
            specie.addChild(specie)
        }

        assertFailsWith<SpecieRecursionException> {
            specie.addChild(specie)
        }
    }

    @Test
    fun `Cannot recurse with multiple steps`() {
        val specieA = Specie("Test specie A")
        val specieB = Specie("Test specie B")
        val specieC = Specie("Test specie C")

        specieB.addParent(specieA)
        specieB.addChild(specieC)

        assertFailsWith<SpecieRecursionException> {
            specieA.addParent(specieB)
        }

        assertFailsWith<SpecieRecursionException> {
            specieA.addParent(specieC)
        }

        assertFailsWith<SpecieRecursionException> {
            specieC.addChild(specieB)
        }

        assertFailsWith<SpecieRecursionException> {
            specieC.addChild(specieA)
        }
    }
}
