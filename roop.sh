gradlew clean build
for i in `seq 1 100`
do
  gradlew :simulator:run
done
python log.py