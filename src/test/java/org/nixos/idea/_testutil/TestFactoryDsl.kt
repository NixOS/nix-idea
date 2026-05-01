package org.nixos.idea._testutil

import kotlinx.collections.immutable.toImmutableList
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Named

@DslMarker
private annotation class TestFactoryDslMarker

@TestFactoryDslMarker
class TestFactoryDsl private constructor() {

    private val nodes = mutableListOf<DynamicNode>()

    companion object {
        fun testFactory(init: TestFactoryDsl.() -> Unit): List<DynamicNode> {
            val container = TestFactoryDsl()
            container.init()
            return container.nodes.toImmutableList()
        }
    }

    fun test(name: String, test: Test.() -> Unit) {
        nodes += dynamicTest(name, { test.invoke(Test()) })
    }

    fun container(name: String, init: TestFactoryDsl.() -> Unit) {
        val container = TestFactoryDsl()
        container.init()
        if (container.nodes.isNotEmpty()) {
            nodes += dynamicContainer(name, container.nodes.toImmutableList())
        }
    }

    fun <T> tests(name: String, data: Iterable<T>, test: Test.(T) -> Unit) {
        val list = data.toImmutableList()
        when (list.size) {
            0 -> {}
            1 -> nodes += dynamicTest(name, { Test().test(list[0]) })
            else -> nodes += dynamicContainer(name, list.map { dynamicTest(it.toString(), { Test().test(it) }) })
        }
    }

    fun <T> containers(name: String, data: Iterable<T>, init: TestFactoryDsl.(T) -> Unit) {
        val list = data.flatMap {
            val container = TestFactoryDsl()
            container.init(it)
            if (container.nodes.isEmpty()) {
                emptyList()
            } else {
                listOf(Named.of(it.toString(), container.nodes.toImmutableList()))
            }
        }.toImmutableList()
        when (list.size) {
            0 -> {}
            1 -> nodes += dynamicContainer(name, list[0].payload)
            else -> nodes += dynamicContainer(name, list.map { dynamicContainer(it.name, it.payload) })
        }
    }

    @TestFactoryDslMarker
    class Test
}
