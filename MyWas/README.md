# WAS 구현

1. 설정파일명		: config.json;

2. 로깅설정관련
	1) 관련파일		: logback.xml
	2) 로그파일경로		: /Users/SungJun/MyWas/log/

3. 사전 준비경로 및 파일
	1) 도큐먼트루트 	: /Users/SungJun/MyWas/docroot/
	2) 필요파일 (프로젝트 내 'docroot_temp/' 하위 경로에 아래 파일들이 있음)
		index.html
		a.html
		b.html
		403.html
		404.html
		500.html
		501.html

4. 프로젝트 패키지 구조
	1) [src/main/java/com.mywas]
		Configuration.java
		HttpServer.java
		RequestProcessor.java
	2) [src/main/resource]
		config.json
		logback.xml
