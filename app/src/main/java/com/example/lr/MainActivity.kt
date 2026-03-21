package com.example.lr1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FilmApp()
                }
            }
        }
    }
}

enum class WatchStatus {
    PLANNED,
    WATCHING,
    DONE
}

data class Film(
    val id: Int,
    val name: String,
    val year: Int,
    val genre: String,
    val duration: Int,
    val status: WatchStatus = WatchStatus.PLANNED
)

val sampleFilmList = listOf(
    Film(1, "Deadpool and Wolverine", 2024, "Action", 128, WatchStatus.DONE),
    Film(2, "Wasabi", 2001, "Action", 94, WatchStatus.WATCHING),
    Film(3, "Me Before You", 2016, "Drama", 110, WatchStatus.PLANNED),
    Film(4, "Titanic", 1997, "Drama", 194, WatchStatus.DONE),
    Film(5, "The Fifth Element", 1997, "Action", 126, WatchStatus.WATCHING),
    Film(6, "Charlie and the Chocolate Factory", 2005, "Fantasy", 115, WatchStatus.PLANNED),
    Film(7, "Twilight", 2008, "Romance", 122, WatchStatus.PLANNED),
    Film(8, "Remember Me", 2010, "Drama", 112, WatchStatus.DONE),
    Film(9, "Love, Rosie", 2014, "Romance", 102, WatchStatus.WATCHING),
    Film(10, "Voice from the Stone", 2016, "Drama", 130, WatchStatus.PLANNED),
)

// State Holder
class FilmStateHolder(
    initialFilms: List<Film>
) {
    var films by mutableStateOf(initialFilms)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var filterStatus by mutableStateOf<WatchStatus?>(null)
        private set

    val filteredFilms: List<Film>
        get() {
            val withSearch = if (searchQuery.isBlank()) films
            else films.filter { it.name.contains(searchQuery, ignoreCase = true) }
            return if (filterStatus == null) withSearch
            else withSearch.filter { it.status == filterStatus }
        }

    val statistics: Statistics
        get() {
            val total = films.size
            val planned = films.count { it.status == WatchStatus.PLANNED }
            val watching = films.count { it.status == WatchStatus.WATCHING }
            val done = films.count { it.status == WatchStatus.DONE }
            return Statistics(total, planned, watching, done)
        }

    fun onSearchChange(newQuery: String) { searchQuery = newQuery }
    fun onFilterChange(status: WatchStatus?) { filterStatus = status }
    fun onNextStatus(filmId: Int) {
        films = films.map { film ->
            if (film.id == filmId) {
                val nextStatus = when (film.status) {
                    WatchStatus.PLANNED -> WatchStatus.WATCHING
                    WatchStatus.WATCHING -> WatchStatus.DONE
                    WatchStatus.DONE -> WatchStatus.PLANNED
                }
                film.copy(status = nextStatus)
            } else film
        }
    }
}

data class Statistics(val total: Int, val planned: Int, val watching: Int, val done: Int)

@Composable
fun FilmApp() {
    val stateHolder = remember { FilmStateHolder(sampleFilmList) }

    FilmListScreen(
        films = stateHolder.filteredFilms,
        searchQuery = stateHolder.searchQuery,
        onSearchChange = stateHolder::onSearchChange,
        filterStatus = stateHolder.filterStatus,
        onFilterChange = stateHolder::onFilterChange,
        onNextStatus = stateHolder::onNextStatus,
        statistics = stateHolder.statistics
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmListScreen(
    films: List<Film>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    filterStatus: WatchStatus?,
    onFilterChange: (WatchStatus?) -> Unit,
    onNextStatus: (Int) -> Unit,
    statistics: Statistics
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Film Watchlist") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Статистика простым текстом
            Text(
                text = "Всего: ${statistics.total} | В планах: ${statistics.planned} | Смотрю: ${statistics.watching} | Посмотрел: ${statistics.done}",
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Поиск
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search by name") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Фильтры (просто кнопки)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterStatus == null,
                    onClick = { onFilterChange(null) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = filterStatus == WatchStatus.PLANNED,
                    onClick = { onFilterChange(WatchStatus.PLANNED) },
                    label = { Text("Planned") }
                )
                FilterChip(
                    selected = filterStatus == WatchStatus.WATCHING,
                    onClick = { onFilterChange(WatchStatus.WATCHING) },
                    label = { Text("Watching") }
                )
                FilterChip(
                    selected = filterStatus == WatchStatus.DONE,
                    onClick = { onFilterChange(WatchStatus.DONE) },
                    label = { Text("Done") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Список или пустое состояние
            if (films.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ничего не найдено")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(films, key = { it.id }) { film ->
                        FilmCard(film, onNextStatus = { onNextStatus(film.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun FilmCard(film: Film, onNextStatus: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(film.name, fontWeight = FontWeight.Bold)
                Text("${film.year} - ${film.genre}")
                Text("Duration ${film.duration} min")
                Text("Status: ${film.status.name}")
            }
            Button(onClick = onNextStatus) {
                Text("Next")
            }
        }
    }
}