package configurations

import "github.com/go-redis/redis"

var (
	client *redis.Client
)

func SetRedisConnection() {
	client = redis.NewClient(&redis.Options{
		Addr:       config.RedisHost,
		Password:   "", // no password set
		DB:         0,  // use default DB
		MaxRetries: 3,
	})
}

func GetRedisConnection() *redis.Client {
	return client
}
