const express = require('express')
const app = express()
const middleware=require('./src/middleware')

const corsConfig = {
    'origin': '*',
    'methods': 'GET,HEAD,PUT,PATCH,POST,DELETE',
    'preflightContinue': false,
    'optionsSuccessStatus': 204
}
const cors = require('cors')
/*
Load configuration
 */
let envPath = process.env.NODE_ENV === undefined ? 'env.test' : 'env.' + process.env.NODE_ENV
console.log('app runs under environment' + envPath)

let config = require('./src/configurations')
config.loadConfiguration(envPath)

const db = config.loadDatabase(config).connection
db.on('error', console.error.bind(console, 'connection error:'))
db.once('open', function () {
    console.log('db connected')
})
app.use(cors(corsConfig))
app.use(middleware.requestId)
app.use(middleware.globalRequest)
app.use(require('./src/controllers/routers'))
app.use(function (req, res, next) {
    return res.status(404).send("Sorry can't find that!")
})
const server = app.listen(config.environment.port, () => {
    console.log('Listen on port ' + config.environment.port)
})

const sigs = ['SIGINT', 'SIGTERM', 'SIGQUIT']
sigs.forEach(sig => {
    process.on(sig, () => {
        console.log(sig)
        // Stops the server from accepting new connections and finishes existing connections.
        server.close(function (err) {
            // close your database connection and exit with success
            // for example with mongoose
            db.close(function () {
                console.log('Mongoose connection disconnected')
                process.exit(0)
            })
        })
    })
})
