package es.itram.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import es.itram.data.repository.SqlDelightPetRepository
import es.itram.db.LasePetDatabase
import es.itram.domain.repository.PetRepository

@Composable
actual fun rememberPlatformPetRepository(): PetRepository {
    val context = LocalContext.current.applicationContext
    return remember(context) { createPetRepository(context) }
}

private fun createPetRepository(context: Context): PetRepository =
    SqlDelightPetRepository(
        AndroidSqliteDriver(
            schema = LasePetDatabase.Schema,
            context = context,
            name = "lasepet.db",
        ),
    )

