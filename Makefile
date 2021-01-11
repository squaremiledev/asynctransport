run:
	@./gradlew clean test distZip && unzip -q tcpcheck/build/distributions/tcpcheck*.zip -d tcpcheck/build/distributions