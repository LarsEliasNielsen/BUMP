package cloud.larn.bump

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BumpApplication

fun main(args: Array<String>) {
    runApplication<BumpApplication>(*args)
}
