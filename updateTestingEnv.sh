exitIfNeeded () {
  if [ $? -ne 0 ]
	then
		exit 1
	fi
}

prefix=`(cd ../ && ./utils/project-prefix)`
(cd ../ && ./gradlew :$prefix-sql-plugin:clean < /dev/null)

exitIfNeeded

(cd ../ && ./gradlew :$prefix-sql-plugin:build -x test < /dev/null)

exitIfNeeded

cp ./build/libs/* ../plugin
