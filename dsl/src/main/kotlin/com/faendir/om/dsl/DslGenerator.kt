package com.faendir.om.dsl

import com.faendir.om.parser.solution.model.Action
import com.faendir.om.parser.solution.model.Solution
import com.faendir.om.parser.solution.model.SolvedSolution
import com.faendir.om.parser.solution.model.part.*
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.joinToCode
import io.github.enjoydambience.kotlinbard.codeBlock
import io.github.enjoydambience.kotlinbard.controlFlow


object DslGenerator {
    fun toDsl(solution: Solution): String {
        return FileSpec.scriptBuilder("")
            .addCode(codeBlock {
                controlFlow("solution") {
                    addStatement("puzzle = %S", solution.puzzle)
                    addStatement("name = %S", solution.name)
                    if (solution is SolvedSolution) {
                        controlFlow("score") {
                            addStatement("cost = %L", solution.cost)
                            addStatement("cycles = %L", solution.cycles)
                            addStatement("area = %L", solution.area)
                            addStatement("instructions = %L", solution.instructions)
                        }
                    }
                    for (part in solution.parts) {
                        when (part) {
                            is Arm -> controlFlow("arm(%L)", part.type.name) {
                                addStatement("number = %L", part.number)
                                addStatement("position = %L to %L", part.position.x, part.position.y)
                                addStatement("rotation = %L", part.rotation)
                                addStatement("size = %L", part.size)
                            }
                            is Glyph -> controlFlow("glyph(%L)", part.type.name) {
                                addStatement("position = %L to %L", part.position.x, part.position.y)
                                addStatement("rotation = %L", part.rotation)
                            }
                            is IO -> controlFlow("io(%L)", part.type.name) {
                                addStatement("index = %L", part.index)
                                addStatement("position = %L to %L", part.position.x, part.position.y)
                                addStatement("rotation = %L", part.rotation)
                            }
                            is Track -> controlFlow("track") {
                                addStatement("position = %L to %L", part.position.x, part.position.y)
                                addStatement("positions = listOf(%L)", part.positions.map { codeBlock("%L to %L", it.x, it.y) }.joinToCode(", "))
                            }
                            is Conduit -> controlFlow("conduit") {
                                addStatement("id = %L", part.id)
                                addStatement("position = %L to %L", part.position.x, part.position.y)
                                addStatement("rotation = %L", part.rotation)
                                addStatement("positions = listOf(%L)", part.positions.map { codeBlock("%L to %L", it.x, it.y) }.joinToCode(", "))
                            }
                            else -> throw IllegalArgumentException("Unknown part type $part")
                        }
                    }
                    controlFlow("tape") {
                        addStatement("parallel(%L)", solution.parts.filterIsInstance<Arm>().map {
                            codeBlock {
                                controlFlow("") {
                                    controlFlow("sequence(%L)", it.number) {
                                        for (action in toMethodCalls(it.steps)) {
                                            add("$action\n")
                                        }
                                    }
                                }
                            }
                        }.joinToCode(","))
                    }
                }
            })
            .build()
            .toString()
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
