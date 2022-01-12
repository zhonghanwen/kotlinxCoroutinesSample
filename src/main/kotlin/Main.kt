import kotlinx.coroutines.*
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds

//fun main() {
//    GlobalScope.launch {  //在后台启动一个新的协程并继续
//        delay(1000L)
//        print("World!") //在延迟后打印输出
//    }
//    print("Hello,")
//
//
//    thread {
//        //delay(1000L)
//        print(" thread ")
//    }
//
////    Thread.sleep(2000L) //阻塞主线程2秒种来保证JVM存活
//
//    runBlocking {
//        delay(2000L)
//    }
//}


//fun main() = runBlocking<Unit> {
//    GlobalScope.launch {
//        delay(1000L)
//        print("world")
//    }
//    print("Hello,")
//    delay(2000L)
//}


//fun main() = runBlocking {
//    val job = GlobalScope.launch {
//        delay(1000L)
//        println("World!")
//
//    }
//
//    println("Hello,")
//    job.join(); //等待直到子协程执行结束
//
//}


//fun main() = runBlocking {
//    launch {  //runBlocking作用域中启动一个新协程
//        delay(1000L)
//        println("world")
//    }
//
//    println("Hello,")
//}


//fun main() = runBlocking {
//    println("start")
//    launch {
//        println("launch...")
//        delay(200L)
//        println("Task from runBlocking")
//    }
//
//    println("begin")
//    coroutineScope {
//        println("coroutine scope")
//        launch {
//            delay(500L)
//            println("Task from nested launch")
//        }
//
//        delay(100L)
//        println("Task from coroutine scope")
//    }
//
//    println("Coroutine scope is over")
//}


//fun main() = runBlocking {
//    launch { doWorld() }
//    println("Hello,")
//}

//这是我的第一个挂起函数
suspend fun doWorld(){
    delay(2000L)
    println("World!")
}

//协程就是像守护进程，没有进程保活的作用。
//fun main() = runBlocking {
//    GlobalScope.launch {
//        repeat(1000) { i ->
//            println("I'm sleeping $i ...")
//            delay(500L)
//        }
//    }
//    delay(1300L) // 在延迟后退出
//}

//fun main() = runBlocking {
//    val job = launch {
//        repeat(1000){
//            println("job: I'm sleeping $it")
//            delay(500L)
//        }
//    }
//    delay(1300L) //延迟一段时间
//    println("main: I'm tired of waiting!")
////    job.cancel()
////    job.join()
//    job.cancelAndJoin()
//    println("main: Now I can quit.")
//}

//fun main() = runBlocking {
//    val startTime = System.currentTimeMillis()
//    val job = launch(Dispatchers.Default) {
//        var nextPrintTime = startTime
//        var i = 0
//        while (isActive){ //一个执行计算的循环，只是为了占用CPU
//            //每秒打印消息两次
//            if(System.currentTimeMillis() >= nextPrintTime){
//                println("job: I'm sleeping ${i++}")
//                nextPrintTime += 500L
//            }
//        }
//    }
//    delay(1300L)
//    println("main: I'm tired of waiting!")
//    job.cancelAndJoin()
//    println("main: Now I can quit.")
//}

//在finally中释放资源 以及运行不能取消的代码块
//fun main() = runBlocking{
//    val job = launch {
//        try {
//            repeat(1000) {
//                println("job: I'm sleeping $it")
//                delay(500L)
//            }
//        } finally { //finally调用挂起函数的行为都会抛出Cancellation
//            withContext(NonCancellable){ //运行不可以取消的代码
//                println("job: I'm running finally")
//                doWorld()
//            }
//        }
//    }
//    delay(1300L)
//    println("main: I'm tried of waiting!")
//    job.cancelAndJoin()
//    println("main: Now I can quit.")
//}

//超时
//fun main(): Unit = runBlocking {
//    withTimeoutOrNull(1300L){
//        repeat(1000){
//            println("I'm sleeping $it")
//            delay(500L)
//        }
//    }
//}


var acquired = 0

class Resource {
    init { acquired ++ } // Acquire the resource
    fun close() { acquired-- } // Release the resource
}

//fun main() {
//    runBlocking {
//        repeat(100_000) { // Launch 100K coroutines
//            launch {
//                val resource = withTimeout(67) { // Timeout of 60 ms
//                    delay(50) // Delay for 50 ms
//                    Resource() // Acquire a resource and return it from withTimeout block
//                }
//                resource.close() // Release the resource
//            }
//        }
//    }
//
//    // Outside of runBlocking all coroutines have completed
//    println(acquired) // Print the number of resources still acquired
//}
//result: 如果你运行上面的代码，你会看到它并不总是打印零，尽管它可能取决于你机器的时间，你可能需要在这个例子中调整超时以实际看到非零值。

//fun main() {
//    runBlocking {
//        repeat(100_000){
//            launch {
//                var resource : Resource? = null
//                try {
//                    withTimeout(64){
//                        val duration = 50.milliseconds
//                        delay(duration)
//                        resource = Resource()
//                    }
//                } finally {
//                    resource?.close()
//                }
//            }
//        }
//    }
//
//    println(acquired)
//}
//result: 此示例始终打印零,资源不泄露。


//组合函数
suspend fun doSomethingUsefulOne(): Int {
    delay(1000L)
    return 13
}


suspend fun doSomethingUsefulTwo(): Int {
    delay(1000L)
    return 29
}

//fun main() = runBlocking {
//    //顺序执行
//    val time = measureTimeMillis {
//        val one = doSomethingUsefulOne()
//        val two = doSomethingUsefulTwo()
//        println("The answer is ${one + two}")
//    }
//    println("Completed in $time ms")
//
//    //异步并发
//    val time1 = measureTimeMillis {
//        val one = async { doSomethingUsefulOne() }
//        val two = async { doSomethingUsefulTwo() }
//        println("The answer is ${one.await() + two.await()}")
//    }
//    println("Completed in $time1 ms")
//
//    //惰性的
//    val time2 = measureTimeMillis {
//        val one = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne() }
//        val two = async(start = CoroutineStart.LAZY) { doSomethingUsefulTwo() }
//        //执行一些计算
//        one.start()
//        two.start()
//        println("The answer is ${one.await() + two.await()}")
//
//    }
//    println("Completed in $time2")
//
//
//
//}
//


fun main() {
    val time = measureTimeMillis {
        //我们可以在协程外启动异步执行
        val one = somethingUsefulOneAsync()
        val two = somethingUsefulTwoAsync()
        //但是结果必须调用其他的挂起或者阻塞

        runBlocking {
            println("The answer is ${one.await() + two.await()}")
        }
    }

    println("Completed in $time ms")
}

//async 风格的函数
fun somethingUsefulOneAsync() = GlobalScope.async {
    doSomethingUsefulOne()
}

fun somethingUsefulTwoAsync() = GlobalScope.async {
    doSomethingUsefulTwo()
}

