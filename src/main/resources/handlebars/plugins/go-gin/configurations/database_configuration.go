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

func init() {
	DATABASE_HOST := os.Getenv(DATABASE_HOST)
	if DATABASE_HOST == "" {
		panic("database host its not defined")
	}
	var err error
	//user:password@/dbname?charset=utf8&parseTime=True&loc=Local"
	engine, err = gorm.Open("mysql", DATABASE_HOST)
	if err != nil {
		panic(err.Error())
	}
	engine.DB().SetMaxOpenConns(10)
	engine.DB().SetMaxIdleConns(1)
}

func GetDB() *gorm.DB {
	return engine
}

