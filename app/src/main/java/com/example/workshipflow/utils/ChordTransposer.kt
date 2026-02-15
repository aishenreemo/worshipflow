package com.example.workshipflow.utils

object ChordTransposer {
    private val sharps = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private val flats = listOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")

    val keys = listOf("C", "C#", "Db", "D", "D#", "Eb", "E", "F", "F#", "Gb", "G", "G#", "Ab", "A", "A#", "Bb", "B")

    // Match [C], [Cmaj7], [C/E], or [ch]C[/ch]
    val bracketPattern = "\\[(?:ch])?([^]\\[]+?)(?:\\[/ch])?]".toRegex()

    // Robust chord regex: Root followed by common chord suffixes
    private val chordRegex = Regex(
        "^([A-G][#b]?)(m|min|maj|M|maj7|M7|maj9|M9|dim|aug|sus[24]?|add[249]?|no5|alt|\\d+|b5|#5|b9|#9|b11|#11|b13|#13|Δ|o|\\+|-)*(/[A-G][#b]?)?$",
        RegexOption.IGNORE_CASE
    )

    private val directives = setOf(
        "Verse", "Chorus", "Bridge", "Intro", "Outro", "Solo", "Instrumental",
        "Interlude", "Refrain", "Pre-chorus", "Ending", "Tag", "Coda"
    )

    fun isChord(candidate: String): Boolean {
        if (candidate.isBlank()) return false
        val clean = candidate.trim()
        if (directives.any { clean.equals(it, ignoreCase = true) }) return false
        return chordRegex.matches(clean)
    }

    fun shiftChord(chord: String, semitones: Int, preferFlats: Boolean = false): String {
        if (chord.isBlank()) return chord
        
        // Handle slash chords recursively
        if (chord.contains("/")) {
            val parts = chord.split("/")
            return parts.joinToString("/") { shiftChord(it.trim(), semitones, preferFlats) }
        }

        val clean = chord.trim()
        
        // Separate root from suffix (e.g., "C#" and "m7")
        val pattern = "^([A-G][#b]?)(.*)$".toRegex()
        val matchResult = pattern.find(clean) ?: return chord
        val root = matchResult.groupValues[1]
        val suffix = matchResult.groupValues[2]

        // Validate that it's actually a chord
        if (!isChord(clean)) return chord

        val rootIndex = sharps.indexOf(root).let { if (it == -1) flats.indexOf(root) else it }
        if (rootIndex == -1) return chord

        // Perform transposition
        var newIndex = (rootIndex + semitones) % 12
        if (newIndex < 0) newIndex += 12
        
        val scale = if (preferFlats) flats else sharps
        return scale[newIndex] + suffix
    }

    fun transposeContent(content: String, fromKey: String, toKey: String, preferFlats: Boolean = false): String {
        val semitones = getSemitonesBetween(fromKey, toKey)
        
        return content.lines().joinToString("\n") { line ->
            if (line.contains("[")) {
                // Handle bracketed chords
                bracketPattern.replace(line) { match ->
                    val inner = match.groupValues[1]
                    if (isChord(inner)) {
                        "[" + shiftChord(inner, semitones, preferFlats) + "]"
                    } else {
                        match.value // Keep [Chorus] etc as is
                    }
                }
            } else if (isChordLine(line)) {
                // Handle naked chords line
                transposeChordLine(line, semitones, preferFlats)
            } else {
                line
            }
        }
    }

    fun detectKey(content: String): String {
        // Use the first detected chord root as the key
        content.lines().forEach { line ->
            if (line.contains("[")) {
                bracketPattern.findAll(line).forEach { match ->
                    val inner = match.groupValues[1]
                    if (isChord(inner)) {
                        extractRoot(inner)?.let { return normalizeKeyName(it) }
                    }
                }
            } else if (isChordLine(line)) {
                line.trim().split(Regex("\\s+")).forEach { word ->
                    if (isChord(word)) {
                        extractRoot(word)?.let { return normalizeKeyName(it) }
                    }
                }
            }
        }
        return "C"
    }

    private fun extractRoot(chord: String): String? {
        val pattern = "^([A-G][#b]?)".toRegex()
        return pattern.find(chord)?.groupValues?.get(1)
    }

    private fun normalizeKeyName(root: String): String {
        val rootIndex = sharps.indexOf(root).let { if (it == -1) flats.indexOf(root) else it }
        if (rootIndex == -1) return root
        return keys.find { it == root } ?: sharps[rootIndex]
    }

    private fun isChordLine(line: String): Boolean {
        if (line.isBlank()) return false
        val words = line.trim().split(Regex("\\s+"))
        if (words.isEmpty()) return false
        return words.all { isChord(it) }
    }

    private fun transposeChordLine(line: String, semitones: Int, preferFlats: Boolean): String {
        val result = StringBuilder(line)
        val wordRegex = Regex("\\S+")
        wordRegex.findAll(line).toList().reversed().forEach { match ->
            val chord = match.value
            if (isChord(chord)) {
                val transposed = shiftChord(chord, semitones, preferFlats)
                result.replace(match.range.first, match.range.last + 1, transposed)
            }
        }
        return result.toString()
    }

    fun getSemitonesBetween(from: String, to: String): Int {
        if (from.isBlank() || to.isBlank()) return 0
        val pattern = "^([A-G][#b]?)".toRegex()
        val fromRoot = pattern.find(from)?.groupValues?.get(1) ?: return 0
        val toRoot = pattern.find(to)?.groupValues?.get(1) ?: return 0

        val fromIndex = sharps.indexOf(fromRoot).let { if (it == -1) flats.indexOf(fromRoot) else it }
        val toIndex = sharps.indexOf(toRoot).let { if (it == -1) flats.indexOf(toRoot) else it }

        if (fromIndex == -1 || toIndex == -1) return 0
        return (toIndex - fromIndex + 12) % 12
    }

    fun getNextKey(currentKey: String, direction: Int, preferFlats: Boolean = false): String {
        val currentIndex = sharps.indexOf(currentKey).let { if (it == -1) flats.indexOf(currentKey) else it }
        if (currentIndex == -1) return currentKey
        var nextIndex = (currentIndex + direction) % 12
        if (nextIndex < 0) nextIndex += 12
        return if (preferFlats) flats[nextIndex] else sharps[nextIndex]
    }
}
