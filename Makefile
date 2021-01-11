run:
	@./gradlew clean test asynctcpacceptance:distZip tcpcheck:shadowDistZip && unzip -q asynctcpacceptance/build/distributions/asynctcpacceptance.zip -d asynctcpacceptance/build/distributions