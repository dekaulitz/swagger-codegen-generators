package configurations

/**
 * TokitaDevEngine version {{generatorVersion}}
 * Generated by engine {{generatorClass}}
 * Generated on {{generateDate}}
 * Developed by sulaimanfahmi@gmail.com
 */
import (
	_ "github.com/go-sql-driver/mysql"
	"github.com/jinzhu/gorm"
	_ "github.com/jinzhu/gorm/dialects/mysql"
	"os"
)
var (
	engine = &gorm.DB{}
)

func SetDatabaseConnection() {
	var err error
	//user:password@/dbname?charset=utf8&parseTime=True&loc=Local"
	engine, err = gorm.Open("mysql", config.DatabaseHost)
	if err != nil {
		panic(err.Error())
	}
	engine.DB().SetMaxOpenConns(10)
	engine.DB().SetMaxIdleConns(1)
}
func GetDatabaseConnection() *gorm.DB {
	return engine
}

