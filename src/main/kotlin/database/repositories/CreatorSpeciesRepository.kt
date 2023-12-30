package database.repositories

import database.entities.CreatorSpecie
import database.tables.CreatorSpecies
import database.tables.Creators
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.select

object CreatorSpeciesRepository {
    fun countActiveCreatorsHavingSpeciesDefined() =
        (CreatorSpecies innerJoin Creators)
            .slice(CreatorSpecies.creator)
            .select { Creators.inactiveReason eq "" }
            .withDistinct()
            .count()

    fun getSpecieNamesToCount(): Map<String, Int> {
        return CreatorSpecie.all().with(CreatorSpecie::specie)
            .groupBy { it.specie.name }
            .mapValues { it.value.size }
    }
}
