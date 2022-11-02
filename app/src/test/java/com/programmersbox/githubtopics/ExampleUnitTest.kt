package com.programmersbox.githubtopics

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun flowTest(): Unit = runBlocking {
        flow {
            repeat(5) {
                emit(it)
                delay(1000)
            }
        }
            .onEach { println(it) }
            .filter { it % 2 == 0 }
            .onEach { println("Here $it") }
            .launchIn(this)
    }
}