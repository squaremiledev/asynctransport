run:
	@./gradlew clean test distZip && unzip -q asynctcpacceptance/build/distributions/asynctcpacceptance.zip -d asynctcpacceptance/build/distributions
	@echo "run with ./asynctcpacceptance/build/distributions/asynctcpacceptance/bin/asynctcpacceptance 9999"
	@echo "--------------------------"
	@./asynctcpacceptance/build/distributions/asynctcpacceptance/bin/asynctcpacceptance
