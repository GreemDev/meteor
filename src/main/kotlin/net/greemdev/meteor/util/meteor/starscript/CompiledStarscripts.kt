/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.meteor.starscript

import meteordevelopment.meteorclient.utils.misc.MeteorStarscript
import meteordevelopment.starscript.Script
import meteordevelopment.starscript.utils.StarscriptError

class CompiledStarscripts : List<Script> {
    private var scripts: MutableList<Script> = mutableListOf()

    fun setScripts(scripts: Collection<String>) {
        this.scripts = scripts.map {
            MeteorStarscript.compile(it)
        }.toMutableList()
    }

    fun runAll(): Pair<List<String>, List<StarscriptError>> {
        val errors = mutableListOf<StarscriptError>()
        val results = mutableListOf<String>()

        scripts.forEach {
            try {
                results.add(MeteorStarscript.run(it))
            } catch (e: StarscriptError) {
                errors.add(e)
            }
        }

        return results to errors
    }

    override val size: Int
        get() = scripts.size

    override fun get(index: Int) = scripts[index]

    override fun isEmpty() = scripts.isEmpty()

    override fun iterator() = scripts.iterator()

    override fun listIterator() = scripts.listIterator()

    override fun listIterator(index: Int) = scripts.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int) = scripts.subList(fromIndex, toIndex)

    override fun lastIndexOf(element: Script) = scripts.lastIndexOf(element)

    override fun indexOf(element: Script) = scripts.indexOf(element)

    override fun containsAll(elements: Collection<Script>) = scripts.containsAll(elements)

    override fun contains(element: Script) = element in scripts

}
