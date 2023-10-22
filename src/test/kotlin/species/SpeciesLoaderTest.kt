package species

import testUtils.specieNamesSet
import kotlin.test.*

class SpeciesLoaderTest {
    private val subject = SpeciesLoader("/species/validChoicesTest.yaml").get()

    @Test
    fun `Tree roots are as expected`() {
        assertEquals(
            setOf("Most species", "Other", "Third root"),
            specieNamesSet(subject.getAsTree()),
        )

        assertEquals(0, subject.getByName("Third root").getParents().size)
        assertEquals(0, subject.getByName("Third root").getChildren().size)

        assertFailsWith<SpecieException> {
            subject.getByName("Nonexistent")
        }

        assertEquals(
            setOf("Felines", "Panthers", "Deer", "Some deer specie"),
            specieNamesSet(subject.getByName("Mammals").getDescendants()),
        )
    }
}
