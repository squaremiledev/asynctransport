run:
	@./gradlew clean test distZip && unzip -q build/distributions/sockets.zip -d build/distributions
	@echo "run with ./build/distributions/sockets/bin/sockets"
	@echo "--------------------------"
	@./build/distributions/sockets/bin/sockets
