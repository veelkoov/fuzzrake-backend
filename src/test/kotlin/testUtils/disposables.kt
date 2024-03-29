package testUtils

import database.Database
import database.tables.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import java.nio.file.Path
import kotlin.io.path.createTempDirectory

fun disposableDatabase(block: (database: Database, transaction: Transaction) -> Unit) {
    val database = Database(":memory:")

    database.transaction {
        SchemaUtils.create(
            CreatorIds,
            CreatorOffersStatuses,
            Creators,
            CreatorUrls,
            CreatorUrlStates,
            CreatorVolatileDatas,
            Events,
        )

        block(database, it)
    }
}

fun <T>disposableDirectory(block: (tempDirectoryPath: Path) -> T): T {
    val tempDirectoryPath: Path = createTempDirectory()

    try {
        return block(tempDirectoryPath)
    } finally {
        if (!tempDirectoryPath.toFile().deleteRecursively()) {
            throw RuntimeException("Failed to cleanup '$tempDirectoryPath'")
        }
    }
}
