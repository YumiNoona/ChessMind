package app.chessmind.data

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class SavedPosition(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val fen: String,
    val bestMove: String,
    val evaluation: String,
    val createdAt: Long = System.currentTimeMillis(),
)

data class HistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val fen: String,
    val bestMove: String,
    val level: String,
    val createdAt: Long = System.currentTimeMillis(),
)

data class PracticeStats(val solved: Int = 0, val correct: Int = 0, val streak: Int = 0, val bestStreak: Int = 0)

data class UserSettings(
    val coordinates: Boolean = true,
    val legalHints: Boolean = true,
    val haptics: Boolean = true,
    val animations: Boolean = true,
    val highContrastBoard: Boolean = false,
    val darkMode: Boolean = false,
    val boardDepth: Boolean = false,
    val pieceDepth: Boolean = true,
    val boardTheme: Int = 0,
    val pieceStyle: Int = 0,
    val dragToMove: Boolean = true,
    val showLastMove: Boolean = true,
    val showMoveList: Boolean = true,
    val autoFlipFriend: Boolean = false,
    val defaultTimeMinutes: Int = 5,
    val aiMoveDelayMs: Int = 650,
    val aiStrength: Int = 0,
    val aiVariedOpenings: Boolean = true,
    val showAiThinking: Boolean = true,
)

data class LocalProfile(val name: String = "Chess player", val imageUri: String? = null)

data class PlayerProgress(
    val rating: Int = 500,
    val games: Int = 0,
    val wins: Int = 0,
    val draws: Int = 0,
)

class LocalStore(context: Context) {
    private val preferences = context.getSharedPreferences("chessmind_local", Context.MODE_PRIVATE)

    fun saved(): List<SavedPosition> = parseArray("saved") { json ->
        SavedPosition(
            id = json.getString("id"), name = json.getString("name"), fen = json.getString("fen"),
            bestMove = json.optString("bestMove"), evaluation = json.optString("evaluation"),
            createdAt = json.getLong("createdAt"),
        )
    }.sortedByDescending { it.createdAt }

    fun save(position: SavedPosition) {
        writeArray("saved", listOf(position) + saved()) { item ->
            JSONObject().put("id", item.id).put("name", item.name).put("fen", item.fen)
                .put("bestMove", item.bestMove).put("evaluation", item.evaluation).put("createdAt", item.createdAt)
        }
    }

    fun deleteSaved(id: String) = writeArray("saved", saved().filterNot { it.id == id }) { item ->
        JSONObject().put("id", item.id).put("name", item.name).put("fen", item.fen)
            .put("bestMove", item.bestMove).put("evaluation", item.evaluation).put("createdAt", item.createdAt)
    }

    fun history(): List<HistoryEntry> = parseArray("history") { json ->
        HistoryEntry(json.getString("id"), json.getString("fen"), json.optString("bestMove"), json.optString("level"), json.getLong("createdAt"))
    }.sortedByDescending { it.createdAt }

    fun addHistory(entry: HistoryEntry) = writeArray("history", (listOf(entry) + history()).take(100)) { item ->
        JSONObject().put("id", item.id).put("fen", item.fen).put("bestMove", item.bestMove)
            .put("level", item.level).put("createdAt", item.createdAt)
    }

    fun stats(): PracticeStats = PracticeStats(
        solved = preferences.getInt("practice_solved", 0),
        correct = preferences.getInt("practice_correct", 0),
        streak = preferences.getInt("practice_streak", 0),
        bestStreak = preferences.getInt("practice_best_streak", 0),
    )
    fun isOnboarded(): Boolean = preferences.getBoolean("onboarded", false)
    fun completeOnboarding() = preferences.edit { putBoolean("onboarded", true) }

