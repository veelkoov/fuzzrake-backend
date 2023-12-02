package tasks

import config.Configuration
import database.Database
import database.repositories.CreatorSpeciesRepository
import database.repositories.CreatorsRepository
import filters.FilterData
import filters.SpecialItem
import filters.StandardItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import species.Specie
import species.SpeciesLoader

class FiltersUpdate(
    private val config: Configuration,
    private val database: Database = Database(config.databasePath),
) {
    fun execute() {
        database.transaction {
            val stats: Map<String, Int> = CreatorSpeciesRepository.getSpecieNamesToCount()

            val items = getSpeciesList(SpeciesLoader().get().getAsTree(), stats)
            val specialItems = listOf(SpecialItem("Unknown", "?", countUnknown(), "unknown"))

            val filters = FilterData(items, specialItems)

            println(Json.encodeToString(filters))
        }
    }

    private fun countUnknown(): Int {
        val knownCount = CreatorSpeciesRepository.countCreatorsHavingSpeciesDefined()
        val allCount = CreatorsRepository.countActive()

        return (allCount - knownCount).toInt()
    }

    private fun getSpeciesList(species: Collection<Specie>, stats: Map<String, Int>): List<StandardItem> {
        return species.filterNot(Specie::getHidden).map { specie -> specieToStandardItem(specie, stats) }
    }

    private fun specieToStandardItem(specie: Specie, stats: Map<String, Int>): StandardItem {
        return StandardItem(
            specie.name,
            specie.name,
            stats[specie.name] ?: 0,
            getSpeciesList(specie.getChildren(),stats),
        )
    }
}
