package com.bogdan

import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.FaviconHandler
import mu.KotlinLogging

fun main(args: Array<String>) {

    val port = 8080

    val logger = KotlinLogging.logger("VertxServer")

    val vertx = Vertx.vertx()

    val server = vertx.createHttpServer()

    val router = Router.router(vertx)

    chunkedRoutes(router)
    parameterizedRoute(router)
    failureRoutes(router)
    faviconHandler(router)

    server.requestHandler { router.accept(it) }.listen(port)
    logger.info { "Started server on port: $port" }


}

private fun parameterizedRoute(router: Router) {
    val route = router.route(HttpMethod.GET, "/parameterized/:id/:name")

    route.handler { routingContext ->

        val id = routingContext.request().getParam("id")
        val name = routingContext.request().getParam("name")

        routingContext.response().end("id=$id\nname=$name")

    }
}

private fun chunkedRoutes(router: Router) {
    router.route("/path").blockingHandler { routingContext ->

        val response = routingContext.response()
        response.isChunked = true
        response.write("route1\n")

        routingContext.vertx().setTimer(500) { routingContext.next() }
    }

    router.route("/path").blockingHandler { routingContext ->

        val response = routingContext.response()
        response.write("route2\n")

        routingContext.vertx().setTimer(500) { routingContext.next() }
    }

    router.route("/path").handler { routingContext ->

        val response = routingContext.response()
        response.write("route3")

        routingContext.response().end()
    }
}

private fun failureRoutes(router: Router) {
    router.get("/error/path1/").handler {
        throw java.lang.RuntimeException("something happened!")
    }

    router.get("/error/path2").handler { routingContext ->
        routingContext.fail(403)
    }

    router.get("/error/*").failureHandler { failureRoutingContext ->
        val statusCode = failureRoutingContext.statusCode()
        // Status code will be 500 for the RuntimeException or 403 for the other failure
        val response = failureRoutingContext.response()
        response.setStatusCode(statusCode).end("Sorry! Not today")
    }
}

private fun faviconHandler(router: Router) {
    router.route().handler(FaviconHandler.create())
}

