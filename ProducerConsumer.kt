import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

/**
 * 这是一个使用 Kotlin 协程和 Channel 实现的经典生产者-消费者问题示例。
 *
 * - Channel: 充当了生产者和消费者之间的通信管道。它是一个线程安全的队列，
 *            当生产者向其中 send 数据，或消费者从中 receive 数据时，都可以被挂起。
 *
 * - Producer: 一个协程，负责创建数据并将其放入 Channel。当生产结束时，它会关闭 Channel。
 *
 * - Consumer: 一个或多个协程，负责从 Channel 中取出数据并进行处理。
 *             使用 for 循环来消费 Channel 是一个惯用法，当 Channel 被关闭且为空时，循环会自动结束。
 */

// 生产者协程
// 它接收一个 SendChannel，表示它只能向这个 Channel 发送数据。
suspend fun producer(channel: kotlinx.coroutines.channels.SendChannel<Int>) {
    println("Producer: Starting to produce numbers...")
    for (x in 1..5) {
        println("Producer: Producing $x")
        // 模拟一些生产所需的时间
        delay(100) 
        // 将生产出的数字发送到 channel
        channel.send(x * x)
    }
    // 生产结束后，关闭 channel，这是一个很重要的步骤。
    // 它会通知消费者不会再有新的数据传来。
    channel.close()
    println("Producer: Done producing. Channel closed.")
}

// 消费者协程
// 它接收一个 ReceiveChannel，表示它只能从这个 Channel 接收数据。
// id 参数用于区分不同的消费者实例。
suspend fun consumer(id: Int, channel: kotlinx.coroutines.channels.ReceiveChannel<Int>) {
    println("Consumer #$id: Ready to consume.")
    // 使用 for 循环来消费 channel 中的数据。
    // 这个循环会在 channel 中有新数据时执行，
    // 并在 channel 被关闭且取完所有数据后自动终止。
    for (y in channel) {
        println("Consumer #$id: Received --> $y")
        // 模拟一些消费所需的时间
        delay(500)
    }
    println("Consumer #$id: Channel is empty and closed. Finishing.")
}

fun main() = runBlocking {
    println("Main: Setting up the channel and coroutines...")

    // 创建一个可以缓冲 2 个整数的 Channel。
    // 如果不设置缓冲或缓冲为0 (Channel.RENDEZVOUS)，
    // 生产者每次 send 时都会挂起，直到有消费者 receive。
    val channel = Channel<Int>(2)

    // 启动一个生产者协程
    launch {
        producer(channel)
    }

    // 启动两个消费者协程，它们会竞争从 Channel 中获取数据
    launch {
        consumer(1, channel)
    }
    launch {
        consumer(2, channel)
    }

    println("Main: Coroutines launched. Waiting for them to complete.")
    // runBlocking 会自动等待其作用域内的所有子协程执行完毕
}
