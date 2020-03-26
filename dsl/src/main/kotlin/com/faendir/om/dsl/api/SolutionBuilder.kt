package com.faendir.om.dsl.api

import com.faendir.om.dsl.OmDsl
import com.faendir.om.sp.part.*
import com.faendir.om.sp.solution.NonSolvedSolution
import com.faendir.om.sp.solution.Solution

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
class SolutionBuilder(var puzzle: String = "", var name: String = "", internal val parts: MutableList<Part> = mutableListOf()) {

    constructor(solution: Solution) : this(solution.puzzle, solution.name, solution.parts.toMutableList())

    @OmDsl
    fun arm(type: ArmType, initializer: Arm.() -> Unit) {
        val arm = Arm(0, 0 to 0, 0, 1, emptyList(), type)
        arm.initializer()
        parts += arm
    }

    @OmDsl
    fun glyph(type: GlyphType, initializer: Glyph.() -> Unit) {
        val glyph = Glyph(0 to 0, 0, type)
        glyph.initializer()
        parts += glyph
    }

    @OmDsl
    fun io(type: IOType, initializer: IO.() -> Unit) {
        val glyph = IO(0, 0 to 0, 0, type)
        glyph.initializer()
        parts += glyph
    }

    @OmDsl
    fun track(initializer: Track.() -> Unit) {
        val track = Track(0 to 0, listOf(0 to 0))
        track.initializer()
        parts += track
    }

    @OmDsl
    fun tape(initializer: Tape.() -> Unit) {
        val tape = Tape(this)
        tape.apply(initializer)
        tape.save()
    }

    fun build(): Solution {
        return NonSolvedSolution(puzzle, name, parts.toList())
    }
}