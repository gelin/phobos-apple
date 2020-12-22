.PHONY: build
build:
	./gradlew assembleDebug

.PHONY: release
release:
	@stty -echo && read -p "Key password: " pwd && stty echo && \
	STORE_PASSWORD=$$pwd KEY_PASSWORD=$$pwd ./gradlew assembleRelease

.PHONY: clean
clean:
	./gradlew clean

.PHONY: copy
copy:
	rsync -av app/build/outputs/apk/release/app-release.apk \
		ftp.gelin.ru:domains/gelin.ru/public_html/android/phobos-apple/phobos-apple-$(shell ./gradlew -q app:printVersionName).apk