    fun settings(): UserSettings = UserSettings(
        coordinates = preferences.getBoolean("setting_coordinates", true),
        legalHints = preferences.getBoolean("setting_legal_hints", true),
        haptics = preferences.getBoolean("setting_haptics", true),
        animations = preferences.getBoolean("setting_animations", true),
        highContrastBoard = preferences.getBoolean("setting_high_contrast", false),
        darkMode = preferences.getBoolean("setting_dark_mode", false),
        boardDepth = preferences.getBoolean("setting_board_depth", false),
        pieceDepth = preferences.getBoolean("setting_piece_depth", true),
        boardTheme = preferences.getInt("setting_board_theme", 0),
        pieceStyle = preferences.getInt("setting_piece_style", 0),
        dragToMove = preferences.getBoolean("setting_drag_to_move", true),
        showLastMove = preferences.getBoolean("setting_show_last_move", true),
        showMoveList = preferences.getBoolean("setting_show_move_list", true),
        autoFlipFriend = preferences.getBoolean("setting_auto_flip_friend", false),
        defaultTimeMinutes = preferences.getInt("setting_default_time", 5),
        aiMoveDelayMs = preferences.getInt("setting_ai_move_delay", 650),
        aiStrength = preferences.getInt("setting_ai_strength", 0),
        aiVariedOpenings = preferences.getBoolean("setting_ai_varied_openings", true),
        showAiThinking = preferences.getBoolean("setting_show_ai_thinking", true),
    )

    fun saveSettings(value: UserSettings) = preferences.edit {
        putBoolean("setting_coordinates", value.coordinates)
        putBoolean("setting_legal_hints", value.legalHints)
        putBoolean("setting_haptics", value.haptics)
        putBoolean("setting_animations", value.animations)
        putBoolean("setting_high_contrast", value.highContrastBoard)
        putBoolean("setting_dark_mode", value.darkMode)
        putBoolean("setting_board_depth", value.boardDepth)
        putBoolean("setting_piece_depth", value.pieceDepth)
        putInt("setting_board_theme", value.boardTheme)
        putInt("setting_piece_style", value.pieceStyle)
        putBoolean("setting_drag_to_move", value.dragToMove)
        putBoolean("setting_show_last_move", value.showLastMove)
        putBoolean("setting_show_move_list", value.showMoveList)
        putBoolean("setting_auto_flip_friend", value.autoFlipFriend)
        putInt("setting_default_time", value.defaultTimeMinutes)
        putInt("setting_ai_move_delay", value.aiMoveDelayMs)
        putInt("setting_ai_strength", value.aiStrength)
        putBoolean("setting_ai_varied_openings", value.aiVariedOpenings)
        putBoolean("setting_show_ai_thinking", value.showAiThinking)
    }

    fun profile(): LocalProfile = LocalProfile(
        name = preferences.getString("profile_name", "Chess player").orEmpty().ifBlank { "Chess player" },
        imageUri = preferences.getString("profile_image", null),
    )

    fun progress(): PlayerProgress = PlayerProgress(
        rating = preferences.getInt("play_rating", 500),
        games = preferences.getInt("play_games", 0),
        wins = preferences.getInt("play_wins", 0),
        draws = preferences.getInt("play_draws", 0),
    )

    fun saveProgress(value: PlayerProgress) = preferences.edit {
        putInt("play_rating", value.rating)
        putInt("play_games", value.games)
        putInt("play_wins", value.wins)
        putInt("play_draws", value.draws)
    }

    fun saveProfile(value: LocalProfile) = preferences.edit {
        putString("profile_name", value.name.trim().ifBlank { "Chess player" })
        if (value.imageUri == null) remove("profile_image") else putString("profile_image", value.imageUri)
    }

    fun recordPractice(correct: Boolean): PracticeStats {
        val old = stats()
        val streak = if (correct) old.streak + 1 else 0
        val next = PracticeStats(old.solved + 1, old.correct + if (correct) 1 else 0, streak, maxOf(old.bestStreak, streak))
        preferences.edit {
            putInt("practice_solved", next.solved); putInt("practice_correct", next.correct)
            putInt("practice_streak", next.streak); putInt("practice_best_streak", next.bestStreak)
        }
        return next
    }

    fun clearHistory() = preferences.edit { remove("history") }
    fun clearSaved() = preferences.edit { remove("saved") }

