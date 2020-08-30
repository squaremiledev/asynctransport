run:
	@./gradlew clean test distZip && unzip -q asynctcpacceptance/build/distributions/asynctcpacceptance.zip -d asynctcpacceptance/build/distributions
	@echo "run with ./asynctcpacceptance/build/distributions/asynctcpacceptance/bin/asynctcpacceptance"
	@echo "--------------------------"
	@./asynctcpacceptance/build/distributions/asynctcpacceptance/bin/asynctcpacceptance
