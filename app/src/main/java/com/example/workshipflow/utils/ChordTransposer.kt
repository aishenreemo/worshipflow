package com.example.workshipflow.utils

object ChordTransposer {
    private val sharps = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private val flats = listOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")

    fun transpose(chord: String, fromKey: String, toKey: String): String {
        val semitones = getSemitonesBetween(fromKey, toKey)
        return transposeChord(chord, semitones)
    }

    private fun getSemitonesBetween(from: String, to: String): Int {
        val fromIndex = (sharps.indexOf(from).takeIf { it != -1 } ?: flats.indexOf(from))
        val toIndex = (sharps.indexOf(to).takeIf { it != -1 } ?: flats.indexOf(to))
        if (fromIndex == -1 || toIndex == -1) return 0
        return (toIndex - fromIndex + 12) % 12
    }

    private fun transposeChord(chord: String, semitones: Int): String {
        val pattern = "^([A-G][#b]?)(.*)$".toRegex()
        val matchResult = pattern.find(chord) ?: return chord
        val root = matchResult.groupValues[1]
        val suffix = matchResult.groupValues[2]

        val rootIndex = (sharps.indexOf(root).takeIf { it != -1 } ?: flats.indexOf(root))
        if (rootIndex == -1) return chord

        val newIndex = (rootIndex + semitones) % 12
        // Default to sharps for now, ideally depends on target key
        return sharps[newIndex] + suffix
    }

    fun transposeContent(content: String, fromKey: String, toKey: String): String {
        val semitones = getSemitonesBetween(fromKey, toKey)
        if (semitones == 0) return content

        val chordPattern = "\\[([^\\]]+)\\]".toRegex()
        return chordPattern.replace(content) { matchResult ->
            val chord = matchResult.groupValues[1]
            "[${transposeChord(chord, semitones)}]"
        }
    }
}