    fun exportJson(): String = JSONObject()
        .put("format", "chessmind-backup-v1")
        .put("saved", JSONArray(preferences.getString("saved", "[]")))
        .put("history", JSONArray(preferences.getString("history", "[]")))
        .put("practice", JSONObject().put("solved", stats().solved).put("correct", stats().correct)
            .put("streak", stats().streak).put("bestStreak", stats().bestStreak))
        .put("settings", JSONObject().put("coordinates", settings().coordinates)
            .put("legalHints", settings().legalHints).put("haptics", settings().haptics)
            .put("animations", settings().animations).put("highContrastBoard", settings().highContrastBoard)
            .put("darkMode", settings().darkMode).put("boardDepth", settings().boardDepth)
            .put("pieceDepth", settings().pieceDepth).put("boardTheme", settings().boardTheme)
            .put("pieceStyle", settings().pieceStyle).put("dragToMove", settings().dragToMove)
            .put("showLastMove", settings().showLastMove).put("showMoveList", settings().showMoveList)
            .put("autoFlipFriend", settings().autoFlipFriend).put("defaultTimeMinutes", settings().defaultTimeMinutes)
            .put("aiMoveDelayMs", settings().aiMoveDelayMs).put("aiStrength", settings().aiStrength)
            .put("aiVariedOpenings", settings().aiVariedOpenings).put("showAiThinking", settings().showAiThinking))
        .put("profile", JSONObject().put("name", profile().name).apply { profile().imageUri?.let { put("imageUri", it) } })
        .put("play", JSONObject().put("rating", progress().rating).put("games", progress().games)
            .put("wins", progress().wins).put("draws", progress().draws))
        .toString(2)

    fun importJson(value: String) {
        val root = JSONObject(value)
        require(root.getString("format") == "chessmind-backup-v1") { "This is not a ChessMind backup." }
        val saved = root.getJSONArray("saved")
        val history = root.getJSONArray("history")
        val practice = root.getJSONObject("practice")
        val settings = root.optJSONObject("settings")
        val profile = root.optJSONObject("profile")
        val play = root.optJSONObject("play")
        preferences.edit {
            putString("saved", saved.toString()); putString("history", history.toString())
            putInt("practice_solved", practice.optInt("solved")); putInt("practice_correct", practice.optInt("correct"))
            putInt("practice_streak", practice.optInt("streak")); putInt("practice_best_streak", practice.optInt("bestStreak"))
            if (settings != null) {
                putBoolean("setting_coordinates", settings.optBoolean("coordinates", true))
                putBoolean("setting_legal_hints", settings.optBoolean("legalHints", true))
                putBoolean("setting_haptics", settings.optBoolean("haptics", true))
                putBoolean("setting_animations", settings.optBoolean("animations", true))
                putBoolean("setting_high_contrast", settings.optBoolean("highContrastBoard", false))
                putBoolean("setting_dark_mode", settings.optBoolean("darkMode", false))
                putBoolean("setting_board_depth", settings.optBoolean("boardDepth", false))
                putBoolean("setting_piece_depth", settings.optBoolean("pieceDepth", true))
                putInt("setting_board_theme", settings.optInt("boardTheme", 0))
                putInt("setting_piece_style", settings.optInt("pieceStyle", 0))
                putBoolean("setting_drag_to_move", settings.optBoolean("dragToMove", true))
                putBoolean("setting_show_last_move", settings.optBoolean("showLastMove", true))
                putBoolean("setting_show_move_list", settings.optBoolean("showMoveList", true))
                putBoolean("setting_auto_flip_friend", settings.optBoolean("autoFlipFriend", false))
                putInt("setting_default_time", settings.optInt("defaultTimeMinutes", 5))
                putInt("setting_ai_move_delay", settings.optInt("aiMoveDelayMs", 650))
                putInt("setting_ai_strength", settings.optInt("aiStrength", 0))
                putBoolean("setting_ai_varied_openings", settings.optBoolean("aiVariedOpenings", true))
                putBoolean("setting_show_ai_thinking", settings.optBoolean("showAiThinking", true))
            }
            if (profile != null) {
                putString("profile_name", profile.optString("name", "Chess player"))
                profile.optString("imageUri").takeIf { it.isNotBlank() && it != "null" }?.let { putString("profile_image", it) }
            }
            if (play != null) {
                putInt("play_rating", play.optInt("rating", 500))
                putInt("play_games", play.optInt("games"))
                putInt("play_wins", play.optInt("wins"))
                putInt("play_draws", play.optInt("draws"))
            }
        }
    }

    private fun <T> parseArray(key: String, transform: (JSONObject) -> T): List<T> = runCatching {
        val array = JSONArray(preferences.getString(key, "[]"))
        buildList { for (index in 0 until array.length()) add(transform(array.getJSONObject(index))) }
    }.getOrDefault(emptyList())

    private fun <T> writeArray(key: String, values: List<T>, transform: (T) -> JSONObject) {
        val array = JSONArray()
        values.forEach { array.put(transform(it)) }
        preferences.edit { putString(key, array.toString()) }
    }
}
