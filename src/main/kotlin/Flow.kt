import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.system.measureTimeMillis

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

fun simpleWithContext(): Flow<Int> = flow {
    withContext(Dispatchers.Default){
        for (i in 1..3){
            Thread.sleep(100)
            emit(i)
        }
    }
}

fun simpleFlowOn(): Flow<Int> = flow {
    for(i in 1..3){
        Thread.sleep(100)
        log("Emitting $i")
        emit(i)
    }
}.flowOn(Dispatchers.IO) //在流构建器中改变消耗 CPU 代码上下文正确方式

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

    //缓冲
    val time = measureTimeMillis {
        simpleFlowOn()
            .buffer() // 缓冲发射项，无需等待
            .collect{value ->
            delay(300)
            log("Collected $value")
        }
    }
    println("Collected in $time ms")

    //合并
    val time1 = measureTimeMillis {
        simpleFlowOn()
            .conflate() //合并发射项，不对每个值进行处理
            .collect{
                delay(300)
                println(it)
            }
    }
    println("conflate Collected in $time1 ms")

    //处理最新值
    val time3 = measureTimeMillis {
        simpleFlowOn()
            .collectLatest {
                println("Collect $it")
                delay(300)
                println("Done $it")
            }
    }
    println("collectLatest Collected in $time3 ms")

    //组合多个流

    //zip
    val nums = (1..3).asFlow()
    val strs = flowOf("one", "two", "three")
    nums.zip(strs){a, b-> "$a -> $b"}.collect{println(it)}

    //Combine
    //当流表示一个变量或操作的最新值时，可能需要执行计算，这依赖于相应流的最新值，并且每当上游流产生值的时候都需要重新计算。这种相应的操作符家族称为 combine。
    val nums1 = (1..3).asFlow().onEach { delay(300) }
    val strs1 = flowOf("one", "two", "three").onEach { delay(400) }
    val startTime = System.currentTimeMillis() //记录开始的时间
    nums1.combine(strs1){a, b -> "$a -> $b"}
        .collect{
            println("$it at ${System.currentTimeMillis() - startTime} ms from start")
        }


    //展平流

    //flatMapConcat 连接模式由 flatMapConcat 与 flattenConcat 操作符实现
    val startTime1 = System.currentTimeMillis()
    (1..3).asFlow().onEach { delay(100) }
        .flatMapConcat { requestFlow(it) }
        .collect{
            println("$it at ${System.currentTimeMillis() - startTime1} ms from start")
        }


    //flatMapMerge 另一种展平模式是并发收集所有传入的流，并将它们的值合并到一个单独的流，以便尽快的发射值.
    val startTime3 = System.currentTimeMillis()
    (1..3).asFlow().onEach { delay(100) }
        .flatMapMerge { requestFlow(it) }
        .collect{
            println("flatMapMerge $it at ${System.currentTimeMillis() - startTime3} ms from start")
        }


    //flatMapLatest
    val startTime4 = System.currentTimeMillis()
    (1..3).asFlow().onEach { delay(100) }
        .flatMapLatest { requestFlow(it) }
        .collect{
            println("flatMapLatest $it at ${System.currentTimeMillis() - startTime4} ms from start")
        }


    //异常

    try {
        streamSimple().collect {
            println(it)
            check(it <= 1) {"Collected $it"}
        }
    }catch (e: Throwable){
        println("Caught $e")
    }

    //一切都已捕获
    try {
        simpleException().collect{
            println(it)
        }
    }catch (e: Throwable){
        println("Caught $e")
    }

    //可以使用 throw 重新抛出异常。
    //可以使用 catch 代码块中的 emit 将异常转换为值发射出去。
    //可以将异常忽略，或用日志打印，或使用一些其他代码处理它。
    simpleException()
        .catch { e -> emit("Caught $e") } // 不会捕获下游异常
        .collect{
            println(it)
    }

//    simpleEx()
//        .catch { e -> println("Caught $e") } // 不会捕获下游异常
//        .collect { value ->
//            check(value <= 1) { "Collected $value" }
//            println(value)
//        }

    //声明式捕获
    simpleEx()
        .onEach {
            check(it <= 1) { "Collected $it" }
//            println(value)
        }.catch { e -> println("Caught $e") }
        .collect()

    //命令式 finally 块
    // 收集器还能使用 finally 块在 collect 完成时执行一个动作。
    try {
        simpleEx().collect{value -> println(value) }
    }finally {
        println("Done")
    }


    //声明式处理
    // 对于声明式，流拥有 onCompletion 过渡操作符，它在流完全收集时调用。
    //onCompletion 的主要优点是其 lambda 表达式的可空参数 Throwable 可以用于确定流收集是正常完成还是有异常发生。
    //与 catch 操作符的另一个不同点是 onCompletion 能观察到所有异常并且仅在上游流成功完成
    simpleEx().onCompletion { cause -> if(cause != null) println("Flow completed exceptionally") }
        .catch { println("Caught exception") }
        .collect{value -> println(value) }


    //启动流
    events().onEach { event -> println("Event: $event") }
        .collect()
    println("Done")

    events().onEach { event -> println("Event: $event") }
        .launchIn(this)  //使用 launchIn 替换 collect 我们可以在单独的协程中启动流的收集
    println("Done")

    //流取消检测
//    (1..5).asFlow().collect{ //出于性能原因，大多数其他流操作不会自行执行其他取消检测
//        if(it == 3){
//            cancel()
//        }
//        println(it)
//    }

    (1..5).asFlow().cancellable().collect{
        if(it == 3){
            cancel()
        }
        println(it)
    }

    //让繁忙的流可取消
    //cancellable（）

    //withContext发出错误
//    simpleWithContext().collect{ value -> println(value) } // flow {...} 构建器中的代码必须遵循上下文保存属性，并且不允许从其他上下文中发射（emit）

}

fun events(): Flow<Int> = (1..3).asFlow().onEach { delay(100) }

fun simpleEx(): Flow<Int> = flow {
    for (i in 1..3) {
        println("Emitting $i")
        emit(i)
    }
}

fun simpleException(): Flow<String> =
    flow {
        for (i in 1..3) {
            println("Emitting $i")
            emit(i) // 发射下一个值
        }
    }
        .map { value ->
            check(value <= 1) { "Crashed on $value" }
            "string $value"
        }

fun requestFlow(i: Int): Flow<String> = flow {
    emit("$i: First")
    delay(500)
    emit("$i: Second")
}


