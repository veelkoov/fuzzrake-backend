package database.repositories

import database.entities.CreatorSpecie
import database.tables.CreatorSpecies
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.selectAll

object CreatorSpeciesRepository {
    fun countCreatorsHavingSpeciesDefined() =
        CreatorSpecies.slice(CreatorSpecies.creator).selectAll().withDistinct().count()

    fun getSpecieNamesToCount(): Map<String, Int> {
        return CreatorSpecie.all().with(CreatorSpecie::specie)
            .groupBy { it.specie.name }
            .mapValues { it.value.size }
    }
}
