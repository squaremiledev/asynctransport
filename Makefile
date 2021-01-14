run:
	@./gradlew clean test && \
	./gradlew asynctcpacceptance:distZip && unzip -q asynctcpacceptance/build/distributions/asynctcpacceptance.zip -d asynctcpacceptance/build/distributions && \
	./gradlew trcheck:shadowDistZip && unzip -q trcheck/build/distributions/trcheck*.zip -d trcheck/build/distributions/