package blog


import io.javalin.ApiBuilder.*
import io.javalin.Javalin
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.javalin.Context
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.Result.Failure
import com.github.kittinunf.result.Result.Success
import com.github.kittinunf.result.getAs
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.javalin.HaltException


fun main(args: Array<String>) {

    val userDao = UserDao()

    //val PORT = System.getenv("port").toInt()
    //val AUTH_TOKEN = System.getenv("auth_token")
    val AUTH_TOKEN = "frewtf43"

    val app = Javalin.create().apply {
        port(7000)
        exception(Exception::class.java) { e, ctx -> e.printStackTrace() }
        error(404) { ctx -> ctx.json("Page not found") }
    }.start()

    app.routes {

        get("/users") { ctx ->
            print("route /users")
            ctx.json(userDao.users)
        }

        get("/testFunc", ::testFunc)

        get("/users/:id") { ctx ->

            "http://192.168.1.188:3004/rtds/all".httpGet().responseString { request, response, result ->
                //do something with response
                when (result) {
                    is Result.Failure -> {
                        val error = result.getAs<String>()
                    }
                    is Result.Success -> {
                        val mapType = object : TypeToken<Map<String, Any>>() {}.type
                        val data: Map<String, String> = Gson().fromJson(result.get(), mapType)
                        println(data)
                    }
                }
            }

            "http://localhost:8080/test/users".httpPost().header(
                    "authorization" to "Basic some-token",
                    "content-type" to "application/json"
            ).body("""
                {
                    "name": "Vikrant",
                    "email": "something@abc.com"
                }
        """.trimIndent()).responseString { request, response, result: Result<String, FuelError> ->
                //do something with outputResponse
                val op: Any = when (result) {
                    is Failure -> {
                        val error = String(result.error.errorData)
                        error
                    }
                    is Success -> {
                        val mapType = object : TypeToken<Map<String, Any>>() {}.type
                        val data: Map<String, String> = Gson().fromJson(result.get(), mapType)
                        println(data)
                        data
                    }
                }
                //ctx.status(200).result(op.toString())
            }

            ctx.json(userDao.findById(ctx.param("id")!!.toInt())!!)
        }

        get("/users/email/:email") { ctx ->
            ctx.json(userDao.findByEmail(ctx.param("email")!!)!!)
        }

        post("/users/create") { ctx ->
            val aa = ctx.body() // return string
            //println("Request body as class ${jacksonObjectMapper().readValue<User>(ctx.body(),User::class.java)}")
            //val user = jacksonObjectMapper().readValue<User>(ctx.body(),User::class.java)

            val user = ctx.bodyAs(User::class.java)
            userDao.save(name = user.name, email = user.email)
            ctx.status(201)
        }

        patch("/users/update/:id") { ctx ->
            val user = ctx.bodyAsClass(User::class.java)
            userDao.update(
                    id = ctx.param("id")!!.toInt(),
                    user = user
            )
            ctx.status(204)
        }

        delete("/users/delete/:id") { ctx ->
            userDao.delete(ctx.param("id")!!.toInt())
            ctx.status(204)
        }

        /*
        app.before { ctx ->
            val authToken = ctx.header("auth_token")
            println(authToken)
            if (authToken != AUTH_TOKEN) {
                throw HaltException(401, "Unauthorized access")
            }
        }

        app.exception(Exception::class.java, { e, ctx ->
            ctx.status(500).result(e.message!!)
        })
        */
    }

}

// extension functions

// curl localhost:7000/hello?abc=43213 -H 'auth_token: abcde'
fun Context.reqParam(name: String): String = this.request().getParameter(name)

// curl -X POST localhost:7000/http-post2 -d '{"message":"something msg"}' -H 'auth_token: abcde'
fun <T> Context.bodyAs(clazz: Class<T>): T = jacksonObjectMapper().readValue<T>(this.body(), clazz)

fun testFunc(ctx: Context) {
    print("route with local function")
    ctx.result("FROM TEST FUNC")
}