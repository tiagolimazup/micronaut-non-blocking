package br.com.zup.bootcamp.orange

import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import io.micronaut.data.repository.reactive.RxJavaCrudRepository
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Controller("/cats")
class CatResource(@Named(TaskExecutors.IO) val ioExecutor: ExecutorService, val cats: Cats) {

    val ioScheduler = Schedulers.from(ioExecutor)

    val log = LoggerFactory.getLogger(CatResource::class.java)

    @Post
    fun create(@Body request: CreateNewCatRequest) =
            request.also { log.info("the thread starting is ${Thread.currentThread().name}") }
                    .let { it.toCat() }
                    .let { cats.save(it)
                                .subscribeOn(ioScheduler) }
                    .also { log.info("the thread ending is ${java.lang.Thread.currentThread().name}") }

    @Get("/{id}")
    fun get(@PathVariable id: Int) =
            id.also { log.info("the thread starting is ${Thread.currentThread().name}") }
                .let { cats.findById(it)
                            .subscribeOn(ioScheduler)
                            .delay(1000, TimeUnit.MILLISECONDS) }
                .also { log.info("the thread ending is ${Thread.currentThread().name}") }
}

data class CreateNewCatRequest(val name: String, val age: Int) {

    fun toCat() = Cat(name, age)
}

@Entity
class Cat(var name: String, var age: Int) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
}

@Repository
interface Cats : RxJavaCrudRepository<Cat, Int>

