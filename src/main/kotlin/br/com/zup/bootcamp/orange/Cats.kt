package br.com.zup.bootcamp.orange

import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import org.slf4j.LoggerFactory
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Controller("/cats")
class CatResource(val cats: Cats) {

    val log = LoggerFactory.getLogger(CatResource::class.java)

    @Post
    fun create(@Body request: CreateNewCatRequest) =
            request.also { log.info("the thread starting is ${Thread.currentThread().name}") }
                    .toCat()
                    .let { cats.save(it) }
                    .also { log.info("the thread ending is ${Thread.currentThread().name}") }

    @Get("/{id}")
    fun get(@PathVariable id: Int) =
            id.also { log.info("the thread starting is ${Thread.currentThread().name}") }
                .let { cats.findById(id) }
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
interface Cats : CrudRepository<Cat, Int>

