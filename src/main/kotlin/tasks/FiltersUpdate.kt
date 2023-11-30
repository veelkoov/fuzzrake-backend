package tasks

import config.Configuration
import database.Database
import database.entities.CreatorSpecie
import database.helpers.getActive
import database.tables.CreatorSpecies
import database.tables.Creators
import filters.FilterData
import filters.SpecialItem
import filters.StandardItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.selectAll
import species.Specie
import species.SpeciesLoader

class FiltersUpdate(
    private val config: Configuration,
    private val database: Database = Database(config.databasePath),
) {
    fun execute() {
        database.transaction {
            val stats: Map<String, Int> = getCreatorSpeciesStats()
            val knownCount = CreatorSpecies.slice(CreatorSpecies.creator).selectAll().withDistinct().count()
            val allCount = Creators.getActive().count()

            val filters = FilterData(getSpeciesTree(stats), listOf(
                SpecialItem("Unknown", "?", (allCount - knownCount).toInt(), "unknown")
            ))

            println(Json.encodeToString(filters))
        }
    }

    private fun getSpeciesTree(stats: Map<String, Int>): List<StandardItem> {
        return getSpeciesList(SpeciesLoader().get().getAsTree(), stats)
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

    private fun getCreatorSpeciesStats(): Map<String, Int> {
        return CreatorSpecie.all().with(CreatorSpecie::specie)
            .groupBy { it.specie.name }
            .mapValues { it.value.size }
    }
}
