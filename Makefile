run:
	@./gradlew clean test tcpcheck:shadowDistZip && unzip -q tcpcheck/build/distributions/tcpcheck*.zip -d tcpcheck/build/distributions