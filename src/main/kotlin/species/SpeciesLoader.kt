package species

import data.Resource
import data.Yaml
import species.yaml.YamlSpecies
import species.yaml.YamlSubspecies

class SpeciesLoader(resource: String = "species.yaml") {
    private val builder = Species.Builder()
    private val result: Species

    init {
        val yamlSpecies = Yaml.parse(Resource.read(resource), YamlSpecies::class.java)

        yamlSpecies.validChoices.forEach { (name, subspecies) ->
            builder.addRootSpecie(createSpecie(name, subspecies))
        }

        result = builder.getResult()
    }

    fun get() = result

    private fun createSpecie(name: String, subspecies: YamlSubspecies): Specie.Builder {
        val specie = builder.getByNameCreatingMissing(name)

        subspecies.getItems().forEach { (name, subspecies) ->
            specie.addChild(createSpecie(name, subspecies))
        }

        return specie
    }
}
