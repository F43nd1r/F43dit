package com.faendir.om.dsl.api

import com.faendir.om.dsl.OmDsl
import com.faendir.om.sp.base.Action
import com.faendir.om.sp.part.Arm
import kotlin.jvm.JvmName
import kotlin.math.max

@OmDsl
class Tape(private val solution: SolutionBuilder) {
    private val steps = mutableListOf<Step>()
    private val subTapes = mutableListOf<Pair<Tape, Int>>()

    @OmDsl
    fun step(initializer: Step.() -> Unit) {
        val step = Step()
        step.initializer()
        steps.add(step)
    }

    @OmDsl
    fun sequence(arm: Int, actions: ArmRepresentation.() -> Unit) {
        ArmRepresentation {
            steps.add(Step(mutableMapOf(arm to it)))
        }.actions()
    }

    fun waitComplete() {
        wait(getLength() - steps.size)
    }

    fun waitAlmostComplete(overlap: Int) {
        wait(getLength() - steps.size - overlap)
    }

    fun waitCompleteOn(tape: Tape) {
        wait(max(steps.size, subTapes.filter { it.first == tape }.map { it.first.getLength() + it.second }.max() ?: 0) - steps.size)
    }

    fun waitAlmostCompleteOn(tape: Tape, overlap: Int) {
        wait(max(steps.size, subTapes.filter { it.first == tape }.map { it.first.getLength() + it.second }.max() ?: 0) - steps.size - overlap)
    }

    @JvmName("wait0")
    fun wait() {
        steps.add(Step())
    }

    fun wait(steps: Int) {
        repeat(steps) { wait() }
    }

    fun getLength(): Int {
        return max(steps.size, subTapes.map { it.first.getLength() + it.second }.max() ?: 0)
    }

    @OmDsl
    fun start(initializer: Tape.() -> Unit): Tape {
        val tape = Tape(solution)
        tape.initializer()
        subTapes.add(tape to steps.size)
        return tape
    }

    @OmDsl
    fun parallel(vararg initializers: Tape.() -> Unit) {
        initializers.forEach {
            start {
                it()
            }
        }
        waitComplete()
    }

    /**
     * overlap to next call
     */
    @OmDsl
    fun parallel(overlap: Int, vararg initializers: Tape.() -> Unit) {
        initializers.forEach {
            start {
                it()
            }
        }
        waitAlmostComplete(overlap)
    }

    private fun getSteps(): List<Step> {
        val out = steps.toMutableList()
        subTapes.forEach { (tape, offset) ->
            val merge = tape.getSteps()
            repeat(offset + merge.size - out.size) {
                out.add(Step())
            }
            for (i in merge.indices) {
                out[i + offset].merge(merge[i])
            }
        }
        return out
    }

    fun save() {
        val instructions = mutableMapOf<Int, MutableList<Action>>()
        val arms = getSteps().map { it.moves.keys.max() ?: 0 }.max() ?: 0
        for (arm in 1..arms) {
            instructions[arm] = mutableListOf()
        }
        getSteps().forEach {
            instructions.forEach { (arm, actions) ->
                actions.add(it.moves[arm] ?: Action.EMPTY)
            }
        }
        instructions.forEach { (arm, instr) ->
            solution.parts.firstOrNull { it is Arm && it.number == arm }?.apply { steps = instr.toList() }
        }
    }
}

@OmDsl
class Step(val moves: MutableMap<Int, Action> = mutableMapOf()) {
    fun arm(i: Int): ArmRepresentation = ArmRepresentation { moves[i] = it }

    fun merge(other: Step) {
        other.moves.forEach { (arm: Int, action: Action) ->
            if (!moves.containsKey(arm) || moves[arm] == Action.EMPTY) {
                moves[arm] = action
            }
        }
    }
}


@OmDsl
class ArmRepresentation(private val output: (Action) -> Unit) {
    fun rotateClockwise() = output(Action.ROTATE_CLOCKWISE)

    fun rc() = rotateClockwise()

    fun rotateCounterClockwise() = output(Action.ROTATE_COUNTERCLOCKWISE)

    fun rcc() = rotateCounterClockwise()

    fun extend() = output(Action.EXTEND)

    fun e() = extend()

    fun retract() = output(Action.RETRACT)

    fun r() = retract()

    fun grab() = output(Action.GRAB)

    fun g() = grab()

    fun drop() = output(Action.DROP)

    fun d() = drop()

    fun pivotClockwise() = output(Action.PIVOT_CLOCKWISE)

    fun pc() = pivotClockwise()

    fun pivotCounterClockwise() = output(Action.PIVOT_COUNTERCLOCKWISE)

    fun pcc() = pivotCounterClockwise()

    fun forward() = output(Action.FORWARD)

    fun f() = forward()

    fun back() = output(Action.BACK)

    fun b() = back()

    @JvmName("wait0")
    fun wait() = output(Action.EMPTY)

    fun wait(steps: Int) = repeat(steps) { wait() }

    fun extendTape() = output(Action.NOOP)

    fun reset() = output(Action.RESET)

    fun repeat() = output(Action.REPEAT)
}