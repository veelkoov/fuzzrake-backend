package tasks

import config.Configuration
import database.Database
import database.entities.Specie
import io.github.oshai.kotlinlogging.KotlinLogging
import species.SpeciesLoader

private val logger = KotlinLogging.logger {}

class SpeciesSync(
    private val config: Configuration,
) {
    private val srcSpecies = SpeciesLoader().get()

    fun execute() {
        val db = Database(config.databasePath)

        db.transaction {
            val dbSpecies = Specie.all().associateBy(Specie::name).toMutableMap()

            // Create all visible species missing from the DB
            srcSpecies.getVisibleNames().minus(dbSpecies.keys).forEach { name ->
                logger.info { "Creating $name specie..." }

                val missingSpecie = Specie.new {
                    this.name = name
                }

                dbSpecies[name] = missingSpecie
            }

            // Remove all obsolete/no-longer-visible species from the DB
            dbSpecies.minus(srcSpecies.getVisibleNames()).forEach { (_, specie) ->
                logger.info { "Removing ${specie.name} specie..." }

                specie.delete()
            }
        }

        logger.info { "Done." }
    }
}
