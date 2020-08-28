run:
	@./gradlew clean test distZip && unzip -q asynctcpapp/build/distributions/asynctcpapp.zip -d asynctcpapp/build/distributions
	@echo "run with ./asynctcpapp/build/distributions/asynctcpapp/bin/asynctcpapp"
	@echo "--------------------------"
	@./asynctcpapp/build/distributions/asynctcpapp/bin/asynctcpapp
