import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

//表示多个值

fun simple(): List<Int> = listOf(1, 2, 3)
fun simple1(): Sequence<Int> = sequence {
    for (i in 1..3){
        Thread.sleep(100)
        yield(i)
    }
}


//挂起函数
suspend fun simple2(): List<Int> {
    delay(1000)
    return listOf(4, 5, 6)
}





//fun main() {
//    simple().forEach { value -> println(value) }
//    simple1().forEach { value -> println(value) }
//
//    runBlocking {
//        simple2().forEach{ value -> println(value) } //这段代码将会在等待一秒之后打印数字。
//    }
//}

//流
fun streamSimple(): Flow<Int> = flow {
    println("Flow started")
    for (i in 1..3){
        delay(100)
        emit(i)
    }
}

//fun main(): Unit = runBlocking {
////    launch {
////        for (k in 1..3){
////            println("I'm not blocked $k")
////            delay(100)
////        }
////    }
//
////    streamSimple().collect { value -> println(value) }
//
//    println("Calling simple function")
//    val flow = streamSimple()
//    println("Calling collect...")
//    flow.collect { value -> println(value) }
//    println("Calling collect again...")
//    flow.collect { value -> println(value) }
//
//    withTimeoutOrNull(250) {
//        flow.collect {value -> println(value) }
//    }
//    println("Done!")
//
//}

//名为 flow 的 Flow 类型构建器函数。
//flow { ... } 构建块中的代码可以挂起。
//函数 simple 不再标有 suspend 修饰符。
//流使用 emit 函数 发射 值。
//流使用 collect 函数 收集 值。


//流构建器

//flowOf 构建器定义了一个发射固定值集的流。
//使用 .asFlow() 扩展函数，可以将各种集合与序列转换为流。

//fun main() = runBlocking {
//    //将一个整数区间转化为流
//    (1..3).asFlow().collect { value -> println(value) }
//}


//过渡流操作符
suspend fun performRequest(request: Int): String {
    delay(1000)
    return "response $request"
}

//限长操作符
fun numbers(): Flow<Int> = flow {
    try {
        emit(1)
        emit(2)
        println("This line will not execute")
        emit(3)
    }finally {
        println("Finally in numbers")
    }
}

fun main() = runBlocking {
    (1..3).asFlow().map { request -> performRequest(request) }.collect {
        response -> println(response)
    }

    //转换操作符
    (1..3).asFlow() //一个请求流
        .transform { request ->
            emit("Making request $request")
            emit(performRequest(request))
        }.collect{ response -> println(response) }

    numbers().take(2).collect{value -> println(value) }

    //末端流操作符
    val sum = (1..5).asFlow().map { it  * it }.reduce{ a, b -> a+b }
    println(sum)

    //流是连续的
    //流的每次单独收集都是按顺序执行的，除非进行特殊操作的操作符使用多个流
    (1..5).asFlow()
        .filter {
            println("Filter $it")
            it % 2 == 0
        }.map {
            println("Map $it")
            "string $it"
        }.collect{
            println("Collect $it")
        }
}


