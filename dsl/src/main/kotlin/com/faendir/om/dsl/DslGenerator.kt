package com.faendir.om.dsl

import com.faendir.om.parser.solution.model.Action
import com.faendir.om.parser.solution.model.Solution
import com.faendir.om.parser.solution.model.part.*


object DslGenerator {
    fun toDsl(solution: Solution): String {
        val parts = solution.parts.sortedWith(compareBy({
            when (it) {
                is Arm -> 0
                is Glyph -> 1
                is IO -> 2
                is Track -> 3
                is Conduit -> 4
                else -> 5
            }
        }, {
            when (it) {
                is Arm -> it.number
                is Glyph -> it.type.ordinal
                is IO -> it.index
                is Conduit -> it.id
                else -> 0
            }
        })).joinToString("\n") { part ->
            when (part) {
                is Arm -> """
                        arm(${part.type.name}) {
                            number = ${part.number}
                            position = ${part.position.x} to ${part.position.y}
                            rotation = ${part.rotation}
                            size = ${part.size}
                        }
                    """
                is Glyph -> """
                    glyph(${part.type.name}) {
                        position = ${part.position.x} to ${part.position.y}
                        rotation = ${part.rotation}
                    }
                """
                is IO -> """
                    io(${part.type.name}) {
                        index = ${part.index}
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
                is Conduit -> """
                    conduit {
                        id = ${part.id}
                        position = ${part.position.x} to ${part.position.y}
                        rotation = ${part.rotation}
                    }
                """
                else -> throw IllegalArgumentException("Unknown part type $part")
            }.trimIndent()
        }
        val tape = solution.parts.filterIsInstance<Arm>().joinToString(", ", "parallel(", ")") {
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
