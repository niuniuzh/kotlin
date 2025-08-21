import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

/**
 * 这是一个更复杂的生产者-消费者示例，包含多个生产者和多个消费者。
 *
 * 关键改动：
 * - 当有多个生产者时，任何一个生产者都不应该单方面关闭 Channel，
 *   否则其他生产者在尝试发送数据时会出错。
 * - 关闭 Channel 的责任转移给了管理这些协程的父级作用域（在这里是 main 函数）。
 *   父作用域会等待所有生产者都完成后，再安全地关闭 Channel。
 */

// 生产者协程
// 新增了 id 参数用于区分，并移除了 channel.close() 调用。
suspend fun producer(id: Int, channel: kotlinx.coroutines.channels.SendChannel<String>) {
    println("Producer #$id: Starting to produce...")
    val items = listOf("Apple", "Banana", "Cherry", "Date")
    items.forEach { item ->
        val message = "$item from #$id"
        println("Producer #$id: Producing -> $message")
        delay(200L * id) // 让不同的生产者有不同的生产速率
        channel.send(message)
    }
    println("Producer #$id: Finished producing.")
}

// 消费者协程（无需任何改动）
suspend fun consumer(id: Int, channel: kotlinx.coroutines.channels.ReceiveChannel<String>) {
    println("Consumer #$id: Ready to consume.")
    for (item in channel) {
        println("Consumer #$id: Received --> $item")
        delay(500) // 模拟消费时间
    }
    println("Consumer #$id: Channel is empty and closed. Finishing.")
}

fun main() = runBlocking {
    println("Main: Setting up the channel...")
    val channel = Channel<String>()

    // 启动三个消费者协程
    repeat(3) { consumerId ->
        launch {
            consumer(consumerId + 1, channel)
        }
    }

    // 启动两个生产者协程
    val producerJobs = List(2) { producerId ->
        launch {
            producer(producerId + 1, channel)
        }
    }

    println("Main: All coroutines launched.")

    // 等待所有的生产者 Job 完成
    producerJobs.joinAll()
    println("Main: All producers have finished their work.")

    // 现在，当且仅当所有生产者都完成后，我们才安全地关闭 Channel
    channel.close()
    println("Main: Channel is now closed.")

    // runBlocking 会等待所有子协程（包括消费者）执行完毕
}