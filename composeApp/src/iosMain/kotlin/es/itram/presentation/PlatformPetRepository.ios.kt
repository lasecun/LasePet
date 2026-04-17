package es.itram.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import es.itram.data.repository.SqlDelightPetRepository
import es.itram.db.LasePetDatabase
import es.itram.domain.repository.PetRepository

@Composable
actual fun rememberPlatformPetRepository(): PetRepository {
    return remember {
        val driver = NativeSqliteDriver(
            schema = LasePetDatabase.Schema,
            name = "lasepet.db",
        )
        SqlDelightPetRepository(driver)
    }
}

