run:
	@./gradlew clean test distZip && unzip -q asynctcpacceptance/build/distributions/asynctcpacceptance.zip -d asynctcpacceptance/build/distributions
	@./asynctcpacceptance/build/distributions/asynctcpacceptance/bin/asynctcpacceptance

	@echo ""
	@echo "to run sample app on port 9999, execute"
	@echo "     ./asynctcpacceptance/build/distributions/asynctcpacceptance/bin/asynctcpacceptance 9999"
	@echo "and connect with"
	@echo "     telnet localhost 9999"
	@echo ""
