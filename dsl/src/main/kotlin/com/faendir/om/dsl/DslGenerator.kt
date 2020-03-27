package com.faendir.om.dsl

import com.faendir.om.sp.base.Action
import com.faendir.om.sp.part.Arm
import com.faendir.om.sp.part.Glyph
import com.faendir.om.sp.part.IO
import com.faendir.om.sp.part.Track
import com.faendir.om.sp.solution.Solution

object DslGenerator {
    fun toDsl(solution: Solution): String {
        val parts = solution.parts.joinToString("\n") { part ->
            when (part) {
                is Arm -> """
                        arm(ArmType.${part.type.name}) {
                            number = ${part.number}
                            position = ${part.position.x} to ${part.position.y}
                            rotation = ${part.rotation}
                            size = ${part.size}
                        }
                    """
                is Glyph -> """
                    glyph(GlyphType.${part.type.name}) {
                        position = ${part.position.x} to ${part.position.y}
                        rotation = ${part.rotation}
                    }
                """
                is IO -> """
                    io(IOType.${part.type.name}) {
                        position = ${part.position.x} to ${part.position.y}
                        rotation = ${part.rotation}
                    }
                """
                is Track -> """
                    track {
                        position = ${part.position.x} to ${part.position.y}
                        positions = listOf(${part.positions.joinToString(", ") { "${it.x} to ${it.y}" }})
                    }
                """
                else -> throw IllegalArgumentException("Unknown part type $part")
            }.trimIndent()
        }
        val tape =  solution.parts.filterIsInstance<Arm>().joinToString(", ", "parallel(", ")") {
            """
{
    sequence(${it.number}) {
${toMethodCalls(it.steps).joinToString("\n").prependIndent("        ")}
    }
}
"""
        }
        return """
solution {
    puzzle = "${solution.puzzle}"
    name = "${solution.name}"
${parts.prependIndent("    ")}
    tape {
${tape.prependIndent("        ")}
    }
}
"""
    }
}

fun toMethodCalls(actions: List<Action>): List<String> {
    return if (actions.isEmpty()) emptyList()
    else {
        val tail = mutableListOf<String>()
        var head = mutableListOf(actions.first())
        val remaining = actions.toMutableList().apply { removeAt(0) }
        loop@ while (true) {
            val next = remaining.firstOrNull()
            when {
                next == null -> {
                    tail.add(head.asMethodCall())
                    break@loop
                }
                Action.EMPTY == next && Action.EMPTY == head.first() -> {
                    head.add(next)
                    remaining.removeAt(0)
                }
                else -> {
                    tail.add(head.asMethodCall())
                    head = mutableListOf(next)
                    remaining.removeAt(0)
                }
            }
        }
        return tail
    }
}

fun List<Action>.asMethodCall(): String {
    return when (this.first()) {
        Action.ROTATE_CLOCKWISE -> "rotateClockwise()"
        Action.ROTATE_COUNTERCLOCKWISE -> "rotateCounterClockwise()"
        Action.EXTEND -> "extend()"
        Action.RETRACT -> "retract()"
        Action.GRAB -> "grab()"
        Action.DROP -> "drop()"
        Action.PIVOT_CLOCKWISE -> "pivotClockwise()"
        Action.PIVOT_COUNTERCLOCKWISE -> "pivotCounterClockwise()"
        Action.FORWARD -> "forward()"
        Action.BACK -> "back()"
        Action.REPEAT -> "repeat()"
        Action.RESET -> "reset()"
        Action.NOOP -> "extendTape()"
        Action.EMPTY -> "wait(${this.size})"
    }
}