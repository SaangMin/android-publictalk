**Public-Talk**
-

💬🌏 네이버의 PAPAGO API를 이용한 다양한 언어의 원활한 채팅앱 🌏🗨️

Store : https://play.google.com/store/apps/details?id=com.skysmyoo.publictalk

**기술 스택**
-

- 언어: Kotlin
- Jetpack Component: View Model, Room, Navigation, Compose, Hilt
- 알림 기능: FCM, Broadcast Receiver
- Network: Retrofit, OkHttp3, Moshi
- Image loading library: Coil
- Architecture: MVVM(Kotlin Flow)
- Database: Firebase
- Open Api: 네이버 파파고 API
- Migration: Live Data -> Flow, Service Locator -> Jetpack Hilt

**사용법(v1.0.2)**
-

1️⃣ 구글 로그인을 통해 로그인을 한다.

2️⃣ 상대방에게 보여줄 나의 정보를 등록한다.

3️⃣ 친구 추가 화면에서 친구의 이메일을 입력하여 친구를 추가한다.

4️⃣ 친구와 대화를 시작한다.

**안내사항(v1.0.2)**
-

- 현재 앱 내부의 언어는 한글 / 영어만 지원됩니다.
- 번역 기능은 내가 설정한 언어가 상대방이 설정한 언어로 번역됩니다.
- 번역을 하지 않고 보내실 메세지는 '*'를 메세지 앞에 붙여주세요.
- 상대방을 친구 추가 하지 않으면 채팅을 보낼 수 없고 받기만 할 수 있습니다.(친구 추가 되지 않은 친구의 이름은 이메일로 표시됩니다.)

**개발 기록 문서**
-
https://github.com/SaangMin/android-publictalk.wiki
