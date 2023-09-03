package com.faendir.om.dsl.api

import com.faendir.om.dsl.OmDsl
import com.faendir.om.parser.solution.model.NonSolvedSolution
import com.faendir.om.parser.solution.model.Position
import com.faendir.om.parser.solution.model.Solution
import com.faendir.om.parser.solution.model.SolvedSolution
import com.faendir.om.parser.solution.model.part.*

@OmDsl
fun solution(initializer: SolutionBuilder.() -> Unit): Solution {
    val solution = SolutionBuilder()
    solution.initializer()
    return solution.build()
}

fun Solution.toBuilder(initializer: SolutionBuilder.() -> Unit = {}): SolutionBuilder {
    return SolutionBuilder(this).apply { initializer() }
}

@OmDsl
class ScoreBuilder(var cost: Int? = null, var cycles: Int? = null, var area: Int? = null, var instructions: Int? = null) {
    constructor(solution: SolvedSolution) : this(solution.cost, solution.cycles, solution.area, solution.instructions)
}

@OmDsl
class SolutionBuilder(var puzzle: String = "", var name: String = "", private val score: ScoreBuilder = ScoreBuilder(), internal val parts: MutableList<Part> = mutableListOf()) {

    constructor(solution: Solution) : this(
        solution.puzzle,
        solution.name,
        if (solution is SolvedSolution) ScoreBuilder(solution) else ScoreBuilder(),
        solution.parts.toMutableList()
    )

    @OmDsl
    fun score(initializer: ScoreBuilder.() -> Unit) {
        score.apply(initializer)
    }

    @OmDsl
    fun arm(type: ArmType, initializer: Arm.() -> Unit) {
        val arm = Arm((parts.map { it.number }.maxOrNull() ?: 0) + 1, Position(0, 0), 0, 1, emptyList(), type)
        arm.initializer()
        parts += arm
    }

    @OmDsl
    fun glyph(type: GlyphType, initializer: Glyph.() -> Unit) {
        val glyph = Glyph(Position(0, 0), 0, 1, type)
        glyph.initializer()
        parts += glyph
    }

    @OmDsl
    fun io(type: IOType, initializer: IO.() -> Unit) {
        val glyph = IO(0, Position(0, 0), 0, 1, type)
        glyph.initializer()
        parts += glyph
    }

    @OmDsl
    fun track(initializer: Track.() -> Unit) {
        val track = Track(Position(0, 0), listOf(Position(0, 0)))
        track.initializer()
        parts += track
    }

    @OmDsl
    fun conduit(initializer: Conduit.() -> Unit) {
        val conduit = Conduit(Position(0, 0), 0, 100, listOf(Position(0, 0)))
        conduit.initializer()
        parts += conduit
    }

    @OmDsl
    fun tape(initializer: Tape.() -> Unit) {
        val tape = Tape(this)
        tape.apply(initializer)
        tape.save()
    }

    fun build(): Solution {
        val cost = score.cost
        val cycles = score.cycles
        val area = score.area
        val instructions = score.instructions
        return if (cost != null && cycles != null && area != null && instructions != null) {
            SolvedSolution(puzzle, name, cycles, cost, area, instructions, parts.toList())
        } else {
            NonSolvedSolution(puzzle, name, parts.toList())
        }
    }
}