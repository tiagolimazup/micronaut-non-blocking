package br.com.zup.bootcamp.orange

import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.scheduling.TaskExecutors
import io.reactivex.Single
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
                    .let { Single.just(it)
                                .doOnSubscribe { log.info("the thread starting is ${Thread.currentThread().name}") }
                                .subscribeOn(ioScheduler)
                                .map { it.toCat() }
                                .map { cats.save(it) }
                                .doOnTerminate { log.info("the thread ending is ${java.lang.Thread.currentThread().name}") }}

    @Get("/{id}")
    fun get(@PathVariable id: Int) =
            id.also { log.info("the thread starting is ${Thread.currentThread().name}") }
                .let { Single.just(it)
                            .doOnSubscribe { log.info("the thread starting is ${Thread.currentThread().name}") }
                            .subscribeOn(ioScheduler)
                            .map { cats.findById(it) }
                            .delay(1000, TimeUnit.MILLISECONDS)
                            .observeOn(ioScheduler)
                            .doOnTerminate { log.info("the thread ending is ${Thread.currentThread().name}") }}
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
interface Cats : CrudRepository<Cat, Int>

