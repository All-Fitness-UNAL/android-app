package com.allfitness.app.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.allfitness.app.R
import com.allfitness.app.core.ui.ScreenHeader

/** Marcador: la Task 5 rellena el contenido de Inicio. */
@Composable
fun HomeScreen() {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(R.string.tab_home))
    }
}
